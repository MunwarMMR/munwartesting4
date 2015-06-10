package com.ii.mobile.home;

import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ii.mobile.R; // same package // same package
import com.ii.mobile.tab.BreakActivity;
import com.ii.mobile.tickle.ActorController;
import com.ii.mobile.users.User;
import com.ii.mobile.util.L;

// other useful methods here

public enum UserWatcher {
	INSTANCE;

	// private final int SLEEP_TIME = 3000;
	// private final int TIMEOUT_TIME = 60 * 60;
	// private final int PROMPT_TIME = 30;
	private boolean running = false;

	private LoginActivity loginActivity = null;

	private class UpdateTask extends AsyncTask<Boolean, Integer, Long> {
		boolean finish;

		@Override
		protected Long doInBackground(Boolean... flags) {
			Thread.currentThread().setName("UserWatcherUpdateThread");
			finish = flags[0];
			return 0l;
		}

		@Override
		protected void onPostExecute(Long result) {
			L.out("start");
			doUpdate(finish);
			L.out("finished");
		}
	}

	public void login(LoginActivity loginActivity) {
		this.loginActivity = loginActivity;
	}

	public void start() {
		// ValidateUser validateUser = User.getUser().getValidateUser();
		if (ActorController.status == null || ActorController.status.getEmployeeStatus() == null)
			return;
		L.out("start: " + running + " "
				+ !(ActorController.status.getEmployeeStatus().equals(BreakActivity.AVAILABLE)));
		L.out("update: " + ActorController.status.getEmployeeStatus());
		if (!running) {
			if (!(ActorController.status.getEmployeeStatus().equals(BreakActivity.AVAILABLE))
					&& !User.getUser().getNeedLogin())
				return;
			// WatcherTask watcherTask = new WatcherTask();
			// watcherTask.execute();
		}
		update(false);
	}

	void update(boolean finish) {
		new UpdateTask().execute(finish);
	}

	public synchronized void doUpdate(boolean finish) {
		// L.out("update: " + finish + " " + User.getUser().getNeedLogout() +
		// L.p(20, "watcher"));
		if (loginActivity == null) {
			L.out("no login activity");
			return;
		}
		User user = User.getUser();
		L.out("update: " + user);
		if (finish) {
			((EditText) loginActivity.findViewById(R.id.txtPassword)).setEnabled(true);
			((EditText) loginActivity.findViewById(R.id.txtUsername)).setEnabled(true);
			((EditText) loginActivity.findViewById(R.id.txtPassword)).setText("");
		}
		// String password = ((EditText)
		// loginActivity.findViewById(R.id.txtPassword)).getText().toString();

		// L.out("update: " + user.getValidateUser().getEmployeeStatus());
		// ValidateUser validateUser = user.getValidateUser();
		// if (validateUser != null)
		if (ActorController.status != null) {
			L.out("ActorController.status.getEmployeeStatus(): " + ActorController.status.getEmployeeStatus());

		}
		if (user.getValidateUser() == null
				|| (user.getNeedLogout()
						|| ActorController.status == null
						|| (ActorController.status.getEmployeeStatus().equals(BreakActivity.NOT_IN)))) {
			// L.out("in: " + ActorController.status);
			Button button = (Button) loginActivity.findViewById(R.id.btnLogin);
			if (button == null) {
				L.out("not btnLogin!");
				return;
			}
			// L.out("in: " + ActorController.status);
			((Button) loginActivity.findViewById(R.id.btnLogin)).setText("Log In");
			((Button) loginActivity.findViewById(R.id.buttonEnter)).setVisibility(View.GONE);
		} else {
			((Button) loginActivity.findViewById(R.id.btnLogin)).setText("Log Out");
			((Button) loginActivity.findViewById(R.id.buttonEnter)).setVisibility(View.VISIBLE);
		}
	}

	public void stop() {
		// L.out("stop: " + running);
		running = false;
		// if (backGroundTask != null)
		// backGroundTask.cancel(true);
	}
};
