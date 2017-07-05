package org.rootio.services;

import org.rootio.handset.R;
import org.rootio.tools.sms.MessageProcessor;
import org.rootio.tools.sms.SMSSwitch;
import org.rootio.tools.utils.Utils;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSService extends Service implements IncomingSMSNotifiable, ServiceInformationPublisher {

	private boolean isRunning;
	private final int serviceId = 2;
	private IncomingSMSReceiver incomingSMSReceiver;
	private boolean wasStoppedOnPurpose = true;

	@Override
	public IBinder onBind(Intent arg0) {
		BindingAgent bindingAgent = new BindingAgent(this);
		return bindingAgent;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		this.incomingSMSReceiver = new IncomingSMSReceiver(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startID) {
		Utils.doNotification(this, "RootIO", "SMS Service started");
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
		this.registerReceiver(this.incomingSMSReceiver, intentFilter);
		this.isRunning = true;
		this.sendEventBroadcast();
		return Service.START_STICKY;
	}

	@Override
	public void onTaskRemoved(Intent intent) {
		super.onTaskRemoved(intent);
		if (intent != null) {
			wasStoppedOnPurpose = intent.getBooleanExtra("wasStoppedOnPurpose", false);
			if (wasStoppedOnPurpose) {
				this.shutDownService();
			} else {
				this.onDestroy();
			}
		}
	}

	@Override
	public void onDestroy() {
		if (this.wasStoppedOnPurpose == false) {
			Intent intent = new Intent("org.rootio.services.restartServices");
			sendBroadcast(intent);
		} else {
			this.shutDownService();
		}
		super.onDestroy();
	}

	private void shutDownService() {
		if (this.isRunning) {
			this.isRunning = false;
			try {
				this.unregisterReceiver(this.incomingSMSReceiver);
			} catch (Exception ex) {
				Log.e(this.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer" : ex.getMessage());
			}
			this.sendEventBroadcast();
			Utils.doNotification(this, "RootIO", "SMS Service stopped");
		}
	}

	@Override
	public void notifyIncomingSMS(SmsMessage message) {
		SMSSwitch smsSwitch = new SMSSwitch(this, message);
		MessageProcessor messageProcessor = smsSwitch.getMessageProcessor();
		if (messageProcessor != null) {
			messageProcessor.ProcessMessage();
		}
	}

	@Override
	public boolean isRunning() {
		return this.isRunning;
	}

	/**
	 * Sends out broadcasts to listeners informing them of service status
	 * changes
	 */
	private void sendEventBroadcast() {
		Intent intent = new Intent();
		intent.putExtra("serviceId", this.serviceId);
		intent.putExtra("isRunning", this.isRunning);
		intent.setAction("org.rootio.services.sms.EVENT");
		this.sendBroadcast(intent);
	}

	@Override
	public int getServiceId() {
		return this.serviceId;
	}

}