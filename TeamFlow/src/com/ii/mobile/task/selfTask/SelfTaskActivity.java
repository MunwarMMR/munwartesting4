package com.ii.mobile.task.selfTask;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import com.ii.mobile.R;
import com.ii.mobile.home.LoginActivity;
import com.ii.mobile.home.UserWatcher;
import com.ii.mobile.soap.ParsingSoap;
import com.ii.mobile.soap.gson.GetTaskDefinitionFieldsForScreenByFacilityID;
import com.ii.mobile.soap.gson.GetTaskDefinitionFieldsForScreenByFacilityID.Field;
import com.ii.mobile.soap.gson.GetTaskInformationByTaskNumberAndFacilityID;
import com.ii.mobile.soap.gson.ListTaskClassesByFacilityID;
import com.ii.mobile.soap.gson.ListTaskClassesByFacilityID.TaskClass;
import com.ii.mobile.soap.gson.ValidateUser;
import com.ii.mobile.tab.TaskActivity;
import com.ii.mobile.users.User;
import com.ii.mobile.util.L;

public class SelfTaskActivity extends FragmentActivity implements
		AdapterView.OnItemClickListener, OnItemSelectedListener {

	// static private final String FUNCTIONAL_AREA = "1";
	private static TaskClass[] taskClass;

	private static Hashtable<String, Field[]> classFieldHashtable = null;
	GetTaskInformationByTaskNumberAndFacilityID task;
	private boolean resetting = false;

	private Bundle outState = null;
	protected static boolean isVisible = false;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.self_task);

		// selection = (TextView) findViewById(R.id.selection);

		Spinner spin = (Spinner) findViewById(R.id.spinner);
		spin.setOnItemSelectedListener(this);

		// get the classes of self-tasks
		TaskClass[] taskClass = getTaskClasses();

		ArrayAdapter<String> arrayAdapter =
				new ArrayAdapter<String>(this, R.layout.list_item, getDisplayItems(taskClass));

		arrayAdapter.setDropDownViewResource(R.layout.list_item);
		spin.setAdapter(arrayAdapter);
		// spin.requestFocus();
	}

	private String[] getDisplayItems(TaskClass[] taskClass) {
		String[] items = new String[taskClass.length];
		for (int i = 0; i < taskClass.length; i++) {
			items[i] = taskClass[i].brief;
			// L.out(i + " item: " + items[i]);
		}
		return items;
	}

	@Override
	protected void onPause() {
		super.onPause();
		L.out("onPause");
		isVisible = false;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		Spinner spin = (Spinner) findViewById(R.id.spinner);

		outState.putString("TaskClass", spin.getSelectedItemPosition() + "");
		List<BaseField> baseFields = BaseField.getBaseFieldList();
		if (baseFields != null)
			for (BaseField baseField : baseFields) {
				baseField.onSaveInstanceState(outState);
				// L.out("contentValue: " + contentValues.size());
			}
	}

	@Override
	protected void onResume() {
		super.onResume();
		L.out("onResume");
		isVisible = true;
		UserWatcher.INSTANCE.stop();
	}

	public static boolean isVisible() {
		return isVisible;
	}

	@Override
	protected void onRestoreInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		this.outState = outState;
		Spinner spin = (Spinner) findViewById(R.id.spinner);
		int itemPosition = (int) L.getLong(outState.getString("TaskClass"));
		L.out("itemPosition: " + itemPosition);
		resetting = true;
		spin.setSelection(itemPosition);
	}

	private TaskClass[] getTaskClasses() {
		if (taskClass != null)
			return taskClass;

		String json = LoginActivity.getJSon(ParsingSoap.LIST_TASK_CLASSES_BY_FACILITY_ID, this);
		if (json != null) {
			ListTaskClassesByFacilityID listTaskClassesByFacilityID = ListTaskClassesByFacilityID.getGJon(json);
			// L.out("listTaskClassesByFacilityID: " +
			// listTaskClassesByFacilityID);
			taskClass = listTaskClassesByFacilityID.getTaskClasses(TaskActivity.getFunctionalArea());
			for (int i = 0; i < taskClass.length; i++) {
				L.out("Class: " + taskClass[i]);
			}
			return taskClass;
		}
		return null;
	}

	public static void initDataCache() {
		classFieldHashtable = null;
		taskClass = null;
	}

	private Field[] getTaskField(String classID) {
		if (classFieldHashtable == null)
			classFieldHashtable = new Hashtable<String, Field[]>();
		Field[] field = classFieldHashtable.get(classID);
		if (field != null)
			return field;

		String json = LoginActivity.getJSon(ParsingSoap.GET_TASK_DEFINITION_FIELDS_FOR_SCREEN_BY_FACILITYID, this);
		if (json != null) {
			GetTaskDefinitionFieldsForScreenByFacilityID getTaskDefinitionFieldsForScreenByFacilityID = GetTaskDefinitionFieldsForScreenByFacilityID.getGJon(json);
			// L.out("getTaskDefinitionFieldsForScreenByFacilityID: "
			// + getTaskDefinitionFieldsForScreenByFacilityID);
			field = getTaskDefinitionFieldsForScreenByFacilityID.getClassField(TaskActivity.getFunctionalArea(), classID);
			field = addExtraFields(field);
			classFieldHashtable.put(classID, field);
			// printField(field);
			return field;
		}
		return null;
	}

	private Field[] addExtraFields(Field[] field) {
		Field[] tmp = new Field[field.length + 1];
		for (int i = 0; i < field.length; i++) {
			tmp[i] = field[i];
		}
		// tmp[field.length] = makeField("CheckBox", "chkStatus", "Stat");
		tmp[field.length] = makeField("Text", "txtNotes", "Notes");
		return tmp;
	}

	private Field makeField(String controlType, String controlName, String customHeader) {
		Field field = new Field();
		field.controlType = controlType;
		field.controlName = controlName;
		field.customHeader = customHeader;
		field.header = customHeader;
		field.required = "0";
		return field;
	}

	@SuppressWarnings("unused")
	private void printField(Field[] field) {
		for (int i = 0; i < field.length; i++) {
			L.out(i + " field: " + field[i]);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long
			id) {
		// Intent intent = new Intent().setClass(this,
		// AdmissionsTaskActivity.class);
		// startActivity(intent);
		L.out("onItemCLICK????");
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
		TextView view = (TextView) arg1;
		L.out("position: " + position + " " + resetting);
		if (view != null)
			L.out("selected: " + view.getText() + " " + position + " " + arg3);
		generateFieldView(getTaskClasses()[position]);
	}

	private void generateFieldView(TaskClass taskClassSelected) {
		L.out("taskClassSelected: " + taskClassSelected);

		String taskClassID = taskClassSelected.taskClassID;
		initTask(taskClassID, taskClassSelected.brief);
		Field[] field = getTaskField(taskClassID);
		// printField(field);
		List<BaseField> baseFields = BaseField.getViews(this, field);
		TableLayout tableLayout = (TableLayout) findViewById(R.id.topLevel);
		tableLayout.removeAllViews();
		int count = 0;
		for (BaseField baseField : baseFields) {
			L.out(count++ + ": " + baseField.field.toStringShort());

			tableLayout.addView(baseField.getFieldView());
		}
		// List<BaseField> baseFields = BaseField.getBaseFieldList();

		L.out("test baseFields: " + baseFields.size());
		if (outState != null) {
			String position = outState.getString("TaskClass");
			if (position != null) {
				int itemPosition = (int) L.getLong(position);
				L.out("test itemPosition: " + itemPosition);
			}
			for (BaseField baseField : baseFields) {
				baseField.onRestoreInstanceState(outState);
			}
			if (baseFields.size() > 0) {
				baseFields.get(0).validateAll();
			}
		}
		resetting = false;
	}

	private void initTask(String taskClassID, String classBrief) {
		task = new GetTaskInformationByTaskNumberAndFacilityID();
		User user = User.getUser();
		ValidateUser validateUser = user.getValidateUser();
		task.setEmployeeID(validateUser.getEmployeeID());
		task.setFacilityID(validateUser.getFacilityID());
		task.setTskTaskClass(taskClassID);
		task.setClassBrief(classBrief);
		task.setRequestorName(user.getUsername());
		task.setRequestorPhone("123456789");
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		L.out("nothing: " + arg0);
	}

	// public GetEmployeeAndTaskStatusByEmployeeID getTask() {
	// GetEmployeeAndTaskStatusByEmployeeID task = new
	// GetEmployeeAndTaskStatusByEmployeeID();
	// List<BaseField> baseFields = BaseField.getBaseFieldList();
	// for (BaseField baseField : baseFields) {
	// baseField.setValue(task);
	// }
	// return task;
	// }

	public void submitButtonClick(View view) {
		List<BaseField> baseFields = BaseField.getBaseFieldList();

		for (BaseField baseField : baseFields) {
			baseField.setValue(task);
			// L.out("contentValue: " + contentValues.size());
		}
		createRecord(task);

		L.out("submit task: " + task);
		ContentValues contentValues = new ContentValues();
		if (BaseField.validated) {

			for (BaseField baseField : baseFields) {
				baseField.addValue(contentValues);
				// L.out("contentValue: " + contentValues.size());
			}
			String temp = "Created Task: ";
			Set<Entry<String, Object>> valueSet = contentValues.valueSet();
			// L.out("valuseSet: " + valueSet.size());
			Iterator<Entry<String, Object>> iter = valueSet.iterator();
			while (iter.hasNext()) {
				Entry<?, ?> entry = iter.next();
				temp += "\n" + entry.getKey() + ": " + entry.getValue();
				// L.out("Entry: " + entry.getKey() + " " + entry.getValue());
			}
			ValidateUser validateUser = User.getUser().getValidateUser();
			// if (validateUser.getEmployeeStatus() == BreakActivity.AT_LUNCH
			// || validateUser.getEmployeeStatus() == BreakActivity.ON_BREAK) {
			// // validateUser.setEmployeeStatus(BreakActivity.AVAILABLE);
			// // TaskActivity.setEmployeeStatus(BreakActivity.AVAILABLE,
			// // false, this);
			// MyToast.show("Unable to create task while on break!");
			// return;
			// }
			// MyToast.show(temp, Toast.LENGTH_LONG);
			// IMActivity.p("Self Task: " + task.getTaskNumber());
			TaskActivity.createSelfTask(task);
			finish();
		}
		else {
			for (BaseField baseField : baseFields) {
				baseField.addFailValue(contentValues);
			}
			// String temp = "Invalid Action:";
			String temp = "";
			Set<Entry<String, Object>> valueSet = contentValues.valueSet();
			// L.out("valuseSet: " + valueSet.size());
			Iterator<Entry<String, Object>> iter = valueSet.iterator();
			while (iter.hasNext()) {
				Entry<?, ?> entry = iter.next();
				temp += "\n" + entry.getKey() + ": " + entry.getValue();
				// L.out("Entry: " + entry.getKey() + " " + entry.getValue());
			}
			// MyToast.show(temp, Toast.LENGTH_LONG);
			showDialog(temp);
		}
	}

	private void showDialog(String msg) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set title
		alertDialogBuilder.setTitle("Invalid Action");

		// set dialog message
		alertDialogBuilder
				.setMessage(msg)
				.setCancelable(false)
				.setNeutralButton("Ok", null);

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	private void createRecord(GetTaskInformationByTaskNumberAndFacilityID task) {
		// ValidateUser validateUser = User.getUser().getValidateUser();
		List<Arg> values = new ArrayList<Arg>();

		values.add(new Arg("FacilityID", task.getFacilityID()));
		values.add(new Arg("StartLocation", task.getHirStartLocationNode()));
		values.add(new Arg("DestinationLocation", task.getHirDestLocationNode()));
		values.add(new Arg("Notes", task.getNotes()));
		values.add(new Arg("TaskClass", task.getTskTaskClass()));
		values.add(new Arg("RequestorName", task.getRequestorName()));
		values.add(new Arg("RequestorEmail", task.getRequestorEmail()));
		values.add(new Arg("RequestorPhone", task.getRequestorPhone()));
		values.add(new Arg("FunctionalAreaID", "1"));
		values.add(new Arg("TaskTypeID", "1"));
		values.add(new Arg("UpdatedBy", User.getUser().getUsername()));
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
		// values.add(new Arg("ScheduleDate", task.getScheduleDate()));
		values.add(new Arg("ScheduleDate", ""));
		values.add(new Arg("CcnRequest", task.getCcnRequest()));
		values.add(new Arg("RequestDate", task.getRequestDate()));
		values.add(new Arg("Status", task.getTaskStatusBrief()));
		values.add(new Arg("Item", task.getItem()));
		values.add(new Arg("CustomField1", task.getCustomField1()));
		values.add(new Arg("CustomField2", task.getCustomField2()));
		values.add(new Arg("CustomField3", task.getCustomField3()));
		values.add(new Arg("CustomField4", task.getCustomField4()));
		values.add(new Arg("CustomField5", task.getCustomField5()));

		for (Arg arg : values) {
			L.out("pair: " + arg.type + " " + arg.value);
		}
	}

}

class Arg {
	String type = null;
	String value = null;

	Arg(String title, String value) {
		this.type = title;
		this.value = value;
	}
}
