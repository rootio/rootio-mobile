package org.rootio.services.synchronization;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import org.json.JSONObject;
import org.rootio.handset.R;
import org.rootio.services.SynchronizationService;
import org.rootio.tools.cloud.Cloud;
import org.rootio.tools.utils.Utils;

import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.HashMap;
import java.time.LocalDateTime;

public class SynchronizationDaemon implements Runnable {
    private final Context parent;
    private final Cloud cloud;
    private Handler handler;
    private MusicListHandler musicListHandler;
    private boolean isSyncing = false;
    private HashMap<Integer, Long> syncLocks = new HashMap<>();
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("sync").equals("start")) {
                SynchronizationDaemon.this.requestSync(intent.getIntExtra("category", 0));
            } else {
                SynchronizationDaemon.this.cancelSyncLock(intent.getIntExtra("category", 0));
            }
        }
    };

    private void cancelSyncLock(int category) {
        if (this.syncLocks.containsKey((new Integer(category)))) {
            this.syncLocks.remove(new Integer(category));
        }
    }


    @Override
    public void run() {

        IntentFilter syncRequestsFilter = new IntentFilter("org.rootio.services.synchronization.SYNC_REQUEST");
        this.parent.registerReceiver(this.receiver, syncRequestsFilter);
        this.musicListHandler = new MusicListHandler(this.parent, this.cloud);
        this.synchronize();
    }

    private Long getCurrentTime()
    {
        return LocalDateTime.now().getLong(ChronoField.MILLI_OF_SECOND);
    }

    private void synchronize() {
        if (!isSyncing) {
            this.handler.post(new Runnable() {
                @Override
                public void run() {
                    Thread thread = new Thread(new Runnable() {
                        public void run() {
                            isSyncing = true;
                            if (!SynchronizationDaemon.this.syncLocks.containsKey(new Integer(1)))
                                SynchronizationDaemon.this.synchronize(new DiagnosticsHandler(SynchronizationDaemon.this.parent, SynchronizationDaemon.this.cloud),new Integer(1));

                            //if the syncLock is greater than 5 mins old, do not consider it.
                            if (!(SynchronizationDaemon.this.syncLocks.containsKey(new Integer(2)) && SynchronizationDaemon.this.syncLocks.get(new Integer(2)) < (getCurrentTime () + 300000) )) {
                                SynchronizationDaemon.this.syncLocks.put(new Integer(2), getCurrentTime());
                                SynchronizationDaemon.this.synchronize(new ProgramsHandler(SynchronizationDaemon.this.parent, SynchronizationDaemon.this.cloud),new Integer(2));
                            }
                            if (!SynchronizationDaemon.this.syncLocks.containsKey(new Integer(3)))
                                SynchronizationDaemon.this.synchronize(new CallLogHandler(SynchronizationDaemon.this.parent, SynchronizationDaemon.this.cloud),new Integer(3));
                            if (!SynchronizationDaemon.this.syncLocks.containsKey(new Integer(4)))
                                SynchronizationDaemon.this.synchronize(new SMSLogHandler(SynchronizationDaemon.this.parent, SynchronizationDaemon.this.cloud),new Integer(4));
                            if (!SynchronizationDaemon.this.syncLocks.containsKey(new Integer(5)))
                                SynchronizationDaemon.this.synchronize(new WhitelistHandler(SynchronizationDaemon.this.parent, SynchronizationDaemon.this.cloud),new Integer(5));
                            if (!SynchronizationDaemon.this.syncLocks.containsKey(new Integer(6)))
                                SynchronizationDaemon.this.synchronize(new FrequencyHandler(SynchronizationDaemon.this.parent, SynchronizationDaemon.this.cloud),new Integer(6));
                            if (!SynchronizationDaemon.this.syncLocks.containsKey(new Integer(7)))
                                SynchronizationDaemon.this.synchronize(SynchronizationDaemon.this.musicListHandler,new Integer(7));
                            if (!SynchronizationDaemon.this.syncLocks.containsKey(new Integer(8)))
                                SynchronizationDaemon.this.synchronize(new PlaylistHandler(SynchronizationDaemon.this.parent, SynchronizationDaemon.this.cloud),new Integer(8));
                            if (!SynchronizationDaemon.this.syncLocks.containsKey(new Integer(9)))
                                SynchronizationDaemon.this.synchronize(new LogHandler(SynchronizationDaemon.this.parent, SynchronizationDaemon.this.cloud),new Integer(9));
                            if (!SynchronizationDaemon.this.syncLocks.containsKey(new Integer(10)))
                                SynchronizationDaemon.this.synchronize(new StationHandler(SynchronizationDaemon.this.parent, SynchronizationDaemon.this.cloud),new Integer(10));
                            isSyncing = false;
                        }
                    });
                    thread.start(); //TODO: fix synchronicity. if the interval is too short, next jobs will start before this finishes!
                    if (((SynchronizationService) SynchronizationDaemon.this.parent).isRunning()) { //the service might be stopped in between the scheduling and actual run of this job
                        SynchronizationDaemon.this.handler.postDelayed(this, SynchronizationDaemon.this.getFrequency() * 1000);
                    }
                }
            }); //this is the first run. Maybe do not delay it..
        }
    }

    public void requestSync(final int category) {
        this.syncLocks.put(new Integer(category), getCurrentTime()); //prevent automated sync while this is running
        final SynchronizationHandler syncHandler;
        switch (category) {
            case 1:
                syncHandler = new DiagnosticsHandler(this.parent, this.cloud);
                break;
            case 2:
                syncHandler = new ProgramsHandler(this.parent, this.cloud);
                break;
            case 3:
                syncHandler = new CallLogHandler(this.parent, this.cloud);
                break;
            case 4:
                syncHandler = new SMSLogHandler(this.parent, this.cloud);
                break;
            case 5:
                syncHandler = new WhitelistHandler(this.parent, this.cloud);
                break;
            case 6:
                syncHandler = new FrequencyHandler(this.parent, this.cloud);
                break;
            case 7:
                syncHandler = new StationHandler(this.parent, this.cloud);
                break;
            case 8:
                syncHandler = new MusicListHandler(this.parent, this.cloud);
                break;
            case 9:
                syncHandler = new PlaylistHandler(this.parent, this.cloud);
                break;
            default:
                syncHandler = null;
                break;
        }
        if (handler != null) {
            this.handler.post(new Runnable() {
                @Override
                public void run() {

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SynchronizationDaemon.this.synchronize(syncHandler, new Integer(category));
                        }
                    });
                    thread.start();
                }
            });
        }
    }


    public SynchronizationDaemon(Context parent) {
        this.parent = parent;
        this.cloud = new Cloud(this.parent);
        this.handler = new Handler();
    }

    /**
     * Causes the thread on which it is called to sleep for atleast the specified number of milliseconds
     *
     * @param milliseconds The number of milliseconds for which the thread is supposed to sleep.
     */
    private void getSomeSleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);// frequency is in seconds
        } catch (InterruptedException ex) {
            Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(SynchronizationDaemon.getSomeSleep)" : ex.getMessage());
        }

    }

    /**
     * Fetches the number of seconds representing the interval at which to issue
     * synchronization requests
     *
     * @return Number of seconds representing synchronization interval
     */
    private int getFrequency() {
        try {
            JSONObject frequencies = new JSONObject((String) Utils.getPreference("frequencies", String.class, this.parent));
            return frequencies.getJSONObject("synchronization").getInt("interval");
        } catch (Exception ex) {
            Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(SynchronizationDaemon.toggleData)" : ex.getMessage());
            return 60; // default to 1 minute
        }
    }

    public void synchronize(final SynchronizationHandler handler, Integer id) {
        try {
            String synchronizationUrl = handler.getSynchronizationURL();
            if(synchronizationUrl == null)
            {
                return;
            }
            HashMap<String, Object> response = Utils.doDetailedPostHTTP(synchronizationUrl, handler.getSynchronizationData().toString());
            JSONObject responseJSON;
            responseJSON = new JSONObject((String) response.get("response"));
            handler.processJSONResponse(responseJSON);
            Utils.logEvent(this.parent, Utils.EventCategory.SYNC, Utils.EventAction.START, String.format("length: %s, response code: %s, duration: %s, url: %s", response.get("length"), response.get("responseCode"), response.get("duration"), response.get("url")));
        } catch (Exception ex) {
            this.syncLocks.remove(id);
            ex.printStackTrace();
            Log.e(SynchronizationDaemon.this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(SynchronizationDaemon.synchronize)" : ex.getMessage());
        }
    }

}
