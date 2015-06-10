package com.ii.mobile.soap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.StrictMode;

import com.ii.mobile.home.MyToast;
import com.ii.mobile.soap.gson.GJon;
import com.ii.mobile.soap.gson.GetEmployeeAndTaskStatusByEmployeeID;
import com.ii.mobile.soap.gson.GetTaskInformationByTaskNumberAndFacilityID;
import com.ii.mobile.soap.gson.Logger;
import com.ii.mobile.soap.gson.ValidateUser;
import com.ii.mobile.tab.BreakActivity;
import com.ii.mobile.tab.IMActivity;
import com.ii.mobile.tab.StatusType;
import com.ii.mobile.tab.TabNavigationActivity;
import com.ii.mobile.tab.TaskActivity;
import com.ii.mobile.tickle.ActorController;
import com.ii.mobile.users.User;
import com.ii.mobile.util.L;

public class RestWriter extends RestService {
	public static final String SUCCESSFUL = "success";
	public static final String FAILURE = "fail";

	public static final String TASK_START = "MobileTaskStart";
	public static final String TASK_DELAY = "MobileTaskDelay";
	public static final String TASK_COMPLETE = "MobileTaskComplete";
	public static final String TASK_COMPLETE_AND_UPDATE_EMPLOYEE_STATUS = "MobileTaskCompleteAndUpdateEmployeeStatus";
	// public static final String UPDATE_TASK_METHOD = "MobileTaskUpdate";
	public final static String MOBILE_UPDATE_PERSONNEL_STATUS = "MobileUpdatePersonnelStatus";
	public final static String MOBILE_TASK_UPDATE = "MobileTaskUpdate";
	public final static String MOBILE_LOGGER = "MobileLogger";

	public RestWriter() {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);
	}

	public String updateRecord(String employeeID, String facilityID, GJon record, String oldTaskNumber,
			String id) {
		// no idea why doesn't call the correct specialization directly! kmf
		// L.out("*** ERROR Should be specialized: " + record.getClass());
		if (record instanceof ValidateUser) {
			return updateValidateUser((ValidateUser) record);
		}
		else if (record instanceof GetTaskInformationByTaskNumberAndFacilityID) {
			// IMActivity.p("Ok");
			return updateTask(employeeID, facilityID, (GetTaskInformationByTaskNumberAndFacilityID) record, oldTaskNumber, id);
		}
		else if (record instanceof Logger) {
			// IMActivity.p("Ok");
			return updateLogger((Logger) record);
		}
		else {
			IMActivity.p("Fail: " + record + "\n");
			L.out("RestWriter.updateRecord error: \n" + record);
		}
		return null;
	}

	public String updateValidateUser(ValidateUser validateUser) {

		String updateMethod = MOBILE_UPDATE_PERSONNEL_STATUS;

		String status = validateUser.getEmployeeStatus();
		// L.out("status: " + status);
		if (validateUser.getTickled()) {
			// L.out("RestWriter validateUser was tickled ignoring update: " +
			// status);
			return SUCCESSFUL;
		}
		// if (TabNavigationActivity.showToasts)
		// MyToast.show("Updating Actor Status: " + status);
		// L.out("RestWriter.updateRecord validateUser: " + updateMethod);
		IMActivity.p("Update User: " + validateUser.getEmployeeStatus() + "\n");
		if (NetKiller.kill()) {
			IMActivity.p("NetKiller Fail Update user: " + validateUser.getEmployeeStatus() + "\n");
			return null;
		}
		String statusCode = StatusType.lookUp(status);
		L.out("updateRecord statusCode: " + statusCode);
		List<Arg> values = new ArrayList<RestWriter.Arg>();
		values.add(new Arg("Status", statusCode));
		values.add(new Arg("TaskNumber", ""));
		values.add(new Arg("SteEmployeeID", validateUser.getEmployeeID()));
		values.add(new Arg("SteHirNode", validateUser.getFacilityID()));
		values.add(new Arg("EnteredBy", validateUser.getMobileUserName()));
		for (Arg arg : values) {
			L.out("pair: " + arg.type + " " + arg.value);
		}

		String result = mobileUpdateRecord(BaseSoap.URL + "/" + updateMethod, getNameValuePairs(values));
		// if (TabNavigationActivity.showToasts)
		// MyToast.show("Updated  Actor Status: " + status
		// + "\n" + result);
		L.out("result: " + result);
		if (result == null) {
			IMActivity.p("Fail Update user: " + validateUser.getEmployeeStatus() + "\n");
			settingToAvailable = false;
		} else {
			IMActivity.p("Ok" + "\n");
		}
		return result;
	}

	public String updateLogger(Logger logger) {

		String updateMethod = MOBILE_UPDATE_PERSONNEL_STATUS;

		// if (TabNavigationActivity.showToasts)
		// MyToast.show("Updating Actor Status: " + status);
		// L.out("RestWriter.updateRecord validateUser: " + updateMethod);
		IMActivity.p("Update Log: " + logger.getEmployeeAndTaskStatusByEmployeeID.getMobileUserName() + "\n");

		List<Arg> values = new ArrayList<RestWriter.Arg>();
		// values.add(new Arg("Status", statusCode));
		// values.add(new Arg("TaskNumber", ""));
		// values.add(new Arg("SteEmployeeID", validateUser.getEmployeeID()));
		// values.add(new Arg("SteHirNode", validateUser.getFacilityID()));
		// values.add(new Arg("EnteredBy", validateUser.getMobileUserName()));
		for (Arg arg : values) {
			L.out("pair: " + arg.type + " " + arg.value);
		}

		String result = mobileUpdateRecord(BaseSoap.URL + "/" + updateMethod, getNameValuePairs(values));
		// if (TabNavigationActivity.showToasts)
		// MyToast.show("Updated  Actor Status: " + status
		// + "\n" + result);
		L.out("result: " + result);
		if (result == null) {
			// IMActivity.p("Fail Update user: " +
			// validateUser.getEmployeeStatus() + "\n");
			settingToAvailable = false;
		} else {
			IMActivity.p("Ok" + "\n");
		}
		return result;
	}

	static boolean blocked = false;

	public static boolean checkIfTheUserCoastIsClear() {
		ValidateUser validateUser = User.getUser().getValidateUser();
		if (validateUser == null) {
			L.out("ValidateUser is null!");
			return false;
		}
		String currentEmployeeStatus = validateUser.getEmployeeStatus();

		if (currentEmployeeStatus.equals(BreakActivity.AT_LUNCH)
				|| currentEmployeeStatus.equals(BreakActivity.ON_BREAK)
				|| currentEmployeeStatus.equals(BreakActivity.NOT_IN)) {
			// MyToast.show("getting it");
			GetEmployeeAndTaskStatusByEmployeeID taskStatus =
					Soap.getSoap().getEmployeeAndTaskStatusByEmployeeID(validateUser.getEmployeeID());
			L.out("taskStatus: " + taskStatus);

			if (taskStatus == null)
				return false;
			String taskNumber = taskStatus.getTaskNumber();
			if (taskNumber != null) {
				IMActivity.o("User coast not clear have taskNumber: " + taskNumber);
				if (TabNavigationActivity.showToasts)
					MyToast.show("Unable to update status - have task: " + taskNumber);
				IMActivity.o(".");
				blocked = true;
				return false;
			}
		}
		if (blocked)
			IMActivity.o("\n");
		blocked = false;
		return true;
	}

	private static boolean settingToAvailable = false;

	public static boolean checkIfTheTaskCoastIsClear(String currentTaskNumber, boolean wantCreate,
			GetEmployeeAndTaskStatusByEmployeeID taskStatus) {
		L.out("wantCreate: " + wantCreate);
		ValidateUser validateUser = User.getUser().getValidateUser();
		if (validateUser == null) {
			return false;
		}

		if (taskStatus == null)
			taskStatus = Soap.getSoap().getEmployeeAndTaskStatusByEmployeeID(validateUser.getEmployeeID());
		if (taskStatus == null) {
			L.out("*** ERROR getEmployeeAndTaskStatusByEmployeeID is null! "
					+ validateUser);
			return false;
		}

		// GetEmployeeAndTaskStatusByEmployeeID taskStatus =
		// Soap.getSoap().getEmployeeAndTaskStatusByEmployeeID(validateUser.getEmployeeID());

		L.out("checkIfTheTaskCoastIsClear taskStatus: " + taskStatus.getTaskNumber() + " "
				+ taskStatus.getTaskStatus());
		L.out("currentTaskNumber: " + currentTaskNumber);
		if (wantCreate) {
			String employeeStatus = taskStatus.getEmployeeStatus();
			L.out("employeeStatus: " + employeeStatus);
			if (!employeeStatus.equals(BreakActivity.AVAILABLE)) {

				if (employeeStatus.equals(BreakActivity.ON_BREAK)
						|| employeeStatus.equals(BreakActivity.AT_LUNCH)
						|| employeeStatus.equals(BreakActivity.NOT_IN)) {
					if (!settingToAvailable) {
						IMActivity.p("Setting to Available from " + taskStatus.getEmployeeStatus()
								+ "\n");
						settingToAvailable = true;
						ValidateUser validate = new ValidateUser();
						validate.setEmployeeStatus(BreakActivity.AVAILABLE);
						validate.setTaskNumber("");
						validate.setEmployeeID(validateUser.getEmployeeID());
						validate.setFacilityID(validateUser.getFacilityID());
						validate.setMobileUserName(validateUser.getMobileUserName());
						String result = new RestWriter().updateValidateUser(validate);
						// IMActivity.p("result: " + result);
					}
				}

				return false;
			} else
				// IMActivity.p("TaskCoastIsNotClear: " + employeeStatus +
				// " #: "
				// + taskStatus.getTaskNumber());
				return true;
		}
		String taskNumber = taskStatus.getTaskNumber();

		if (taskNumber != null && !currentTaskNumber.equals(taskNumber)) {
<<<<<<< HEAD
			IMActivity.p("Unable to update status of " + currentTaskNumber + " have legacy task: "
					+ taskNumber);
=======
			if (TabNavigationActivity.showToasts)
				MyToast.show("Unable to update status of " + currentTaskNumber + " have legacy task: "
						+ taskNumber);
>>>>>>> 26fe81448f9b13a55150e4f6c6f2ccca714ad58b
			return false;
		}
		if (blocked)
			IMActivity.p(".");
		blocked = false;

		settingToAvailable = false;
		return true;
	}

	public static final String COAST_IS_NOT_CLEAR = "CoastIsNotClear";
	String lastUpdateTaskNumber = "";

	public String updateTask(String employeeID, String facilityID,
			GetTaskInformationByTaskNumberAndFacilityID task, String oldTaskNumber, String id) {
		// if (TabNavigationActivity.showToasts)
		// MyToast.show("UpdateTask: " + task.getTaskNumber() + " to Status: "
		// + task.getTaskStatusBrief());
		if (task.getTickled()) {
			L.out("RestWriter Task was tickled ignoring update: " + task.getTaskStatusBrief());
			return SUCCESSFUL;
		}
		// if (NetKiller.kill())
		// return null;
		String taskNumber = task.getTaskNumber();
		L.out("updateTask taskNumber: " + taskNumber + " oldTaskNumber: " + oldTaskNumber);
		if (oldTaskNumber != null && L.getLong(oldTaskNumber) != 0l) {
			ValidateUser validateUser = User.getUser().getValidateUser();
			if (validateUser == null) {
				return null;
			}
			GetEmployeeAndTaskStatusByEmployeeID taskStatus =
					Soap.getSoap().getEmployeeAndTaskStatusByEmployeeID(validateUser.getEmployeeID());
			if (taskStatus == null) {
				L.out("*** ERROR getEmployeeAndTaskStatusByEmployeeID is null! "
						+ taskNumber);
				return null;
			}
			if (checkIfTheTaskCoastIsClear(oldTaskNumber, true, taskStatus)) {
				return createRecord(task, oldTaskNumber, id);
			} else {
				L.out("lastUpdateTaskNumber: " + lastUpdateTaskNumber + " taskStatus.getTaskNumber(): "
						+ taskStatus.getTaskNumber());
				if ((taskStatus.getTaskNumber() != null
						&& taskStatus.getTaskNumber().equals(lastUpdateTaskNumber))
						|| taskStatus.getEmployeeStatus().equals(BreakActivity.AT_LUNCH)
						|| taskStatus.getEmployeeStatus().equals(BreakActivity.ON_BREAK))
					return null;
				return COAST_IS_NOT_CLEAR;
			}
		}
		String updateMethod = null;
		String status = task.getTaskStatusBrief();
		L.out("updateTask status: " + status);

		List<Arg> values = new ArrayList<RestWriter.Arg>();
		values.add(new Arg("FunctionalAreaID", task.getFunctionalArea()));
		values.add(new Arg("TaskNumber", task.getTaskNumber()));
		values.add(new Arg("EmployeeID", employeeID));
		values.add(new Arg("FacilityID", facilityID));
		String mobileUserName = task.getMobileUserName();

		L.out("task.getMobileUserName(): " + task.getMobileUserName());
		values.add(new Arg("UpdatedBy", mobileUserName));

		if (status.equals(TaskActivity.ASSIGNED)) {
		}
		if (status.equals(TaskActivity.ACTIVE)) {
			updateMethod = TASK_START;
		}
		if (status.equals(TaskActivity.DELAYED)) {
			updateMethod = TASK_DELAY;
			L.out("delayed: " + task.getDelayType());
			values.add(new Arg("TskDelayType", task.getDelayType()));
		}
		if (status.equals(TaskActivity.COMPLETED)) {
			// updateMethod = TASK_COMPLETE;
			updateMethod = TASK_COMPLETE_AND_UPDATE_EMPLOYEE_STATUS;
			L.out("completed: " + task.getCompleteTo());
			// values.add(new Arg("AutoAssignCheck", "false"));
			values.add(new Arg("EmployeeCustomStatus", task.getCompleteTo()));
		}
		if (status.equals(TaskActivity.CANCELED)) {
		}
		if (updateMethod == null) {
			L.out("Nothing to do for this status: " + status);
			return SUCCESSFUL;
		}
		L.out("update task");
		for (Arg arg : values) {
			L.out("pair: " + arg.type + " " + arg.value);
		}
		if (NetKiller.kill()) {
			IMActivity.p("NetKiller Fail Update task: " + taskShort(task) + "\n");
			return null;
		}
		IMActivity.p("Update task: " + taskShort(task) + "\n");
		String result = mobileUpdateRecord(BaseSoap.URL + "/" + updateMethod, getNameValuePairs(values));
		// if (TabNavigationActivity.showToasts && false)
		// MyToast.show("finished updateTask: " + task.getTaskNumber() +
		// " to : "
		// + task.getTaskStatusBrief() + " result: " + result);
		// IMActivity.o("finished updateTask: " + task.getTaskNumber() +
		// " to : "
		// + task.getTaskStatusBrief() + " result: " + result);
		if (result == null) {
			IMActivity.p("Fail Update task: " + taskShort(task) + "\n");
		} else {
			lastUpdateTaskNumber = task.getTaskNumber();
			IMActivity.p("Ok" + "\n");
		}
		return result;

	}

	private String createRecord(GetTaskInformationByTaskNumberAndFacilityID task, String oldTaskNumber,
			String id) {
		// ValidateUser validateUser = User.getUser().getValidateUser();
		List<Arg> values = new ArrayList<RestWriter.Arg>();
		String updateMethod = MOBILE_TASK_UPDATE;
		values.add(new Arg("FacilityID", task.getFacilityID()));
		values.add(new Arg("StartLocation", task.getHirStartLocationNode()));
		values.add(new Arg("DestinationLocation", task.getHirDestLocationNode()));
		values.add(new Arg("Notes", task.getNotes()));
		values.add(new Arg("TaskClass", task.getTskTaskClass()));
		values.add(new Arg("RequestorName", task.getRequestorName()));
		values.add(new Arg("RequestorEmail", task.getRequestorEmail()));
		values.add(new Arg("RequestorPhone", task.getRequestorPhone()));
		values.add(new Arg("FunctionalAreaID", TaskActivity.getFunctionalArea()));
		values.add(new Arg("TaskTypeID", "1"));
		values.add(new Arg("UpdatedBy", User.getUser().getUsername()));
		// values.add(new Arg("UpdatedBy", "MobileDevice"));
		values.add(new Arg("AutoAssignCheck", "false"));
		values.add(new Arg("ValidateRooms", "false"));
		values.add(new Arg("EmployeeID", task.getEmployeeID()));
		values.add(new Arg("TaskNumber", ""));
		values.add(new Arg("ModeType", task.getTskModeType()));
		values.add(new Arg("EquipmentType", task.getTaskEquipmentType()));
		values.add(new Arg("FrequencyType", task.getTskFrequencyType()));
		values.add(new Arg("PatientName", task.getPatientName()));
		values.add(new Arg("PatientDOB", task.getPatientDOB()));
		values.add(new Arg("CostCodeID", task.getSteCostCode()));
		values.add(new Arg("PersistDay", ""));
		values.add(new Arg("IsolationPatient", task.getIsolationPatientServer()));
		values.add(new Arg("TaskBrief", task.getTaskBriefServer()));
		// if (addScheduleDate(task))
		// values.add(new Arg("ScheduleDate", task.getScheduleDate()));
		values.add(new Arg("ScheduleDate", ""));
		values.add(new Arg("CcnRequest", task.getCcnRequest()));
		values.add(new Arg("RequestDate", task.getRequestDate()));
		values.add(new Arg("Status", task.getTaskStatusBrief()));
		// values.add(new Arg("Status", BreakActivity.));
		values.add(new Arg("Item", task.getItem()));
		values.add(new Arg("CustomField1", task.getCustomField1()));
		values.add(new Arg("CustomField2", task.getCustomField2()));
		values.add(new Arg("CustomField3", task.getCustomField3()));
		values.add(new Arg("CustomField4", task.getCustomField4()));
		values.add(new Arg("CustomField5", task.getCustomField5()));
		L.out("create task");
		for (Arg arg : values) {
			L.out("pair: " + arg.type + " " + arg.value);
		}
		if (TabNavigationActivity.showToasts)
			MyToast.show("Create Task: " + oldTaskNumber + " Status: "
					+ task.getTaskStatusBrief());
		IMActivity.p("Create Task: " + task.getTaskNumber() + "\n");
		if (NetKiller.kill()) {
			IMActivity.p("NetKiller Fail task: " + task.getTaskNumber() + "\n");
			return null;
		}
		String taskNumber = mobileUpdateRecord(BaseSoap.URL + "/" + updateMethod, getNameValuePairs(values));
		// taskNumber = null;
		if (taskNumber != null && taskNumber.replace(" ", "").length() == 0)
			taskNumber = null;
		if (taskNumber != null)
			IMActivity.p("Ok task: " + taskNumber + "\n");
		L.out("here is the result: #" + taskNumber + "# " + task.getTaskStatusBrief());
		if (taskNumber == null) {
			IMActivity.p("Fail task: " + task.getTaskNumber() + "\n");
			// if (TabNavigationActivity.showToasts)
			// MyToast.show("failed to create task on legacy: " +
			// task.getTskTaskClass());
		}
		if (taskNumber != null) {
			task.setTaskNumber(taskNumber);
			assignTask(task, oldTaskNumber, id);
		}
		return taskNumber;
	}

	private String taskShort(GetTaskInformationByTaskNumberAndFacilityID task) {
		return task.getTaskNumber() + " status: " + task.getTaskStatusBrief();

	}

	private void assignTask(GetTaskInformationByTaskNumberAndFacilityID task, String oldTaskNumber, String id) {
		L.out("assigning task: " + task.getTaskNumber() + " status: "
				+ task.getTaskStatusBrief() + " oldTaskNumber: " + oldTaskNumber);

		ActorController.setTaskInformationByTaskNumberAndFacilityID(task, task.getTaskNumber(), false);
		if (ActorController.task != null) {
			L.out("update Actorcontroller.task: " + ActorController.task.getTaskNumber());
		}
		SoapDbAdapter.getSoapDbAdapter();
		task = SoapDbAdapter.updateTask(id, task);
		if (ActorController.task != null
				&& ActorController.task.getTaskNumber().equals(oldTaskNumber)) {
			// L.out("update Actorcontroller.task: " + ActorController.task);
			ActorController.task = task;
			ActorController.status.setTaskNumber(task.getTaskNumber());
			ActorController.setTaskInformationByTaskNumberAndFacilityID(task, task.getTaskNumber(), false);
			// ActorController.status.setTaskStatus(task.getTaskStatusBrief());
			L.out("update Actorcontroller.status: " + ActorController.status);
			if (User.getUser().getValidateUser() != null)
				User.getUser().getValidateUser().setTaskNumber(task.getTaskNumber());
			ActorController.update(ActorController.status);
		} else if (ActorController.task == null && !task.getTaskStatusBrief().equals(TaskActivity.COMPLETED)) {
			ActorController.task = task;
			ActorController.status.setTaskNumber(task.getTaskNumber());
			ActorController.setTaskInformationByTaskNumberAndFacilityID(task, task.getTaskNumber(), false);
			// ActorController.status.setTaskStatus(task.getTaskStatusBrief());
			L.out("update Actorcontroller.status: " + ActorController.status);
			if (User.getUser().getValidateUser() != null)
				User.getUser().getValidateUser().setTaskNumber(task.getTaskNumber());
			ActorController.update(ActorController.status);
		}
	}

	private List<NameValuePair> getNameValuePairs(List<Arg> values) {
		int count = values.size();
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(count);
		for (Arg arg : values) {
			if (arg.value == null)
				arg.value = "";
			nameValuePairs.add(new BasicNameValuePair(arg.type, arg.value));
			// L.out("pair: " + arg.type + " " + arg.value);
		}
		return nameValuePairs;
	}

	// public String createTask(GetTaskInformationByTaskNumberAndFacilityID
	// task) {
	// // String result = updateTask(getCreateRestURL(task,
	// // UPDATE_TASK_METHOD), task);
	// L.out("result: " + "not implemented");
	// return SUCCESSFUL;
	// }

	private String mobileUpdateRecord(String urlString, List<NameValuePair> nameValuePairs) {
		// L.out(task.toString());
		StringBuilder s = new StringBuilder();
		try {
			HttpClient httpclient = new DefaultHttpClient();

			// L.out("mobileUpdateRecord: " + urlString);
			HttpPost httppost = new HttpPost(urlString);
			httppost.setHeader("Accept", "*/*");
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			// L.out("entity: " + httppost.getEntity().getContent());
			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			String sResponse;

			while ((sResponse = reader.readLine()) != null) {
				s = s.append(sResponse);
			}
			String responseString = s.toString();
			L.out("RESTful Response: " + responseString);
			return getTaskNumber(responseString);
			// return SUCCESSFUL;

		} catch (Exception e) {
			L.out("exception: " + e);
			L.out("stringBuilder: " + s);
			return null;
		}
	}

	private String getTaskNumber(String responseString) {
		// Pure Kludge. Rather than write a parser for EmployeeAndTaskStatus
		// We just grab the TaskNumber out of it. Since this will change, this
		// is temp code!
		int index = responseString.indexOf("TaskNumber") + 12;
		// L.out("index: " + index);
		if (index == -1)
			return null;
		// int jindex = responseString.indexOf("\"", index);
		// L.out("jindex: " + jindex);
		String temp = responseString.substring(index, responseString.indexOf("\"", index));
		L.out("getTaskNumber: " + temp);
		return temp;
	}

	class Arg {
		String type = null;
		String value = null;

		Arg(String title, String value) {
			this.type = title;
			this.value = value;
		}
	}

}
