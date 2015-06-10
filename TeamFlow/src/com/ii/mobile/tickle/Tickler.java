package com.ii.mobile.tickle;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.ii.mobile.database.NetworkUploader;
import com.ii.mobile.home.LoginActivity;
import com.ii.mobile.home.MyToast;
import com.ii.mobile.home.StaticLoader;
import com.ii.mobile.soap.NetKiller;
import com.ii.mobile.soap.ParsingSoap;
import com.ii.mobile.soap.Soap;
import com.ii.mobile.soap.SoapDbAdapter;
import com.ii.mobile.soap.gson.GJon;
import com.ii.mobile.soap.gson.GetEmployeeAndTaskStatusByEmployeeID;
import com.ii.mobile.soap.gson.GetTaskInformationByTaskNumberAndFacilityID;
import com.ii.mobile.soap.gson.ValidateUser;
import com.ii.mobile.tab.AudioPlayer;
import com.ii.mobile.tab.BreakActivity;
import com.ii.mobile.tab.IMActivity;
import com.ii.mobile.tab.TaskActivity;
import com.ii.mobile.task.TaskSoap.TaskSoapColumns;
import com.ii.mobile.users.User;
import com.ii.mobile.util.L;

public class Tickler implements Runnable {

	public static final String TASK_NUMBER = "TaskNumber";
	public static final String JSON = "JSON";
	public static final String TASK_STATUS = "TaskStatus";
	public static final String TEXT_MESSAGE = "TextMessage";
	public static final String RECEIVED_DATE = "ReceivedDate";
	public static final String FROM_USER_NAME = "FromUserName";
	public static final String EMPLOYEE_STATUS = "EmployeeStatus";
	public static final String IM_STATUS = "IMStatus";

	private static Context context;
	private Thread thread = null;
	private boolean lastConnection = false;
	private static Tickler tickler = null;

	private final int NO_CONNECTION_WAIT = 1000;
	public static final int EXAMPLE = 0;
	public static final int ANOTHER_EXAMPLE = 1;

	public static final int POLLING_INTERVAL = 2500;

	private static boolean wasPaused = false;

	static int count = 0;
	private static GetEmployeeAndTaskStatusByEmployeeID lastStatus;
	@SuppressWarnings("unused")
	private final TickleService tickleService;
	public static boolean blocked = false;

	public static Tickler startTickler(TickleService tickleService, Context context) {
		// L.out("context: " + context);
		if (tickler != null) {
			return tickler;
		}
		tickler = new Tickler(tickleService, context);
		tickler.thread.setName("TickleThread");
		// Tickler.onPause();
		return tickler;
	}

	public static Tickler getTickler() {
		return tickler;
	}

	private Tickler(TickleService tickleService, Context context) {
		this.tickleService = tickleService;
		Tickler.context = context;
		this.thread = new Thread(this);
		thread.start();
	}

	@Override
	public void run() {
		// L.out("starting Tickler!");

		while (true) {
			wasPaused = false;
			if (checkConnection()) {
				// if ((count % 100) == 0)
				// L.out(count + ": tickling ");
				count += 1;
				L.sleep(POLLING_INTERVAL);
				if (!NetKiller.kill(false))
					pollTickler();
				synchronized (mPauseLock) {
					while (mPaused) {
						try {
							// L.sleep(5000);
							L.out("started pauselock");
							mPauseLock.wait();
						} catch (InterruptedException e) {
						}
						// L.out("finish sleeping Tickler onPause");
					}
					// L.out("running mPaused: " + mPaused);
				}
			}
		}
	}

	private static final Object mPauseLock = new Object();
	private static boolean mPaused = false;

	public static void onPause() {
		IMActivity.p("Tickler onPause: " + "\n");
		// L.out("Tickler onPause: " + mPaused);
		// IMActivity.p("Tickler onPause\n");
		mPaused = true;
		wasPaused = true;
		if (!mPaused) {
			// if (TabNavigationActivity.showToasts)
			// MyToast.show("Tickler onPause");
			synchronized (mPauseLock) {
				// MyToast.show("Tickler finished onPause");
				// mPaused = true;
			}
		}
		L.out("finished Tickler onPause");
	}

	public static void onResume() {
		IMActivity.p("Tickler onResume: " + "\n");
		// L.out("Tickler onResume: " + "\n");
		boolean loggedOut = false;
		if (ActorController.status != null && ActorController.status.equals(BreakActivity.NOT_IN)) {
			loggedOut = true;
			return;
		}
		// L.sleep(5000);
		// L.out("Tickler loggedOut: " + loggedOut);
		if (mPaused && !loggedOut)
			synchronized (mPauseLock) {

				mPaused = false;
				// lastStatus = null;
				mPauseLock.notifyAll();
			}
	}

	private void pollTickler() {
		try {
			if (User.getUser() == null) {
				L.out("*** ERROR user is null!");
				return;
			}

			User user = User.getUser();
			ValidateUser validateUser = user.getValidateUser();
			if ((count % 100) == 0)
				L.out(count
						+ ": tickling "
						+ lastStatus
						+ " "
						);
			if (validateUser == null)
				return;

			GetEmployeeAndTaskStatusByEmployeeID status =
					Soap.getSoap().getEmployeeAndTaskStatusByEmployeeID(validateUser.getEmployeeID());
			// L.out("status: " + status.getJson());

			if (mPaused || wasPaused) {
				L.out("wasPaused ignoring the status");
				return;
			}
			if (status == null) {
				L.out("ERROR getEmployeeAndTaskStatusByEmployeeID is null! ");
				return;
			} else {
				if (status.getTaskStatus() != null && status.getTaskStatus().length() < 2)
					status.setTaskStatus("");
				if (status.getEmployeeStatus() != null && status.getEmployeeStatus().length() < 2)
					status.setEmployeeStatus("");

				if (!StaticLoader.finished) {
					IMActivity.p("ignoring tickle - waiting on staticLoad: " + status.getTaskNumber());
					return;
				}

				ActorController.checkIMMessage(status);
				if (ActorController.task != null
						&& !ActorController.task.getTaskNumber().equals(status.getTaskNumber())) {
					if (status.getTaskNumber() != null) {
						L.out("ignore new task tickle!");
						return;
					}
				}
				sendTaskUpdate(status);
			}
		} catch (Exception e) {
			L.out("*** Tickle Error: " + e + L.p());
		}
	}

	public void sendTaskUpdate(GetEmployeeAndTaskStatusByEmployeeID status) {
		// L.out("different: " + isDifferent(status));
		// L.out("status: " + status.getEmployeeStatus());
		if (status.getEmployeeStatus().equals(BreakActivity.NOT_IN)
				&& !LoginActivity.enoughTimeToLogout(30)) {
			L.out("ignoring not_in tickle!");
			return;
		}
		blocked = false;

		if (isDifferent(status, lastStatus)) {
			L.out("is different: " + lastStatus);
			String taskNumber = status.getTaskNumber();
			// if (taskNumber == null || taskNumber.length() < 2) {
			// taskNumber = null;
			// status.setTaskNumber(null);
			// }
			L.out("status: " + status.toStringShort());
			// L.out("taskNumber: " + taskNumber);
			if (taskNumber != null) {
				GetTaskInformationByTaskNumberAndFacilityID cacheStatus = ActorController.getTaskInformationByTaskNumberAndFacilityID(taskNumber);
				// L.out("fetched from cache: " + cacheStatus);
				if (cacheStatus == null) {
					User user = User.getUser();
					if (user != null && user.getValidateUser() != null) {

						ValidateUser validateUser = user.getValidateUser();
						GetTaskInformationByTaskNumberAndFacilityID task =
								Soap.getSoap().getTaskInformationByTaskNumberAndFacilityID(taskNumber, validateUser.getFacilityID());
						// L.out("task: " + task);

						if (task == null) {
							L.out("ERROR: task is null");
							return;
						}
						IMActivity.p("Dispatch task: " + task.getTaskNumber() + " status: "
								+ task.getTaskStatusBrief() + "\n");

						task.setFunctionalArea(User.getUser().getValidateUser().getFunctionalArea());
						task.setMobileUserName(validateUser.getMobileUserName());
						boolean updated = ActorController.setTaskInformationByTaskNumberAndFacilityID(task, status, taskNumber, true);
						if (updated)
							return;
						// long result =
						// SoapDbAdapter.getSoapDbAdapter().create(ParsingSoap.GET_TASK_INFORMATION_BY_TASK_NUMBER_AND_FACILITY_ID,
						// taskStatus.getJson(), validateUser.getEmployeeID(),
						// validateUser.getFacilityID(), taskNumber);
						// L.out("result : " + result);
						if (ActorController.task == null
								|| ActorController.task.getTaskNumber().equals(status.getTaskNumber())) {

							task.tickled = GJon.TRUE_STRING;
							updateTaskDataModel(task.getNewJson(), validateUser.getEmployeeID(), validateUser.getFacilityID(), taskNumber);
							updateEmployeeDataModel(status.getEmployeeStatus(), true);
							ActorController.task = task;
							ActorController.status = status;
							AudioPlayer.INSTANCE.playSound(AudioPlayer.NEW_TASK);
						} else {
							MyToast.show("Dispatch assigned a new Task: "
									+ status.getTaskNumber());
							AudioPlayer.INSTANCE.playSound(AudioPlayer.DISPATCHER_CHANGE_STATE);
						}
					}
				} else {
					if (isDifferent(status, ActorController.status)) {
						IMActivity.p("Task to " + status.getTaskStatus() + " from Server\n");
						MyToast.show("Dispatch Changed Task Status: \n" + status.toStringShort(), Toast.LENGTH_LONG);
						AudioPlayer.INSTANCE.playSound(AudioPlayer.DISPATCHER_CHANGE_STATE);
					}
					cacheStatus.setTaskStatusBrief(status.getTaskStatus());
				}

			} else if (isDifferent(status, ActorController.status)) {
				if (SoapDbAdapter.haveSomethingToUpload() > 0) {
					L.out("ignoring server until done updating: " + status);
					blocked = true;
					NetworkUploader.networkUploader.onResume();
					return;
				}
				IMActivity.p("Dispatch user: " + status.getEmployeeStatus() + "\n");
				MyToast.show("Dispatch Changed Status: \n" + status.toStringShort(), Toast.LENGTH_LONG);

				ActorController.status = status;
				AudioPlayer.INSTANCE.playSound(AudioPlayer.DISPATCHER_CHANGE_STATE);
			}

			ActorController.update(status);
		}
		if (status != null)
			lastStatus = status;
	}

	public static void initLastStatus() {
		lastStatus = null;
	}

	private boolean isDifferent(GetEmployeeAndTaskStatusByEmployeeID status,
			GetEmployeeAndTaskStatusByEmployeeID otherStatus) {
		if (otherStatus == null) {
			L.out("otherStatus is null ");
			return true;
		}
		// if (status.getMessage() != null
		// && status.getMessage().mobileMessage != null
		// && status.getMessage().mobileMessage.length > 0)
		// L.out("getMessage ");
		if (!status.getEmployeeStatus().equals(otherStatus.getEmployeeStatus()))
		{
			L.out("getEmployeeStatus:  " + status.getEmployeeStatus());
			if (otherStatus != null)
				L.out("getEmployeeStatus:  " + otherStatus.getEmployeeStatus());
		}
		if (sameNullField(status.getTaskStatus(), otherStatus.getTaskStatus()))
			L.out("getTaskStatus ");
		if (sameNullField(status.getTaskNumber(), otherStatus.getTaskNumber())) {
			L.out("getTaskNumber: " + status.getTaskNumber());
			L.out("lastStatus.getTaskNumber: " + otherStatus.getTaskNumber());
		}

		// if (lastStatus == null)
		// return true;
		// if (status.getMessage() != null
		// && status.getMessage().mobileMessage != null
		// && status.getMessage().mobileMessage.length > 0)
		// return true;
		if (!status.getEmployeeStatus().equals(otherStatus.getEmployeeStatus()))
			return true;
		if (sameNullField(status.getTaskNumber(), otherStatus.getTaskNumber()))
			return true;
		if (status != null && !status.equals(TaskActivity.COMPLETED))
			return false;
		if (sameNullField(status.getTaskStatus(), otherStatus.getTaskStatus()))
			return true;

		return false;
	}

	private boolean sameNullField(String statusOne, String statusTwo) {
		if (statusOne == null && statusTwo == null)
			return false;
		if (statusOne != null) {
			if (statusOne.equals(statusTwo))
				return false;
			return true;
		} else {
			if (statusTwo != null)
				return false;
		}
		return true;
	}

	private boolean checkConnection() {
		if (!isConnectedToInternet() || User.getUser() == null) {
			if (lastConnection) {
				L.out("No connection to internet");
				// MyToast.show("Network unreachable\nData will be saved and\nuploaded automatically\nto Crothall");
				lastConnection = false;
				lastStatus = null;
			}
			L.sleep(NO_CONNECTION_WAIT);
		} else {
			if (!lastConnection) {
				L.out("lastConnection: " + lastConnection);
				// MyToast.show("Network Reachable\nUploading data to Crothall");
			}
			lastConnection = true;
		}
		return lastConnection;
	}

	public static boolean isConnectedToInternet() {
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

	synchronized void updateEmployeeDataModel(String employeeStatus, boolean tickled) {
		ValidateUser validateUser = User.getUser().getValidateUser();
		L.out("updateEmployeeDataModel: " + employeeStatus);
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
		values.put(SoapDbAdapter.TICKLED, true);

		SoapDbAdapter.getSoapDbAdapter().update(values, SoapDbAdapter.getWhere(soapMethod, employeeID, facilityID, taskNumber));
	}

	private void updateTaskDataModel(String json, String employeeID, String facilityID, String taskNumber) {

		// L.out("updateDataModel");
		String soapMethod = ParsingSoap.GET_TASK_INFORMATION_BY_TASK_NUMBER_AND_FACILITY_ID;

		ContentValues values = new ContentValues();
		values.put(TaskSoapColumns.JSON, json);
		values.put(TaskSoapColumns.FACILITY_ID, facilityID);
		values.put(TaskSoapColumns.EMPLOYEE_ID, employeeID);
		values.put(TaskSoapColumns.TASK_NUMBER, taskNumber);
		values.put(TaskSoapColumns.SOAP_METHOD, soapMethod);
		values.put(SoapDbAdapter.TICKLED, GJon.TRUE_STRING);

		SoapDbAdapter.getSoapDbAdapter().update(values, SoapDbAdapter.getWhere(soapMethod, employeeID, facilityID, taskNumber));
	}
}
