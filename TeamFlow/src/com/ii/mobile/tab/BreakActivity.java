/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ii.mobile.tab;

import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.View;
import android.widget.TextView;

import com.ii.mobile.R; // same package // same package
import com.ii.mobile.home.LoginActivity;
import com.ii.mobile.home.MyToast;
import com.ii.mobile.soap.NetKiller;
import com.ii.mobile.tickle.ActorController;
import com.ii.mobile.users.User;
import com.ii.mobile.util.L;

/**
 * 
 * @author kfairchild
 */
public class BreakActivity extends Activity {

	static CountDownTimer waitTimer;
	static long startTime = 0;

	static int muteVolume = 0;
	static boolean muted = false;

	public static String AVAILABLE = "Available";
	public static String ACTIVE = "Active";
	public static String ASSIGNED = "Assigned";
	// public static String ACTIVE = "Active";
	public static String DELAYED = "Delayed";
	public static String AT_LUNCH = "At Lunch";
	public static String ON_BREAK = "On Break";
	public static String NOT_IN = "Not In";

	// String resumeBreakType = AVAILABLE;

	public static StatusType[] statusTypes = new StatusType[] {
			new StatusType(AVAILABLE, "1"),
			new StatusType(AT_LUNCH, "5"),
			new StatusType(ON_BREAK, "6"),
			new StatusType(NOT_IN, "7"),
	};

	int count = 0;
	long elapsedTime = 0;
	private Vibrator vibrator;
	private static AudioManager mAudioManager;
	public static BreakActivity breakActivity = null;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		breakActivity = this;
		L.out("create: " + getEmployeeStatus());
		// String currentStatus = getEmployeeStatus();
		L.out("test lookup: " + StatusType.lookUp(AT_LUNCH));
		updateStatus(getEmployeeStatus(), true);
		setContentView(R.layout.take_break);
		L.out("mAudioManager: " + mAudioManager);
		if (mAudioManager == null)
			mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		addLongClick();
		addLongClick2();
	}

	private void addLongClick() {
		TextView textView = (TextView) findViewById(R.id.background);

		// MyToast.show("Airplane Mode long click is enabled");
		textView.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View view) {
				vibrator.vibrate(100);
				SharedPreferences settings = getSharedPreferences(User.PREFERENCE_FILE, 0);
				boolean staffUser = settings.getBoolean(LoginActivity.STAFF_USER, false);
				if (!staffUser)
					return false;
				NetKiller.setKiller();
				return true;
				// SharedPreferences settings =
				// getSharedPreferences(User.PREFERENCE_FILE, 0);
				// boolean staffUser =
				// settings.getBoolean(LoginActivity.STAFF_USER, false);
				// if (!staffUser)
				// return false;
				//
				// return true;
			}
		});
	}

	private void addLongClick2() {
		TextView textView = (TextView) findViewById(R.id.lunchBreakText);

		// MyToast.show("Airplane Mode long click is enabled");
		textView.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View view) {
				vibrator.vibrate(100);
				SharedPreferences settings = getSharedPreferences(User.PREFERENCE_FILE, 0);
				boolean staffUser = settings.getBoolean(LoginActivity.STAFF_USER, false);
				if (!staffUser)
					return false;
				NetKiller.setKiller();
				return true;

			}
		});
	}

	private String getEmployeeStatus() {
		if (User.getUser().getValidateUser() == null) {
			// L.out("validateUser is null!");
			stopTimer();
			return null;
		}
		if (ActorController.status == null)
			return null;
		return ActorController.status.getEmployeeStatus();
	}

	@Override
	protected void onResume() {
		super.onResume();
		L.out("resume: " + getEmployeeStatus());
		updateStatus(getEmployeeStatus(), true);
		updateMuteButton();
	}

	@Override
	protected void onPause() {
		super.onPause();
		L.out("pause: " + getEmployeeStatus());
		// stopTimer();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		outState.putLong("startTime", startTime);
		outState.putBoolean("muted", muted);
		L.out("onSave: " + startTime + " " + getEmployeeStatus() + " muted: " + muted);
	}

	@Override
	protected void onRestoreInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		startTime = outState.getLong("startTime");
		muted = outState.getBoolean("muted");
		L.out("onRestore: " + " muted: " + muted);
		updateStatus(getEmployeeStatus(), true);
		// if (getEmployeeStatus() != null) {
		// takeBreak(getEmployeeStatus());
		// }
	}

	public void playAudioButtonClick(View view)
	{
		// new AudioPlayer(this).playSound(AudioPlayer.ERROR);
		AudioPlayer.INSTANCE.playSound(AudioPlayer.ERROR);
		// AudioManager mAudioManager = (AudioManager)
		// getSystemService(Context.AUDIO_SERVICE);
		float maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
		float currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
		int percent = (int) ((currentVolume / maxVolume) * 100);

		MyToast.show("Volume is " + percent + " percent" + " and " + (!muted ? "not muted" : "muted"));
	}

	public void volumeButtonClick(View view)
	{
		muteButton();
	}

	synchronized private void muteButton() {
		float maxRingVolume =
				mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
		muteVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
		// TextView button = (TextView) findViewById(R.id.volumeText);

		if (muted) {
			mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
			int percent = (int) ((muteVolume / maxRingVolume) * 100);
			// button.setText("Mute Volume");
			MyToast.show("Un-muting and setting Volume to " + percent + " percent");
		} else {
			// button.setText("Un-Mute Volume");

			mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
			int percent = (int) ((muteVolume / maxRingVolume) * 100);
			// mAudioManager.setStreamVolume(AudioManager.STREAM_RING, 0,
			// AudioManager.FLAG_VIBRATE);
			MyToast.show("Muting volume of " + percent + " percent");
		}
		muted = !muted;
		updateMuteButton();
	}

	private void updateMuteButton() {
		TextView button = (TextView) findViewById(R.id.volumeText);
		if (!muted)
			button.setText("Mute Volume");
		else
			button.setText("Un-Mute Volume");
	}

	public void lunchBreakButtonClick(View view)
	{
		if (ActorController.task == null) {
			User.getUser().setWantBreak(true);
			updateStatus(AT_LUNCH, false);
		}

		else {
			AudioPlayer.INSTANCE.playSound(AudioPlayer.ERROR);
			// new AudioPlayer(this).playSound(AudioPlayer.ERROR);
			MyToast.show("Unable to take lunch break\nwith an active task!");
		}
	}

	public void shortBreakButtonClick(View view)
	{
		if (ActorController.task == null) {
			User.getUser().setWantBreak(true);
			updateStatus(ON_BREAK, false);
		}
		else {
			AudioPlayer.INSTANCE.playSound(AudioPlayer.ERROR);
			// new AudioPlayer(this).playSound(AudioPlayer.ERROR);
			MyToast.show("Unable to go on break\nwith an active task!");
		}
	}

	public void showBreak(String breakType) {

		TextView textview = (TextView) findViewById(R.id.title);
		textview.setText(breakType);
		// updateStatus(breakType);
		// startTimer();
	}

	public void startTimer() {
		L.out("startTimer called!");
		if (startTime == 0)
			startTime = new GregorianCalendar().getTimeInMillis();
		if (waitTimer != null) {
			L.out("killing waitTimer!");
			waitTimer.cancel();
			waitTimer = null;
		}
		waitTimer = new CountDownTimer(99999999, 900) {

			@Override
			public void onTick(long millisUntilFinished) {
				// called every 300 milliseconds, which could be used to
				// send messages or some other action

				TextView textView = (TextView) findViewById(R.id.breakTimer);
				if (startTime != 0)
					elapsedTime = new GregorianCalendar().getTimeInMillis() - startTime;
				count = count + 1;
				if (textView != null) {
					String title = "Time On Break: ";
					if (getEmployeeStatus() != null && getEmployeeStatus().equals(DELAYED))
						title = "Delayed on Task: ";
					textView.setText(title + L.getElapsedTime(elapsedTime));
				}

				else {
					// L.out("*** error: " + count);
				}
				// L.out("count: " + count);
			}

			@Override
			public void onFinish() {
				// if you would like to execute something when time finishes
			}
		}.start();
	}

	private void stopTimer() {
		// L.out("stopTimer waitTimer: " + waitTimer);
		if (waitTimer != null) {
			waitTimer.cancel();
			waitTimer = null;
		}
		startTime = 0l;
	}

	public void finishBreakButtonClick(View view) {
		if (ActorController.task == null) {

			updateStatus(AVAILABLE, false);

		}
		else {
			AudioPlayer.INSTANCE.playSound(AudioPlayer.ERROR);
			// new AudioPlayer(this).playSound(AudioPlayer.ERROR);
			MyToast.show("Finish the delay using the task screen!");
		}

	}

	// public void updateStatus(String breakType) {
	// updateStatus(breakType, true);
	// }

	public void update() {
		// L.out("ActorController.status: " + ActorController.status);
		if (ActorController.status == null || ActorController.status.getEmployeeStatus() == null)
			return;
		String breakType = ActorController.status.getEmployeeStatus();
		L.out("breakType: " + breakType);
		if (breakType == null)
			return;
		if (breakType.equals(AVAILABLE) || breakType.equals(ACTIVE) || breakType.equals(ASSIGNED)) {
			setContentView(R.layout.take_break);
			stopTimer();
		} else {
			L.out("on_break: " + breakType);
			setContentView(R.layout.on_break);
			startTimer();
			showBreak(breakType);
		}
		updateMuteButton();

		TabNavigationActivity.updateTitle();
	}

	public void updateStatus(String breakType, boolean displayOnly) {
		if (breakType == null)
			return;
		if (breakType.equals(AVAILABLE) || breakType.equals(ACTIVE) || breakType.equals(ASSIGNED)) {
			setContentView(R.layout.take_break);
			stopTimer();
		} else {
			setContentView(R.layout.on_break);
			startTimer();
			showBreak(breakType);
		}
		updateMuteButton();
		if (!displayOnly)
			TaskActivity.setEmployeeStatus(breakType, false, this);
		TabNavigationActivity.updateTitle();
	}
}
