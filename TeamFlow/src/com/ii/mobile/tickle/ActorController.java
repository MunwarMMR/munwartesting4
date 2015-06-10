package com.ii.mobile.tickle;

import java.util.Hashtable;

import android.os.AsyncTask;
import android.os.Bundle;

import com.ii.mobile.application.ApplicationContext;
import com.ii.mobile.database.NetworkUploader;
import com.ii.mobile.home.LoginActivity;
import com.ii.mobile.home.MyToast;
import com.ii.mobile.home.UserWatcher;
import com.ii.mobile.soap.SoapDbAdapter;
import com.ii.mobile.soap.gson.GetEmployeeAndTaskStatusByEmployeeID;
import com.ii.mobile.soap.gson.GetEmployeeAndTaskStatusByEmployeeID.Message;
import com.ii.mobile.soap.gson.GetEmployeeAndTaskStatusByEmployeeID.MobileMessage;
import com.ii.mobile.soap.gson.GetTaskInformationByTaskNumberAndFacilityID;
import com.ii.mobile.tab.BreakActivity;
import com.ii.mobile.tab.IMActivity;
import com.ii.mobile.tab.TabNavigationActivity;
import com.ii.mobile.tab.TaskActivity;
import com.ii.mobile.users.User;
import com.ii.mobile.util.L;

public enum ActorController {
	INSTANCE;

	public static Hashtable<String, GetTaskInformationByTaskNumberAndFacilityID> getActionStatusHashtable = new Hashtable<String, GetTaskInformationByTaskNumberAndFacilityID>();
	public static GetTaskInformationByTaskNumberAndFacilityID task = null;
	public static GetEmployeeAndTaskStatusByEmployeeID status = null;
	public static TaskActivity activity = null;

	public static Long loginTime = 0l;

	public static GetTaskInformationByTaskNumberAndFacilityID getTaskInformationByTaskNumberAndFacilityID(
			String taskNumber) {
		return getActionStatusHashtable.get(taskNumber);
	}

	public static boolean setTaskInformationByTaskNumberAndFacilityID(
			GetTaskInformationByTaskNumberAndFacilityID task, GetEmployeeAndTaskStatusByEmployeeID status,
			String taskNumber, boolean assign) {
		L.out("taskNumber : " + taskNumber);
		GetTaskInformationByTaskNumberAndFacilityID sameTask = SoapDbAdapter.soapDbAdapter.isSameTask(task);
		// L.out("sameTask : " + sameTask);
		if (sameTask != null) {
			IMActivity.p("Server updated: " + taskNumber + " for: " + sameTask.oldTaskNumber + "\n");
			task = sameTask;
			getActionStatusHashtable.put(taskNumber, task);
			if (ActorController.task != null
					&& sameTask.oldTaskNumber.equals(ActorController.task.getTaskNumber()))
				ActorController.task = sameTask;
			NetworkUploader networkUploader = NetworkUploader.getNetworkUploader();
			L.out("networkUploader: " + networkUploader);
			if (networkUploader != null) {
				networkUploader.onResume();
			}
			return true;
		}

		getActionStatusHashtable.put(taskNumber, task);
		update(status);
		return false;
	}

	public static void setTaskInformationByTaskNumberAndFacilityID(
			GetTaskInformationByTaskNumberAndFacilityID task, String taskNumber, boolean b) {
		L.out("taskNumber : " + taskNumber);
		getActionStatusHashtable.put(taskNumber, task);
	}

	public static void update(GetEmployeeAndTaskStatusByEmployeeID status) {
		// L.out("update : " + status);
		ActorController.status = status;
		if (status.getEmployeeStatus().equals(BreakActivity.NOT_IN)) {

			// L.out("finished");
			MyToast.show("Logged out by dispatch!");
			ApplicationContext.activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// UserWatcher.INSTANCE.stop();
					// User.getUser().getValidateUser() = null;

					UserWatcher.INSTANCE.doUpdate(true);
					User.getUser().setValidateUser(null);
					Tickler.onPause();
					TabNavigationActivity.getTabNavigationActivity().finish();
				}
			});
			// L.out("really finished");

			return;
		}
		if (status.getTaskNumber() == null)
			task = null;
		else {
			GetTaskInformationByTaskNumberAndFacilityID newTask =
					getTaskInformationByTaskNumberAndFacilityID(status.getTaskNumber());
			if (newTask == null)
				L.out("newTask is null: " + status.getTaskNumber());
			task = newTask;
		}
		new UpdateView().execute();
	}

	public static void checkIMMessage(GetEmployeeAndTaskStatusByEmployeeID status) {
		// L.out("checkIMMessage : " + status.message);
		if (status.message != null) {
			Message message = status.message;
			if (message.mobileMessage == null)
				return;
			for (final MobileMessage mobileMessage : message.mobileMessage) {
				// L.out("mobileMessage : " + mobileMessage);
				final Bundle data = new Bundle();
				// String receivedDate = mobileMessage.receivedDate;
				// L.out("mobileMessage.receivedDate: " + receivedDate);
				data.putString(Tickler.TEXT_MESSAGE, mobileMessage.textMessage);
				data.putString(Tickler.RECEIVED_DATE, mobileMessage.receivedDate);
				data.putString(Tickler.FROM_USER_NAME, mobileMessage.fromUserName);
				// if (lastReceivedDate == null ||
				// !receivedDate.equals(lastReceivedDate))
				LoginActivity.loginActivity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// L.out("receivedMessage : " +
						// mobileMessage.textMessage);
						IMActivity.receivedMessage(data);
					}
				});

				// lastReceivedDate = receivedDate;
			}
		}
	}

	public static void init(TaskActivity taskActivity) {
		activity = taskActivity;
		if (status == null && User.getUser() != null && User.getUser().getValidateUser() != null) {
			status = new GetEmployeeAndTaskStatusByEmployeeID();
			status.setEmployeeStatus(User.getUser().getValidateUser().getEmployeeStatus());
			status.setTaskNumber(User.getUser().getValidateUser().getTaskNumber());
			status.setTaskStatus(User.getUser().getValidateUser().getTaskStatus());
			new UpdateView().execute();
		}
	}

}

class UpdateView extends AsyncTask<Integer, Integer, Long> {

	@Override
	protected Long doInBackground(Integer... params) {
		Thread.currentThread().setName("TaskControllerUpdateViewThread");
		return 0l;
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		L.out("probably not used but executes in UI thread");
	}

	@Override
	protected void onPreExecute() {
	}

	@Override
	protected void onPostExecute(Long l) {

		if (ActorController.task != null) {
			if (ActorController.task.getTaskStatusBrief().equals(TaskActivity.ASSIGNED))
				TaskActivity.taskActivity.startAssignedTimer();
			else if (ActorController.task.getTaskStatusBrief().equals(TaskActivity.ACTIVE)
					|| ActorController.task.getTaskStatusBrief().equals(TaskActivity.DELAYED)) {
				TaskActivity.taskActivity.startActiveTimer();
				// taskActivity.startAssignedTimer();
			}
			else if (ActorController.task.getTaskStatusBrief().equals(TaskActivity.COMPLETED))
				TaskActivity.taskActivity.stopTimer();
		}
		BreakActivity.breakActivity.update();
		TaskActivity.taskActivity.updateView();
	}
}