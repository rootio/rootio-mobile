package org.rootio.services;

import java.util.Date;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.content.ContentValues;
import android.content.Context;

public class ServiceState {

    private final int serviceId;
    private int serviceState;
    private final Context context;
    private Date lastUpdatedDate;
    private String serviceName;

    public ServiceState(Context context, int serviceId) {
        this.context = context;
        this.serviceId = serviceId;
        this.fetchServiceState();
    }

    public ServiceState(Context context, int serviceId, String serviceName, int serviceState) {
        this.context = context;
        this.serviceId = serviceId;
        this.serviceName= serviceName;
        this.serviceState = serviceState;
    }

    /**
     * Gets the state of this service
     *
     * @return Integer representing state of this service. 1: Service is runing,
     * 0: Service is not running
     */
    int getServiceState() {
        // for SMS service. always return true
        if (this.serviceId == 2) {
            return 1;
        }
        return this.serviceState;
    }

    /**
     * Sets the state of the service
     *
     * @param serviceState The integer specifying the state of the service
     */
    public void setServiceState(int serviceState) {
        this.serviceState = serviceState;
        this.save();
    }

    /**
     * Gets the date when the service state was last modified
     *
     * @return Date object representing when service state was last modified
     */
    public Date getLastUpdatedDate() {
        return this.lastUpdatedDate;
    }

    /**
     * Persists the state of the service for consideration across reboots or
     * power failures
     */
    public void save() {
        if(serviceStateExists()) {
            updateServiceState();
        }
        else{
            insertServiceState();
        }
    }

    private void updateServiceState() {
        String tableName = "servicestate";
        ContentValues data = new ContentValues();
        data.put("servicestate", serviceState);
        data.put("lastupdateddate", Utils.getCurrentDateAsString("yyyy-MM-dd HH:mm:ss"));
        String whereClause = "id = ?";
        String[] whereArgs = new String[]{String.valueOf(serviceId)};
        //DBAgent agent = new DBAgent(this.context);
        DBAgent.updateRecords(tableName, data, whereClause, whereArgs);
    }

    /**
     * Fetches the state of the service as persisted in the database
     */
    private void fetchServiceState() {
        String tableName = "servicestate";
        String[] columns = new String[]{"service", "servicestate", "lastupdateddate"};
        String whereClause = "id = ?";
        String[] whereArgs = new String[]{String.valueOf(serviceId)};
        //DBAgent agent = new DBAgent(this.context);
        String[][] result = DBAgent.getData(true, tableName, columns, whereClause, whereArgs, null, null, null, null);
        this.serviceState = result.length > 0 ? Utils.parseIntFromString(result[0][1]) : 0;
    }

    private void insertServiceState()
    {
        String tableName = "servicestate";
        ContentValues data = new ContentValues();
        data.put("id", serviceId);
        data.put("service", serviceName);
        data.put("servicestate", serviceState);
        data.put("lastupdateddate", Utils.getCurrentDateAsString("yyyy-MM-dd HH:mm:ss"));
        //String whereClause = "id = ?";
       // String[] whereArgs = new String[]{String.valueOf(serviceId)};
        //DBAgent agent = new DBAgent(this.context);
        DBAgent.saveData(tableName, null, data);
    }

    private boolean serviceStateExists()
    {
        String tableName = "servicestate";
        String[] columns = new String[]{"service", "servicestate", "lastupdateddate"};
        String whereClause = "id = ?";
        String[] whereArgs = new String[]{String.valueOf(serviceId)};
        //DBAgent agent = new DBAgent(this.context);
        String[][] result = DBAgent.getData(true, tableName, columns, whereClause, whereArgs, null, null, null, null);
        return result != null && result.length > 0; // && ? Utils.parseIntFromString(result[0][1]) : 0;
    }
}
