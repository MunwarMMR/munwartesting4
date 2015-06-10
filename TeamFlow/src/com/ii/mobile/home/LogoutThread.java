package com.ii.mobile.home;

import android.graphics.Color;
import android.widget.Button;

import com.ii.mobile.soap.SoapDbAdapter;
import com.ii.mobile.tab.BreakActivity;
import com.ii.mobile.tickle.ActorController;
import com.ii.mobile.util.L;

public class LogoutThread implements Runnable {

	private final Button logoutButton;
	private final Thread thread;
	private final LoginActivity loginActivity;

	public static boolean notStopped = true;

	public LogoutThread(Button logoutButton, LoginActivity context) {
		L.out("LoginThread");
		this.logoutButton = logoutButton;
		this.thread = new Thread(this);
		this.loginActivity = context;
		notStopped = true;
		thread.start();
	}

	@Override
	public void run() {
		int firstTest = SoapDbAdapter.haveSomethingToUpload();
		boolean updateFail = ((firstTest != 0) ? true : false);
		if (ActorController.status == null)
			return;
		boolean taskFail = !ActorController.status.getEmployeeStatus().equals(BreakActivity.AVAILABLE);
		if (!updateFail && !taskFail)
			return;
		if (updateFail)
			MyToast.show("Unable to logout\nWaiting on " + L.getPlural(firstTest, " update"));
		else
			MyToast.show("Unable to logout if you have a task or on delay!");
		logoutButton.setEnabled(false);
		logoutButton.setTextColor(Color.parseColor("#777777"));

		boolean showReason = false;

		while (notStopped
				|| !ActorController.status.getEmployeeStatus().equals(BreakActivity.AVAILABLE)) {
			int uploads = SoapDbAdapter.haveSomethingToUpload();
			if (uploads == 0
					&& ActorController.status.getEmployeeStatus().equals(BreakActivity.AVAILABLE)) {
				notStopped = false;
				// MyToast.show("Logout is enabled!");
			}
			if (uploads == 0 && !showReason
					&& !ActorController.status.getEmployeeStatus().equals(BreakActivity.AVAILABLE)) {
				MyToast.show("Unable to logout if you have a task or on delay!");
				showReason = true;
			}
			L.sleep(1000);
		}
		updateButton();
	}

	private void updateButton() {
		loginActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				logoutButton.setEnabled(true);
				logoutButton.setTextColor(Color.parseColor("#ffffff"));

			}
		});
	}

}
