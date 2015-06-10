package com.ii.mobile.tab;

import android.media.MediaPlayer;
import android.os.Bundle;

import com.ii.mobile.R; // same package // same package
import com.ii.mobile.home.LoginActivity;

public class Tickled {

	private final LoginActivity activity;

	private MediaPlayer mp = null;

	public Tickled(LoginActivity activity) {
		this.activity = activity;

	}

	public void checkTickle(Bundle data) {
		// printTickle(data);
		// playSound(0);
		// flip for MVC
		// TaskActivity.getTaskActivity().tickled(data);
		// SoapDbAdapter.getSoapDbAdapter().tickled(data);
	}

	// @SuppressWarnings("unused")
	// private void printTickle(Bundle data) {
	// String temp = "Tickled ";
	// if (data.getString(Tickler.TASK_NUMBER) != null)
	// temp += Tickler.TASK_NUMBER + ": " + data.getString(Tickler.TASK_NUMBER);
	// if (data.getString(Tickler.TASK_STATUS) != null)
	// temp += " " + Tickler.TASK_STATUS + ": " +
	// data.getString(Tickler.TASK_STATUS);
	// MyToast.show(temp);
	// }

	/**
	 * Used to play IM notification and alert sounds that are locally stored on
	 * the device.
	 * 
	 * @param sFile
	 */
	public void playSound(int sFile) {
		// set up MediaPlayer

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				mp = MediaPlayer.create(activity.getApplicationContext(), R.raw.notify);
				mp.start();
			}
		});
		thread.start();
	}
}
