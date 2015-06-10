package com.ii.mobile.tab;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.TabHost;

import com.ii.mobile.R; // same package // same package
import com.ii.mobile.home.LoginActivity;
import com.ii.mobile.soap.NetKiller;
import com.ii.mobile.update.Updater;
import com.ii.mobile.users.User;
import com.ii.mobile.util.L;

public class TabNavigationActivity extends TabActivity implements OnGestureListener, OnDoubleTapListener {

	private static Title title;
	private GestureDetector detector;
	public static TabHost tabHost;
	private static TabNavigationActivity tabNavigationActivity = null;

	public static boolean showToasts = false;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.actor_options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.update:
			// MyToast.show("update");
			User.getUser().initUpdate();
			new Updater(this).checkForUpdate();
			return true;

		default:
		}
		return super.onOptionsItemSelected(item);
	}

	// private Tickled getTickled() {
	// if (tickled != null)
	// return tickled;
	// tickled = new Tickled(this);
	// return tickled;
	// }

	@Override
	protected void onResume() {
		super.onResume();
		L.out("onResume");

		if (User.getUser() == null || User.getUser().getValidateUser() == null) {
			finish();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		L.out("onPause");
		// LoginActivity.setStaleTime();
		// try {
		// unbindService(serviceConnection);
		// } catch (Exception e) {
		// L.out("unbind error: " + e + L.p());
		// }
		// UserWatcher.INSTANCE.start();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tabNavigationActivity = this;
		L.out("started");
		// isDemoVersion = getResources().getBoolean(R.bool.isDemoVersion);
		boolean customTitleSupported = true;

		customTitleSupported =
				requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.tab);

		// binder = new Binder(this, incomingMessenger, TickleService.class);
		detector = new GestureDetector(this, this);

		Resources res = getResources(); // Resource object to get Drawables
		tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Resusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab

		intent = getIntent();
		// L.out("called intent: " + intent + " " + intent.getData());
		Uri save = intent.getData();

		title = new Title(this, customTitleSupported);
		updateTitle();
		// Create an Intent to launch an Activity for the tab (to be reused)
		// intent = new Intent().setClass(this, ListActivity.class);
		// Initialize a TabSpec for each tab and add it to the TabHost
		intent = new Intent().setClass(this, TaskActivity.class);
		intent.setData(save);
		spec = tabHost
				.newTabSpec("task")
				.setIndicator("Task",
						res.getDrawable(R.drawable.ic_tab_task))
				.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, BreakActivity.class);
		intent.setData(save);

		spec = tabHost
				.newTabSpec("break")
				.setIndicator("Break",
						res.getDrawable(R.drawable.ic_tab_status))
				.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, IMActivity.class);
		intent.setData(save);

		// intent.putExtra(WorkOrderColumns.WORKORDERID, workOrderId);
		spec = tabHost
				.newTabSpec("im")
				.setIndicator("IM", res.getDrawable(R.drawable.ic_tab_im))
				.setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(1);
		tabHost.setCurrentTab(2);
		if (save == null) {
			tabHost.setCurrentTab(0);
		} else {
			tabHost.setCurrentTab(0);
		}
		// String type = getType(save);
		// tabHost.setCurrentTab(2);

		// checkIfServiceIsRunning();
		// binder = new Binder(this, incomingMessenger, TickleService.class);
		// new StaticLoader(this).execute();

	}

	public static void updateTitle() {
		if (title == null) {
			// L.out("*** ERROR unable to update title!");
			return;
		}
		tabNavigationActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				title.update();
			}
		});

	}

	// @Override
	// public boolean onKeyUp(int keyCode, KeyEvent event) {
	// switch (keyCode) {
	// case KeyEvent.KEYCODE_MENU:
	// MyToast.show("Menu key released");
	// return true;
	// case KeyEvent.KEYCODE_SEARCH:
	// MyToast.show("Search key released");
	// return true;
	// case KeyEvent.KEYCODE_VOLUME_UP:
	// if (event.isTracking() && !event.isCanceled())
	// MyToast.show("volume_up key released");
	// return true;
	// case KeyEvent.KEYCODE_VOLUME_DOWN:
	// MyToast.show("volume_up key down");
	// return true;
	// }
	// return super.onKeyDown(keyCode, event);
	// }
	//
	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// switch (keyCode) {
	// case KeyEvent.KEYCODE_MENU:
	// MyToast.show("Menu key pressed");
	// return true;
	// case KeyEvent.KEYCODE_SEARCH:
	// MyToast.show("Search key pressed");
	// return true;
	// case KeyEvent.KEYCODE_BACK:
	// onBackPressed();
	// return true;
	// case KeyEvent.KEYCODE_VOLUME_UP:
	// event.startTracking();
	// MyToast.show("volume_up key pressed");
	// return true;
	// case KeyEvent.KEYCODE_VOLUME_DOWN:
	// MyToast.show("volume_down key pressed");
	// return false;
	// }
	// return super.onKeyDown(keyCode, event);
	// }

	@Override
	public boolean onTouchEvent(MotionEvent me) {
		detector.onTouchEvent(me);
		return super.onTouchEvent(me);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// L.out("---onDown----" + e.toString());
		return false;
	}

	@Override
	@SuppressWarnings("unused")
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (true)
			return false;
		L.out("---onFling---" + e1.toString() + e2.toString());

		int index = tabHost.getCurrentTab();
		if (velocityX < 0.0f) {
			index -= 1;
		} else
			index += 1;
		L.out("index: " + index + " " + tabHost.getChildCount());
		index = Math.max(index, 0);
		index = Math.min(index, 5 - 1);
		L.out("fling: " + velocityX + " current: " + tabHost.getCurrentTab() + " new: " + index);
		tabHost.setCurrentTab(index);
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		SharedPreferences settings = getSharedPreferences(User.PREFERENCE_FILE, 0);
		boolean staffUser = settings.getBoolean(LoginActivity.STAFF_USER, false);
		if (!staffUser)
			return;
		L.out("---onLongPress---" + e.toString());
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(100);
		NetKiller.setKiller();
		// int fred = 100 / 0;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// L.out("---onScroll---" + e1.toString() + e2.toString());
		// int index = (int) ((e2.getRawX() / 800.0f) * 5.0f);
		// L.out("index: " + index + " " + e2.getRawX());
		// tabHost.setCurrentTab(index);
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// L.out("---onShowPress---" + e.toString());
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// L.out("---onSingleTapUp---" + e.toString());
		return false;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		L.out("---onDoubleTap---" + e.toString());
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		L.out("---onDoubleTapEvent---" + e.toString());
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		L.out("---onSingleTapConfirmed---" + e.toString());
		return false;
	}

	public static TabNavigationActivity getTabNavigationActivity() {
		return tabNavigationActivity;
	}

	// private final ServiceConnection serviceConnection = new
	// ServiceConnection() {
	//
	// public void onServiceConnected(ComponentName className, IBinder binder) {
	// // MyToast.show("connected");
	// messenger = new Messenger(binder);
	// // onClickTest();
	// }
	//
	// public void onServiceDisconnected(ComponentName className) {
	// MyToast.show("disconnected");
	// messenger = null;
	// }
	// };
	//
	// private void checkIfServiceIsRunning() {
	// // If the service is running when the activity starts, we want to
	// // automatically bind to it.
	// if (TickleService.isRunning()) {
	// doBindService();
	// }
	// }
	//
	// void doBindService() {
	// bindService(new Intent(this, TickleService.class), serviceConnection,
	// Context.BIND_AUTO_CREATE);
	// }
	//
	// void doUnbindService() {
	// if (serviceIsBound) {
	// // Detach our existing connection.
	// unbindService(serviceConnection);
	// serviceIsBound = false;
	// L.out("Unbinding.");
	// }
	// }
	// private final Handler incomingHandler = new Handler() {
	// @Override
	// public void handleMessage(Message message) {
	// // L.out("message: " + message);
	// Bundle data = message.getData();
	// // L.out("data: " + data + "message.arg1 " + message.arg1 + " ");
	// if (data != null) {
	// getTickled().checkTickle(data);
	// }
	// }
	// };
	//
	// final Messenger incomingMessenger = new Messenger(incomingHandler);

	@Override
	public void onDestroy() {
		L.out("onDestroy");
		// binder.onDestroy();
		title.onDestroy();
		super.onDestroy();
	}
}
