package com.ii.mobile.home;

<<<<<<< HEAD
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
=======
>>>>>>> 26fe81448f9b13a55150e4f6c6f2ccca714ad58b
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
<<<<<<< HEAD
import android.os.AsyncTask;
=======
>>>>>>> 26fe81448f9b13a55150e4f6c6f2ccca714ad58b
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.crittercism.app.Crittercism;
<<<<<<< HEAD
import com.ii.mobile.R;
=======
import com.ii.mobile.R; // same package // same package
>>>>>>> 26fe81448f9b13a55150e4f6c6f2ccca714ad58b
import com.ii.mobile.alarm.Alarm;
import com.ii.mobile.application.ApplicationContext;
import com.ii.mobile.application.Shortcut;
import com.ii.mobile.block.blocker.BlockService;
import com.ii.mobile.bus.Binder;
import com.ii.mobile.monitor.UnitTestActivity;
import com.ii.mobile.service.NotifyService;
import com.ii.mobile.soap.ParsingSoap;
import com.ii.mobile.soap.SoapDbAdapter;
import com.ii.mobile.soap.StaticSoap;
import com.ii.mobile.soap.StaticSoap.StaticSoapColumns;
import com.ii.mobile.soap.gson.ValidateUser;
import com.ii.mobile.tab.BreakActivity;
import com.ii.mobile.tab.PickField;
import com.ii.mobile.tab.TabNavigationActivity;
import com.ii.mobile.tab.TaskActivity;
import com.ii.mobile.tab.Tickled;
import com.ii.mobile.task.TaskSoap.TaskSoapColumns;
import com.ii.mobile.task.selfTask.SelfTaskActivity;
import com.ii.mobile.tickle.ActorController;
import com.ii.mobile.tickle.TickleService;
import com.ii.mobile.tickle.Tickler;
import com.ii.mobile.update.Updater;
import com.ii.mobile.users.User;
import com.ii.mobile.util.L;

public class LoginActivity extends Activity implements TextWatcher, OnEditorActionListener, LoginCallBack,
		StaticLoaderCallBack {

	protected EditText txtUsername;
	protected EditText txtPassword;
	public Button loginButton;
	protected User user = null;
	protected Vibrator vibrator;
	protected String platform = null;
	public static String STAFF_USER = "staffUser";
	public static long loginTime = 0l;
	protected boolean addedLongClick = false;

	protected static String lastUser = "", currentUser = "";

	public static LoginActivity loginActivity = null;
	SharedPreferences preferences;

	// static private boolean startedIntentBlocker = true;

	OnClickListener onClickListener;

	protected void initCritter() {
		// new Updater(this).checkForUpdate();
		Resources resources = getResources();
		boolean isProduction = resources.getBoolean(R.bool.isProduction);
		boolean wantCrashReport = resources.getBoolean(R.bool.wantCrashReport);
		boolean isTestVersion = resources.getBoolean(R.bool.isTestVersion);
		if (wantCrashReport)
		{
			String appId;
			if (isProduction)
				appId = resources.getString(R.string.prodId);
			else if (isTestVersion)
				appId = resources.getString(R.string.testId);
			else
				appId = resources.getString(R.string.devId);
			Crittercism.initialize(this, appId);
			Crittercism.setUsername("No Login");
		}
	}

	public boolean isStaffUser() {
		SharedPreferences settings = getSharedPreferences(User.PREFERENCE_FILE, 0);
		return settings.getBoolean(STAFF_USER, false);
	}

	public boolean wantMAM() {
		return getResources().getBoolean(R.bool.wantMAM);
	}

	// public static void receivedMessage(Bundle data) {
	// if (loginActivity == null) {
	// L.out("*** ERROR No IMActivity to receive message");
	// return;
	// }
	// loginActivity.addMessage(data);
	// }
	//
	// private String parseDate(String receivedDate) {
	// if (receivedDate == null)
	// return "";
	// Long temp = L.getLong(receivedDate);
	// if (temp != 0)
	// return L.toDateSecond(temp);
	// int index = receivedDate.indexOf("T");
	// if (index == -1)
	// return receivedDate;
	// int jindex = receivedDate.indexOf(".", index);
	// if (jindex == -1)
	// return receivedDate;
	// return receivedDate.substring(index + 1, jindex);
	// }

	// private void addMessage(Bundle data) {
	//
	// }

	public static Binder binder = null;

	private final Handler incomingHandler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			// L.out("message: " + message);
			Bundle data = message.getData();
			// L.out("data: " + data + "message.arg1 " + message.arg1 + " ");
			if (data != null) {
				getTickled().checkTickle(data);
			}
		}
	};

	private Tickled tickled = null;

	private Tickled getTickled() {
		if (tickled != null)
			return tickled;
		tickled = new Tickled(this);
		return tickled;
	}

	final Messenger incomingMessenger = new Messenger(incomingHandler);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loginActivity = this;
		preferences = getSharedPreferences("user123", MODE_PRIVATE);
		lastUser = preferences.getString("lastUser", "first");
		SharedPreferences prefs = getSharedPreferences(User.PREFERENCE_FILE, MODE_PRIVATE);
		boolean isProduction = getResources().getBoolean(R.bool.isProduction);
		if (isProduction)
			L.setDebug(false);
		else
			L.setDebug(true);

		binder = new Binder(this, incomingMessenger, TickleService.class);

		// testFacilities();
		initCritter();
		if (!prefs.getBoolean("shortcutKey", false))
		{
			prefs.edit().putBoolean("shortcutKey", true).commit();
			Shortcut.INSTANCE.createShortCut(this);
		}
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		L.out("LoginActivity Started: " + getClass().getSimpleName());

		setContentView(getLayout());
		userWatcherLogin();
		createGUI();

		MyToast.make(this);

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		L.out("Starting Services");
		startService(new Intent(this, getNotificationService(this)));
		if (wantMAM()) {

			if (!isStaffUser())
				startService(new Intent(this, BlockService.class));
			// startService(new Intent(this, ProximityService.class));
			// startService(new Intent(this, TickleService.class));

			// startAlarm();
			// new Alarm().resetAlarm(this);
			// L.out("startService");
			// AntiSync.INSTANCE.stopSync(this);

		}
		// WakeLocker.INSTANCE.start(this);
		if (User.getUser().getValidateUser() != null) {
			vibrator.vibrate(200);
			User.getUser().setNeedLogin(true);
			User.getUser().setNeedLogout(false);
			LogoutThread.notStopped = false;
			Tickler.onResume();
			Intent intent = new Intent().setClass(LoginActivity.this, getTopLevelClass());
			startActivity(intent);
		}
	}

	protected void startAlarm() {
		new Alarm().resetAlarm(this);
	}

	public int getLayout() {
		return R.layout.ii_login;
	}

	// @SuppressWarnings("unused")
	// private void testFacilities() {
	// String bay = "73916";
	// String alaska = "120712";
	// String test = "20867";
	//
	// String value =
	// "value to encrypt ting 123ksjiajwoi  fhiudhwuhwiuhwiuwhiwu";
	// L.out(" value: " + value + " " + value.length());
	// String pw = "xyzzy123";
	// try {
	// String result = SecurityUtils.encryptAES(pw, value);
	// L.out("result: " + result + " " + result.length());
	// String tinged = SecurityUtils.decryptAES(pw, result);
	// L.out("tinged: " + tinged + " " + tinged.length());
	//
	// } catch (Exception e) {
	// L.out("encryption error: " + e + L.p());
	// }
	// }

	// @SuppressWarnings("unused")
	// private void testFacility(String facilityID) {
	// Soap.setPlatform(getString(R.string.default_platform));
	// GetTaskDefinitionFieldsForScreenByFacilityID foo =
	// Soap.getSoap().getTaskDefinitionFieldsForScreenByFacilityID(facilityID);
	// L.out("GetTaskDefinitionFieldsForScreenByFacilityID: " + facilityID +
	// "\n" + foo);
	// ListTaskClassesByFacilityID bar =
	// Soap.getSoap().listTaskClassesByFacilityID(facilityID);
	// L.out("GetTaskDefinitionFieldsForScreenByFacilityID: " + facilityID +
	// "\n" + bar);
	// }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		L.out("onNewIntent: " + intent.getDataString());
	}

	public Class<?> getLoginClass() {
		return LoginActivity.class;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		L.out("onActivityResult: " + requestCode + " " + resultCode);
		// make sure we are starting
		Intent intent = new Intent(this.getApplicationContext(),
				getLoginClass());
		// L.out("starting");
		startActivity(intent);
	}

	@Override
	public void onPause() {
		super.onPause();
		L.out("onPause");
		// setStaleTime();
	}

	@Override
	protected void onRestart() {
		super.onRestart();

		if (User.getUser().getValidateUser() != null)
		{
			// Toast.makeText(this, "Disabled", Toast.LENGTH_LONG).show();
			txtUsername.setEnabled(false);
			txtPassword.setEnabled(false);
		}
		else
		{
			// Toast.makeText(this, "Enabled", Toast.LENGTH_LONG).show();
			txtUsername.setEnabled(true);
			txtPassword.setEnabled(true);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		L.out("onResume");
		// make sure UserWatcher has this activity
		// User.getUser().setNeedLogin(false);
		// new Updater(this).checkForUpdate();
		checkForUpdate();
		userWatcherLogin();
		userWatcherDoUpdate(false);
		// Tickler.onPause();
		// TaskActivity.initDataCache();
		PickField.initDataCache();
		// new LogoutThread(loginButton, this);
	}

	protected void userWatcherLogin() {
		// L.out("wrong userWatcherLogin!" + L.p());
		UserWatcher.INSTANCE.login(this);
	}

	protected void userWatcherDoUpdate(boolean flag) {
		// L.out("wrong userWatcherDoUpdate!" + L.p());
		UserWatcher.INSTANCE.doUpdate(flag);
	}

	public void checkForUpdate() {
		// L.out("wrong update!" + L.p());
		new Updater(this).checkForUpdate();
	}

	@Override
	protected void onDestroy() {
		// WakeLocker.INSTANCE.stop();
		L.out("onDestroy: " + User.getUser());
		Critter.onDestroy();
		binder.onDestroy();
		ApplicationContext.user = User.getUser();
		super.onDestroy();

	}

	public Class<?> getTopLevelClass() {
		return TabNavigationActivity.class;
	}

	public Class<?> getNotificationService(Activity activity) {
		return NotifyService.class;
	}

	protected void commonGUI() {
		TextView versionView = (TextView) this.findViewById(R.id.version);
		versionView.setText(getResources().getString(R.string.crothall_version));
<<<<<<< HEAD
		
		new NetworkTask().execute();
		
=======
>>>>>>> 26fe81448f9b13a55150e4f6c6f2ccca714ad58b
	}

	public void createGUI() {

		commonGUI();
		// if (ApplicationContext.user != null)
		// User.setUser(ApplicationContext.user.getUsername(),
		// ApplicationContext.user.getPassword());
		L.out("create gui: " + User.getUser());
		txtUsername = (EditText) this.findViewById(R.id.txtUsername);
		txtUsername.addTextChangedListener(this);
		txtPassword = (EditText) this.findViewById(R.id.txtPassword);
		txtPassword.addTextChangedListener(this);
		txtPassword.setOnEditorActionListener(this);

		loginButton = (Button) this.findViewById(R.id.btnLogin);
		platform = getResources().getString(R.string.default_platform);
		// user = User.setUser(txtUsername.getText().toString(),
		// txtPassword.getText().toString(), platform);
		L.out("login: " + user);

		String longClickonLogin = getResources().getString(R.string.long_click_on_login);
		L.out("longClickonLogin: " + longClickonLogin);

		SharedPreferences settings = getSharedPreferences(User.PREFERENCE_FILE, 0);
		boolean staffUser = settings.getBoolean(STAFF_USER, false);
		platform = settings.getString(User.PLATFORM, platform);
		L.out("platform: " + platform);
		String username = settings.getString(User.UserColumns.USERNAME, "");
		L.out("username: " + username);
		String employeeID = settings.getString(User.UserColumns.EMPLOYEE_ID, "");
		L.out("employeeID: " + employeeID);
		// String facilityID = settings.getString(User.UserColumns.FACILITY_ID,
		// "");
		editing = true;
		txtUsername.setText(username);
		editing = false;

		// platform = settings.getString(User.PLATFORM, "");
		L.out("staffUser: " + staffUser);
		if (staffUser) {
			longClickonLogin = "true";
			String password = settings.getString(User.UserColumns.PASSWORD, "");
			L.out("password: " + password);
			editing = true;
			txtPassword.setText(password);
			editing = false;
			addLongClick();
		} else {
			if (User.getUser() != null) {
				editing = true;
				txtPassword.setText(User.getUser().getPassword());
				editing = false;
			}
		}

		onClickListener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				vibrator.vibrate(200);
				// L.out("loginButton.getText: " + loginButton.getText());
				// L.out("getString(R.string.log_out): " +
				// getString(R.string.log_out));
				if (view.equals(loginButton) && loginButton.getText().equals(getString(R.string.log_out))) {
					doLogout();
					return;
				}
				SharedPreferences settings = getSharedPreferences(
						User.PREFERENCE_FILE, 0);
				boolean staffUser = settings.getBoolean(STAFF_USER, false);

				if (staffUser && txtPassword.getText().toString().equals("frank"))
					IntentionalCrash.intentionalCrash();
				L.out("platform: " + platform);
				currentUser = txtUsername.getText().toString();
				user = User.setUser(txtUsername.getText().toString(),
						txtPassword.getText().toString(), platform);
				L.out("user: " + user);
				if (txtPassword.getText().toString().equals("zxc")) {
					SharedPreferences.Editor editor = settings.edit();
					editor.putBoolean(STAFF_USER, true);
					editor.commit();
					txtPassword.setText("");
					// User.getUser().setNeedLogout(false);
					// userWatcherDoUpdate(true);
					if (wantMAM())
						stopService(new Intent(LoginActivity.this, BlockService.class));
					addLongClick();
					return;
				}
				if (txtPassword.getText().toString().equals("")) {
					if (staffUser) {
						MyToast.show("No longer staff user");
						SharedPreferences.Editor editor = settings.edit();
						editor.putBoolean(STAFF_USER, false);
						editor.commit();
						txtPassword.setText("");
					} else {
						MyToast.show("Invalidate Login: Enter a Password!");
					}
					return;
				}

				TaskActivity.initDataCache();
				SelfTaskActivity.initDataCache();
				StaticLoader.initDataCache();
				if (SoapDbAdapter.getSoapDbAdapter() != null)
					SoapDbAdapter.getSoapDbAdapter().deleteAll();
				Tickler.initLastStatus();
				new DoLogin(LoginActivity.this, LoginActivity.this).execute();
			}
		};
		loginButton.setOnClickListener(onClickListener);

		OnClickListener enterClickListener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				vibrator.vibrate(200);
				User.getUser().setNeedLogin(true);
				User.getUser().setNeedLogout(false);
				LogoutThread.notStopped = false;
				Tickler.onResume();
				Intent intent = new Intent().setClass(LoginActivity.this, getTopLevelClass());
				startActivity(intent);
			}
		};
		Button enterButton = (Button) this.findViewById(R.id.buttonEnter);
		// MyToast.show("enterButton: " + enterButton);
		if (enterButton != null)
			enterButton.setOnClickListener(enterClickListener);
	}

	public void doLogout() {
		L.out("legacy");
		user = User.getUser();
		if (!ActorController.status.getEmployeeStatus().equals(BreakActivity.AVAILABLE)) {
			MyToast.show("Unable to logout if you have a task or on delay!");
			return;
		}
		int updatesLeft = SoapDbAdapter.soapDbAdapter.somethingToUpdate();
		if (updatesLeft != 0) {
			MyToast.show("Unable to logout have " + L.getPlural(updatesLeft, "update") + " pending!");
			return;
		}
		user.setNeedLogout(true);
		if (ActorController.status.getEmployeeStatus().equals(BreakActivity.AVAILABLE))
			// UserWatcher.INSTANCE.doUpdate(false);
			userWatcherDoUpdate(false);
		txtPassword.setText("");
		UserWatcher.INSTANCE.stop();
		// user.getValidateUser().setEmployeeStatus(BreakActivity.NOT_IN);
		TaskActivity.setEmployeeStatus(BreakActivity.NOT_IN, false, LoginActivity.this);
		Tickler.onPause();
		// ApplicationContext.user = null;
		MyToast.show("Logged out");
		txtUsername.setEnabled(true);
		txtPassword.setEnabled(true);
		StaticLoader.finished = true;
	}

	private final String lastPlatform = null;

	// long click
	public void sayClick(View v) {
		user = User.next();
		L.out("user: " + user);
		txtUsername.setText(user.getUsername());
		txtPassword.setText(user.getPassword());
		loginButton.setEnabled(true);
		loginButton.setTextColor(Color.parseColor("#ffffff"));
		platform = user.getPlatform();
		user.setNeedLogout(true);
		UserWatcher.INSTANCE.doUpdate(false);
		if (platform.equals(lastPlatform))
			changePlatform();
		L.out("platform: " + platform);
		// MyToast.show("Platform is: " + shortName(user.getPlatform()),
		// Toast.LENGTH_SHORT);
		// MyToast.show("Server is: " + shortPlatform(user.getPlatform()),
		// Toast.LENGTH_SHORT);
		// MyToast.show("Changing user to: " + user.getUsername()
		// + "\nPlatform: " + user.getPlatform(), Toast.LENGTH_SHORT);
	}

	public String shortPlatform(String longPlatform) {
		if (longPlatform == null)
			return null;
		int index = longPlatform.indexOf(".com");
		if (index == -1)
			return longPlatform;
		return longPlatform.substring(0, index + 4);
	}

	public void changePlatform() {
		L.out("do nothing change platform");
	}

	public void sayClickIntentBlock(View v) {
		// vibrator.vibrate(200);
		L.out("BlockService.running: " + BlockService.running);
		if (!wantMAM())
			return;
		if (!BlockService.running) {
			MyToast.show("Started IntentBlock Service");
			startService(new Intent(this, BlockService.class));
		} else {
			MyToast.show("Stopped IntentBlock Service");
			stopService(new Intent(this, BlockService.class));
		}
		// startedIntentBlocker = !startedIntentBlocker;
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
	}

	@Override
	public void onTextChanged(CharSequence text, int arg1, int arg2, int arg3) {
		// L.out("onTextChanged: " + text);
		// if (text.toString().length() > 0) {
		// // setStaleTime();
		// }
	}

	private static final String PATTERN = "^[a-zA-Z 0-9?\\.\\+!\\*\\$%#@_\\-\\?,_]*$";
	private boolean editing = false;

	@Override
	public void afterTextChanged(Editable editable) {
		L.out("afterTextChanged: " + editing);
		if (editing)
			return;
		String text = editable.toString();
		int length = text.length();

		if (!Pattern.matches(PATTERN, text)) {
			editing = true;
			editable.delete(length - 1, length);
			editing = false;
		}
		// String text = arg0.toString();
		if (text.length() != 0) {
			User.getUser().setNeedLogout(true);

			User.getUser().setValidateUser(null);
		}
		// UserWatcher.INSTANCE.doUpdate(false);
		userWatcherDoUpdate(false);
	}

	protected void addLongClick() {
		TextView textView = (TextView) findViewById(R.id.fullScreen);
		if (!addedLongClick) {
			addedLongClick = true;
			MyToast.show("Running in staff mode");
			textView.setOnLongClickListener(new View.OnLongClickListener() {

				@Override
				public boolean onLongClick(View view) {
					// L.out("long view: " + view);
					if (isStaffUser()) {
						vibrator.vibrate(400);
						sayClick(view);
					}
					return true;
				}
			});
			// View view = findViewById(R.id.scrollView);
			// view.setOnLongClickListener(new View.OnLongClickListener() {
			//
			// @Override
			// public boolean onLongClick(View view) {
			// if (isStaffUser()) {
			// vibrator.vibrate(400);
			// sayClickIntentBlock(view);
			// }
			// return true;
			// }
			// });
			// View unitTestView = findViewById(R.id.unitTestView);
			// unitTestView.setOnLongClickListener(new
			// View.OnLongClickListener() {
			//
			// @Override
			// public boolean onLongClick(View view) {
			// if (isStaffUser()) {
			// vibrator.vibrate(400);
			// // Intent intent = new
			// // Intent().setClass(LoginActivity.this,
			// // UnitTestActivity.class);
			// // startActivity(intent);
			// TabNavigationActivity.showToasts =
			// !TabNavigationActivity.showToasts;
			// if (TabNavigationActivity.showToasts)
			// MyToast.show("Toasts are turned on!");
			// else
			// MyToast.show("Toasts are turned off!");
			// }
			// return true;
			// }
			// });
		} else {
			MyToast.show("Running in staff mode");
		}
	}

	public Class<?> getUnitTestActivity() {
		// MyToast.show("right one");
		return UnitTestActivity.class;
	}

	public static String getJSon(String methodName, Activity activity) {
		return getJSon(methodName, activity, null);
	}

	synchronized public static String getJSon(String methodName, Activity activity, String taskNumber) {
		// L.out("methodName: " + methodName);
		String facilityID = User.getUser().getFacilityID();
		String employeeID = User.getUser().getEmployeeID();
		// if
		// (methodName.equals(ParsingSoap.GET_TASK_DEFINITION_FIELDS_DATA_FOR_SCREEN_BY_FACILITY_ID)
		// ||
		// methodName.equals(ParsingSoap.GET_TASK_DEFINITION_FIELDS_FOR_SCREEN_BY_FACILITYID)
		// || methodName.equals(ParsingSoap.LIST_TASK_CLASSES_BY_FACILITY_ID)
		// || methodName.equals(ParsingSoap.LIST_DELAY_TYPES))
		// employeeID = null;
		Intent intent = activity.getIntent();
		String[] selectionArgs = new String[] { employeeID, facilityID, taskNumber };
		intent.setData(Uri.parse("content://" + StaticSoap.AUTHORITY + "/" +
				methodName));
		Cursor cursor = activity.managedQuery(activity.getIntent().getData(), null, null, selectionArgs, null);
		if (cursor != null) {
			cursor.moveToFirst();
			do {
				// int index = cursor.getColumnIndex(StaticSoapColumns.JSON);
				// L.out("index: " + index + " " + cursor.getClass());
				String json = cursor.getString(cursor.getColumnIndex(StaticSoapColumns.JSON));
				if (json != null) {
					// L.out("json: " + json.length());
				}
				else
					L.out("*** ERROR json: " + json);
				return json;

			} while (cursor.moveToNext());
		}
		return null;
	}

	public static boolean isConnectedToInternet(Context context) {
		// L.out("activity: " + context);
		if (context == null) {
			L.out("no context yet");
			return false;
		}

		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		return info != null && info.isConnected();
	}

	public static long getLoginTime() {
		return loginTime;
	}

	public static boolean enoughTimeToLogout(int timeToWait) {
		long now = new GregorianCalendar().getTimeInMillis();
		// L.out("enoughTimeToLogout: " + ((now - loginTime) / 1000 >
		// timeToWait));
		// L.out("now: " + now);
		// L.out("loginTime: " + loginTime);
		// L.out("((now - loginTime) / 1000): " + ((now - loginTime) / 1000));
		if (((now - loginTime) / 1000) > timeToWait)
			return true;
		return false;
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		L.out("action: " + actionId);
		onClickListener.onClick(v);
		return false;
	}

	synchronized void updateEmployeeDataModel(String employeeStatus, boolean tickled) {
		ValidateUser validateUser = User.getUser().getValidateUser();
		L.out("login updateEmployeeDataModel here: " + employeeStatus);
		String facilityID = User.getUser().getPassword();
		String employeeID = User.getUser().getUsername();
		String taskNumber = User.getUser().getPlatform();
		String soapMethod = ParsingSoap.VALIDATE_USER;
		validateUser.setEmployeeStatus(employeeStatus);
		validateUser.setTickled(tickled);

		ContentValues values = new ContentValues();
		values.put(TaskSoapColumns.JSON, validateUser.getNewJson());
		values.put(TaskSoapColumns.FACILITY_ID, User.getUser().getPassword());
		values.put(TaskSoapColumns.EMPLOYEE_ID, User.getUser().getUsername());
		values.put(TaskSoapColumns.TASK_NUMBER, taskNumber);
		values.put(TaskSoapColumns.SOAP_METHOD, soapMethod);

		SoapDbAdapter.soapDbAdapter.update(values, SoapDbAdapter.getWhere(soapMethod, employeeID, facilityID, taskNumber));
		// validateUser.setEmployeeStatus(employeeStatus);
	}

	private void registerCrittercism() {
		if (User.getUser().getValidateUser() != null) {
			try {
				JSONObject metadata = new JSONObject();
				// add arbitrary metadata
				metadata.put("user_id", User.getUser().getValidateUser().getEmployeeID());
				metadata.put("facility_id", User.getUser().getValidateUser().getFacilityID());
				metadata.put("name", User.getUser().getUsername());
				metadata.put("username", User.getUser().getUsername());
				metadata.put("android", android.os.Build.VERSION.RELEASE);
				metadata.put("MANUFACTURER", android.os.Build.MANUFACTURER);
				metadata.put("BRAND", android.os.Build.BRAND);
				metadata.put("DEVICE", android.os.Build.DEVICE);
				metadata.put("BOARD", android.os.Build.BOARD);
				// send metadata to crittercism (asynchronously)
				Crittercism.setMetadata(metadata);
			} catch (Exception e) {
				L.out("crittercism error: " + e);
			}
		}
	}

	@Override
	public void staticLoaderSuccess() {
		Crittercism.setUsername(User.getUser().getUsername() + "/" + User.getUser().getFacilityID());
		// L.out("success: " + User.getUser().getUsername() + "/" +
		// User.getUser().getFacilityID());
		MyToast.show("Successful Login");
		User.getUser().setNeedLogin(true);
		User.getUser().setNeedLogout(false);

		if (User.getUser().getValidateUser().getEmployeeStatus().equals(BreakActivity.NOT_IN)) {
			User.getUser().getValidateUser().setEmployeeStatus(BreakActivity.AVAILABLE);
			if (ActorController.status != null)
				ActorController.status.setEmployeeStatus(BreakActivity.AVAILABLE);
			updateEmployeeDataModel(BreakActivity.AVAILABLE, false);
		} else {
			if (ActorController.status != null)
				ActorController.status.setEmployeeStatus(User.getUser().getValidateUser().getEmployeeStatus());
			updateEmployeeDataModel(BreakActivity.AVAILABLE, true);
		}
		preferences.edit().putString("lastUser", User.getUser().getUsername()).commit();
		Intent intent = new Intent().setClass(this, getTopLevelClass());
		startActivity(intent);
	}

	@Override
	public void staticLoaderFail() {
		String temp = "Failed loading Static Content!"
				+ "\nYou are welcome to try again"
				+ "\n(just press Enter)."
				+ "\nOr you may wait until"
				+ "\nyou have better WI-FI."
				+ "\n";
		MyToast.show(temp, Toast.LENGTH_LONG);
		MyToast.show(temp, Toast.LENGTH_LONG);
		User.getUser().setValidateUser(null);
	}

	@Override
	public void loginFailed() {
		if (!isConnectedToInternet(this))
			MyToast.show("Need a connection to internet\nto login!");
		else
			MyToast.show("Invalid Login");
		User.getUser().setValidateUser(null);
	}

	@Override
	public void loginSuccess() {
		registerCrittercism();
		new DownloadStaticTask(this, this).execute();
	}

<<<<<<< HEAD
	class NetworkTask extends AsyncTask<Void, Void, Boolean>{
		
		boolean isReachable = false;
		boolean networkConnection = false;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			final ConnectivityManager connMgr = (ConnectivityManager) LoginActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
			final NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
			
			if (netInfo != null && netInfo.isConnected())
				networkConnection = true;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			
			if (networkConnection) {
				// Some sort of connection is open, check if server is reachable
				try {
					URL url = new URL("http://www.google.com");
					// URL url = new URL("http://10.0.2.2");
					HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
					urlc.setRequestProperty("User-Agent", "Android Application");
					urlc.setRequestProperty("Connection", "close");
					urlc.setConnectTimeout(2 * 1000);
					urlc.connect();
					isReachable = (urlc.getResponseCode() == 200);
					
				} catch (IOException e) {
				}
			}
			
			return isReachable;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			TextView versionView = (TextView) LoginActivity.this.findViewById(R.id.version);
			int color = Color.parseColor("#FFFF0000");
			if (networkConnection)
				color = Color.parseColor("#FF0000FF");
			if (isReachable)
				color = Color.parseColor("#FFAAAAAA");
			
			versionView.setTextColor(color);
		}
		
	}
	

=======
>>>>>>> 26fe81448f9b13a55150e4f6c6f2ccca714ad58b
}
