package com.ii.mobile.task.selfTask;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.ii.mobile.R; // same package // same package
import com.ii.mobile.soap.gson.GetTaskDefinitionFieldsForScreenByFacilityID.Field;
import com.ii.mobile.soap.gson.GetTaskInformationByTaskNumberAndFacilityID;
import com.ii.mobile.tab.PickField;
import com.ii.mobile.util.L;

abstract public class BaseField extends PickField {

	public static final String TEXT = "Text";
	public static final String TYPE_AHEAD = "TypeAhead";
	public static final String PICK_LIST = "PickList";
	public static final String DATE_TIME_SPLIT = "DateTimeSplit";
	public static final String PATIENT_DOB = "TxtPatientDOB";
	public static final String DROP_MODE_ENTRY = "DrpModeEntry";
	public static final String CHECK_BOX = "CheckBox";

	protected static final String REQUIRED = "#CC0000";
	protected static final String REQUIRED_PRESENT = "#CC9999";
	protected static final String OPTIONAL = "#000000";
	protected static final String VALID_BUTTON_COLOR = "#00AA00";

	private static ArrayList<BaseField> baseFieldList;
	private static boolean inited = false;
	public static boolean validated = false;

	protected String lastPick = null;

	// amazing that need to put here. Not initialize in super and then
	// initialized after to default! Since call createFieldLayout here, the
	// side-effects go away!
	protected EditText editText = null;
	protected int month = 0;
	protected int day = 1;
	protected int year = 1970;
	protected Spinner spinner;
	protected DatePickerFragment newFragment = null;
	protected AutoComplete autoComplete;
	boolean edited = false;

	protected SelfTaskActivity activity;

	private final TableRow fieldLayout;
	protected final Field field;
	protected TextView titleView;

	public BaseField(SelfTaskActivity activity, Field field) {
		super(activity);
		this.activity = activity;
		this.field = field;
		fieldLayout = createFieldLayout();
		validate();
	}

	private TextView createTitleView() {
		titleView = new TextView(activity);
		LayoutParams layoutParams =
				new TableRow.LayoutParams(
						TableRow.LayoutParams.WRAP_CONTENT,
						TableRow.LayoutParams.MATCH_PARENT);
		titleView.setGravity(Gravity.CENTER_VERTICAL);
		titleView.setLayoutParams(layoutParams);
		titleView.setTextSize(15);
		titleView.setTextColor(Color.parseColor("#000000"));
		if(field.header.contains("Custom"))
			titleView.setText(field.customHeader);
		else
			titleView.setText(field.header);
		return titleView;
	}

	private TableRow createFieldLayout() {
		TableRow tableRow = new TableRow(activity);
		LayoutParams layoutParams =
				new TableRow.LayoutParams(
						TableRow.LayoutParams.MATCH_PARENT,
						TableRow.LayoutParams.MATCH_PARENT, 1.0f);
		tableRow.setLayoutParams(layoutParams);
		tableRow.addView(createTitleView());
		@SuppressWarnings("unused")
		View foo = createValueView();
		tableRow.addView(createValueView());
		return tableRow;
	}

	public void validateAll() {
		// L.out("inited: " + inited);
		if (!inited) {
			return;
		}
		// int count = 0;
		validated = true;
		if (baseFieldList == null)
			return;
		for (BaseField baseField : baseFieldList) {
			boolean valid = baseField.validate();
			// L.out((count++) + ": valid: " + valid + " BaseField: " +
			// baseField.getClass() + " "
			// + baseField.field.toStringShort());
			if (!valid)
				validated = false;
		}
		updateSubmitButton();
		// L.out("valididate valid: " + validated + " " + baseFieldList.size());
	}

	private void updateSubmitButton() {
		Button button = (Button) activity.findViewById(R.id.submitButton);

		if (validated)
			button.setTextColor(Color.parseColor(VALID_BUTTON_COLOR));
		else
			button.setTextColor(Color.parseColor(REQUIRED));
	}

	public static List<BaseField> getViews(SelfTaskActivity activity, Field[] fields) {

		baseFieldList = new ArrayList<BaseField>();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			// L.out(i + " field: " + field.customHeader + " controlType: " +
			// field.controlType);
			// L.out(i + " field: " + field);
			String controlType = field.controlType;
			String controlName = field.controlName;
			// L.out("controlType: " + controlType);
			if (field.customHeader == null) {
				L.out("customHeader is null! " + field.toString());
				field.customHeader = "Persist";
			}
			if (field.customHeader.equals("Persist")) {
			}
			else if (controlType.equals(PICK_LIST) || controlType.equals(DROP_MODE_ENTRY)) {
				baseFieldList.add(new PickListField(activity, field));
				// notice different test. Use a better widget than text for DOB
			} else if (controlName.equals(PATIENT_DOB)) {
				baseFieldList.add(new DatePickerField(activity, field));
			} else if (controlType.equals(TEXT)) {
				baseFieldList.add(new TextField(activity, field));
			} else if (controlType.equals(TYPE_AHEAD)) {
				baseFieldList.add(new TypeAheadField(activity, field));
			} else if (controlType.equals(DATE_TIME_SPLIT)) {
				// L.out("date time ignored: " + field);
			} else if (controlType.equals(CHECK_BOX)) {
				baseFieldList.add(new CheckBoxWidget(activity, field));
				// L.out("date time ignored: " + field);
			} else {
				L.out("*** ERROR in controlType: " + controlType);
			}
		}
		inited = true;
		if (baseFieldList.size() > 1)
			baseFieldList.get(0).validateAll();

		return baseFieldList;
	}

	public static List<BaseField> getBaseFieldList() {
		return baseFieldList;
	}

	abstract View createValueView();

	abstract ContentValues addValue(ContentValues contentValues);

	abstract ContentValues addFailValue(ContentValues contentValues);

	protected abstract boolean validate();

	public View getFieldView() {
		return fieldLayout;
	}

	@Override
	public String toString() {
		return "base field: " + field.customHeader + " controlType: " + field.controlType;
	}

	public void setValue(GetTaskInformationByTaskNumberAndFacilityID task) {
		task.setNamedValue(field.header, getValue());
		setSideEffect(task);
	}

	protected void setSideEffect(GetTaskInformationByTaskNumberAndFacilityID task) {
	}

	abstract public String getValue();

	public abstract void onSaveInstanceState(Bundle outState);

	abstract void onRestoreInstanceState(Bundle outState);

}
