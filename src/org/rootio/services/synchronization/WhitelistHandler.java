/**
 * 
 */
package org.rootio.services.synchronization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.rootio.activities.DiagnosticStatistics;
import org.rootio.radioClient.R;
import org.rootio.tools.cloud.Cloud;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.content.Context;
import android.util.Log;

/**
 * @author Jude Mukundane, M-ITI/IST-UL
 *
 */
public class WhitelistHandler implements SynchronizationHandler {
	private Context parent;
	private Cloud cloud;

	WhitelistHandler(Context parent, Cloud cloud) {
		this.parent = parent;
		this.cloud = cloud;
	}

	public JSONObject getSynchronizationData() {
		return new JSONObject();
	}

	/**
	 * Breaks down the information in the JSON file for program and schedule information
	 * 
	 * @param programDefinition The JSON program definition received from the cloud server
	 */
	public void processJSONResponse(JSONObject synchronizationResponse) {
		FileOutputStream str = null;
		try {
			File whitelistFile = new File(this.parent.getFilesDir().getAbsolutePath() + "/whitelist.json");
			str = new FileOutputStream(whitelistFile);
			str.write(synchronizationResponse.toString().getBytes());
		} catch (Exception e) {
			Log.e(this.parent.getString(R.string.app_name), e.getMessage() == null ? "Null pointer[WhitelistHandler.processJSONObject]" : e.getMessage());
		}
		finally
		{
			try
			{
				str.close();
			}
			catch(Exception e)
			{
				Log.e(this.parent.getString(R.string.app_name), e.getMessage() == null ? "Null pointer[WhitelistHandler.processJSONObject]" : e.getMessage());
			}
		}
	}

	@Override
	public String getSynchronizationURL() {
		return String.format("http://%s:%s/%s/%s/whitelist?api_key=%s", this.cloud.getServerAddress(), this.cloud.getHTTPPort(), "api/station", this.cloud.getStationId(), this.cloud.getServerKey());	
	}

}
