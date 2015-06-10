/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ii.mobile.tab;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.Html;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ii.mobile.R;
import com.ii.mobile.home.LoginActivity;
import com.ii.mobile.soap.Soap;
import com.ii.mobile.soap.gson.GetEmployeeAndTaskStatusByEmployeeID;
import com.ii.mobile.soap.gson.Logger;
import com.ii.mobile.task.selfTask.SelfTaskActivity;
import com.ii.mobile.tickle.ActorController;
import com.ii.mobile.tickle.Tickler;
import com.ii.mobile.users.User;
import com.ii.mobile.util.L;
import com.ii.mobile.util.PrettyPrint;

public class IMActivity extends Activity {
	private Vibrator vibrator;
	private static IMActivity imActivity;
	private static final String MESSAGES = "messages";
	public TextView textView;
	private ScrollView scrollView;
	public static String loginMessage = "";
	boolean last = false;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		L.out("creating IMActivity");
		IMActivity.imActivity = this;
		setContentView(R.layout.instant_message_view);
		textView = (TextView) findViewById(R.id.chatWindow);
		scrollView = (ScrollView) findViewById(R.id.messageScrollView);
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		addLongClick();
		addLogLongClick();

		IntentFilter intentFilter = new IntentFilter("android.intent.action.SERVICE_STATE");

		BroadcastReceiver receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				boolean onOff = isAirplaneModeOn(context);
				if (last != onOff)
					if (true) {
						// MyToast.show("AirplaneMode is " + (onOff ? "on" :
						// "off"));
						IMActivity.p("Airplane Mode is " + (onOff ? "on" : "off") + "\n");
					}
				last = onOff;
			}
		};
		registerReceiver(receiver, intentFilter);
		// L.out("icicle onRestoreInstanceState: " + textView.getText() + " " +
		// loginMessage);
		// L.out("icicle: " + ((icicle != null) ? "yes" : "no"));
		if (icicle != null) {
			String messages = icicle.getString(MESSAGES);
			// L.out("messages: " + messages);
			messages += loginMessage;
			TextView textView = (TextView) findViewById(R.id.chatWindow);
			textView.setText(messages);
		} else {
			TextView textView = (TextView) findViewById(R.id.chatWindow);
			textView.setText(loginMessage);
		}
		scrollView.fullScroll(ScrollView.FOCUS_DOWN);
		// loginMessage = "";
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		// L.out("onSaveInstanceState: " + textView.getText());
		TextView textView = (TextView) findViewById(R.id.chatWindow);
		outState.putString(MESSAGES, textView.getText().toString());
	}

	@Override
	protected void onRestoreInstanceState(Bundle outState)
	{

		super.onRestoreInstanceState(outState);
		// L.out("onRestoreInstanceState: " + textView.getText() + " " +
		// loginMessage);
		// String messages = outState.getString(MESSAGES);
		// L.out("messages: " + messages);
		// TextView textView = (TextView) findViewById(R.id.chatWindow);
		// messages += loginMessage;
		// textView.setText(messages);
		// loginMessage = "";
	}

	private void addLongClick() {
		TextView textView = (TextView) findViewById(R.id.chatWindow);

		// MyToast.show("Airplane Mode long click is enabled");
		textView.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View view) {

				SharedPreferences settings = getSharedPreferences(User.PREFERENCE_FILE, 0);
				boolean staffUser = settings.getBoolean(LoginActivity.STAFF_USER, false);
				if (!staffUser)
					return false;
				vibrator.vibrate(100);
				sayClick(view);
				return true;
			}
		});

	}

	private void addLogLongClick() {
		TextView textView = (TextView) findViewById(R.id.logWindow);

		// MyToast.show("Airplane Mode long click is enabled");
		textView.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View view) {
				vibrator.vibrate(100);
				sayLogClick();
				return true;
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		L.out("onResume");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		L.out("onDestroy");
		imActivity = null;
	}

	private static boolean isAirplaneModeOn(Context context) {
		// Toggle airplane mode.

		return Settings.System.getInt(context.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, 0) != 0;
	}

	void output(final String string) {

		final TextView textView = (TextView) findViewById(R.id.chatWindow);
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				textView.setText(textView.getText() + string);
				scrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}

	public static void o(String string) {
		if (!TabNavigationActivity.showToasts) {
			L.out(string);
			return;
		}
		if (imActivity != null) {
			imActivity.output(string);
		}
		else
			L.out(string);
	}

	public static void p(String string) {
		if (imActivity != null && imActivity.isStaffUser()) {
			loginMessage += string;
			imActivity.output(string);
			L.out(string);
		}
		else
			L.out(string);
	}

	public boolean isStaffUser() {
		SharedPreferences settings = getSharedPreferences(User.PREFERENCE_FILE, 0);
		return settings.getBoolean(LoginActivity.STAFF_USER, false);
	}

	void sayClick(View view) {
		TextView textView = (TextView) findViewById(R.id.chatWindow);
		if (ActorController.status != null)
			textView.setText("\nApp Status: \n"
					+ ActorController.status.toStringShort()
					+ "\n\n");
		GetEmployeeAndTaskStatusByEmployeeID taskStatus =
				Soap.getSoap().getEmployeeAndTaskStatusByEmployeeID(User.getUser().getValidateUser().getEmployeeID());

		if (taskStatus != null) {
			textView.setText(textView.getText() + "Full Legacy Status: " + taskStatus.toString());
			textView.setText(textView.getText() + " \n\nLegacy Status: \n" + taskStatus.toStringShort()
					+ "\n");
		}
		else
			textView.setText(textView.getText() + "\n \nLegacy Status: " + "unavailable\n");
	}

	void sayLogClick() {
		TextView textView = (TextView) findViewById(R.id.chatWindow);
		if (ActorController.status != null)
			textView.setText("\nLogging: \n");
		Logger logger = new Logger(ActorController.status);
		logger.json = null;
		// L.out("logger: " + logger);
		// L.out("logger: " + logger.getNewJson());
		String tmp = PrettyPrint.formatPrint(logger.getNewJson());
		L.out("tmp: " + tmp);
		textView.setText(Html.fromHtml(textView.getText() + tmp));
	}

	void turnNetworkOnOff(boolean flag) {
		// MyToast.show("network: " + flag);
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(flag);
	}

	public static IMActivity getIMActivity() {
		return imActivity;
	}

	public static void receivedMessage(Bundle data) {
		if (imActivity == null) {
			L.out("***  No IMActivity to receive message");
			loginMessage += getStringFromMessage(data);
			L.out("loginMessage: " + loginMessage);
			AudioPlayer.INSTANCE.playSound(AudioPlayer.NEW_MESSAGE);
			return;
		}
		imActivity.addMessage(data);
		if (!SelfTaskActivity.isVisible()) {
			TabNavigationActivity.tabHost.setCurrentTab(2);
		}
	}

	private void addMessage(Bundle data) {
		String message = data.getString(Tickler.TEXT_MESSAGE);
		// L.out("addMessage: " + message);
		String receivedDate = data.getString(Tickler.RECEIVED_DATE);
		// L.out("received Date: " + receivedDate);
		String fromUserName = data.getString(Tickler.FROM_USER_NAME);

		String tmp = parseDate(receivedDate) + " " + fromUserName + ": "
				+ message + "\n ";
		TextView textView = (TextView) findViewById(R.id.chatWindow);
		textView.setText(textView.getText() + tmp);
		loginMessage += tmp;
		AudioPlayer.INSTANCE.playSound(AudioPlayer.NEW_MESSAGE);

		runOnUiThread(new Runnable() {

			@Override
			public void run() {

				scrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}

	private static String getStringFromMessage(Bundle data) {
		String message = data.getString(Tickler.TEXT_MESSAGE);
		// L.out("addMessage: " + message);
		String receivedDate = data.getString(Tickler.RECEIVED_DATE);
		// L.out("received Date: " + receivedDate);
		String fromUserName = data.getString(Tickler.FROM_USER_NAME);

		String tmp = "\n " + parseDate(receivedDate) + " " + fromUserName + ": "
				+ message;
		return tmp;
	}

	private static String parseDate(String receivedDate) {
		if (receivedDate == null)
			return "";
		Long temp = L.getLong(receivedDate);
		if (temp != 0)
			return L.toDateSecond(temp);
		int index = receivedDate.indexOf("T");
		if (index == -1)
			return receivedDate;
		int jindex = receivedDate.indexOf(".", index);
		if (jindex == -1)
			return receivedDate;
		return receivedDate.substring(index + 1, jindex);
	}
}
