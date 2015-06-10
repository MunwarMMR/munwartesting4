package com.ii.mobile.tickle;

import java.util.Map.Entry;
import java.util.Set;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;

import com.ii.mobile.bus.BindService;
import com.ii.mobile.util.L;

public class TickleService extends BindService {

	private static boolean isRunning = false;

	public TickleService() {
		super();
		L.out("Started TickleService");
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);
	}

	public void sendMessage(ContentValues values) {
		// Message backMsg = Message.obtain();
		// backMsg.arg1 = result;
		Bundle bundle = new Bundle();
		Set<Entry<String, Object>> set = values.valueSet();
		for (Entry<String, Object> entry : set) {
			bundle.putString(entry.getKey(), (String) entry.getValue());
			// L.out("sending: " + entry.getKey() + " " + entry.getValue());
		}
		sendMessage(bundle);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		L.out("TickleService started");
		isRunning = true;
		Tickler.startTickler(this, getApplicationContext());
		// inMessenger = new Messenger(new IncomingHandler());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// L.out("Received start id " + startId + ": " + intent);
		return START_STICKY; // run until explicitly stopped.
	}

	public static boolean isRunning()
	{
		return isRunning;
	}

	@Override
	public void onDestroy() {
		L.out("onDestroy!");
		isRunning = false;
		super.onDestroy();
		// Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onStart(Intent intent, int startid) {
		L.out("onStart!");
	}
}
