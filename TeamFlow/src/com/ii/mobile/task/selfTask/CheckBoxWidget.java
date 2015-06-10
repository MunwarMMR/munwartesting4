package com.ii.mobile.task.selfTask;

import android.content.ContentValues;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.TableRow;

import com.ii.mobile.soap.gson.GetTaskDefinitionFieldsForScreenByFacilityID.Field;
import com.ii.mobile.util.L;

public class CheckBoxWidget extends BaseField {

	private CheckBox checkBox;

	public CheckBoxWidget(SelfTaskActivity selfTaskActivity, Field field) {
		super(selfTaskActivity, field);
	}

	@Override
	public CheckBox createValueView() {
		checkBox = new CheckBox(activity);

		TableRow.LayoutParams layoutParams = new
				TableRow.LayoutParams(
						TableRow.LayoutParams.MATCH_PARENT,
						TableRow.LayoutParams.MATCH_PARENT);

		// layoutParams.setMargins(0, 0, 10, 0);
		checkBox.setLayoutParams(layoutParams);
		// layoutParams = (LayoutParams) editText.getLayoutParams();
		// layoutParams.setMargins(20, 0, 40, 0);
		// checkBox.setHint(field.customHeader);
		checkBox.setTextSize(15);
		checkBox.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

		if (field.required.equals("1"))
			checkBox.setHintTextColor(Color.parseColor("#FFAAAA"));
		else
			checkBox.setHintTextColor(Color.parseColor("#AAAAAA"));

		String result = activity.task.getNamedValue(field.customHeader);
		// L.out("result: " + result);
		checkBox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (((CheckBox) v).isChecked()) {
					L.out("value set to: " + getValue());
				}
			}

		});

		return checkBox;
	}

	@Override
	public boolean validate() {
		// L.out("validate foo: " + foobar + " edit: " + editText);
		if (checkBox == null) {
			L.out("checkBox is null for " + field.toString());
			return true;
		}

		if (field.required.equals("1")) {
			if (true) {
				titleView.setTextColor(Color.parseColor(REQUIRED_PRESENT));
				return false;
			}
		} else
			titleView.setTextColor(Color.parseColor(OPTIONAL));

		// setValue();

		return true;
	}

	@Override
	public ContentValues addValue(ContentValues contentValues) {
		String text = getValue();
		if (text != null && !text.equals("")) {
			// L.out("text: " + text);
			contentValues.put(field.controlName, text);
		}
		return contentValues;
	}

	@Override
	public ContentValues addFailValue(ContentValues contentValues) {
		if (field.required.equals("1")) {
			String text = checkBox.getText().toString();
			if (text == null || text.equals("")) {
				// L.out("text: " + text);
				contentValues.put(field.controlName, "Not Entered!");
			}
		}
		return contentValues;
	}

	@Override
	public String getValue() {
		if (checkBox.isChecked())
			return "Yes";
		return "No";

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(field.controlName, getValue());
	}

	// public String getTaskValue() {
	// // L.out("field: " + field);
	// // L.out("task: " + task);
	// String text =
	// SelfActionFragment.getActionStatus.getNamedValue(field.controlName);
	// return text;
	// }

	private void setState(String text) {
		if (text != null && text.equals("Yes"))
			checkBox.setChecked(true);
		else
			checkBox.setChecked(false);
	}

	@Override
	public void onRestoreInstanceState(Bundle outState) {
		String text = outState.getString(field.controlName);
		// L.out("text: " + text);
		setState(text);
		// checkBox.setText(outState.getString(field.customHeader));
	}
}
