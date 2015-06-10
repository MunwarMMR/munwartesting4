package com.ii.mobile.task.selfTask;

import android.content.ContentValues;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TableRow;

import com.ii.mobile.soap.gson.GetTaskDefinitionFieldsForScreenByFacilityID.Field;
import com.ii.mobile.util.L;

public class TextField extends BaseField {

	public Editable lastString;
	public String foobar = "no way!";

	public TextField(SelfTaskActivity activity, Field field) {
		super(activity, field);
	}

	@Override
	EditText createValueView() {
		editText = new EditText(activity);
		foobar = "yahoo";
		TableRow.LayoutParams layoutParams = new
				TableRow.LayoutParams(
						TableRow.LayoutParams.MATCH_PARENT,
						TableRow.LayoutParams.MATCH_PARENT);

		// layoutParams.setMargins(0, 0, 10, 0);
		editText.setLayoutParams(layoutParams);
		// layoutParams = (LayoutParams) editText.getLayoutParams();
		// layoutParams.setMargins(20, 0, 40, 0);
		//		editText.setHint(field.customHeader);
		
		if(field.header.contains("Custom"))
			editText.setHint(field.customHeader);
		else
			editText.setHint(field.header);
		if (!field.customHeader.startsWith("Note"))
			editText.setSingleLine();
		else {
			editText.setLines(5);
			editText.setGravity(Gravity.TOP | Gravity.LEFT);
		}
		editText.setTextSize(15);
		editText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
		if (field.required.equals("1"))
			editText.setHintTextColor(Color.parseColor("#FFAAAA"));
		else
			editText.setHintTextColor(Color.parseColor("#AAAAAA"));
		editText.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				// validate();
				String newText = editText.getText().toString();
				// L.out("lastString: " + lastString);
				// L.out("newText: " + newText);
				if (field.required.equals("1")) {
					if (!lastString.equals(newText))
						validateAll();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				lastString = editText.getText();
				// L.out("lastString: " + lastString);
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// intentionally left blank
			}

		});
		// L.out("edittext: " + editText + " " + field.toStringShort());
		validateAll();
		return editText;
	}

	@Override
	protected boolean validate() {
		// L.out("validate foo: " + foobar + " edit: " + editText);
		if (editText == null) {
			L.out("editText is null for " + field.toStringShort());
			return true;
		}

		Editable foo = editText.getText();
		String text = foo.toString();
		// L.out("validate textField: #" + text + "#" + text.length());

		if (field.required.equals("1")) {
			if (text != null && text.equals("")) {
				titleView.setTextColor(Color.parseColor(REQUIRED));
				return false;
			}
			else
				titleView.setTextColor(Color.parseColor(REQUIRED_PRESENT));

		} else
			titleView.setTextColor(Color.parseColor(OPTIONAL));

		return true;
	}

	@Override
	ContentValues addValue(ContentValues contentValues) {
		String text = editText.getText().toString();
		if (text != null && !text.equals("")) {
			// L.out("text: " + text);
			contentValues.put(field.controlName, text);
		}
		return contentValues;
	}

	@Override
	ContentValues addFailValue(ContentValues contentValues) {
		if (field.required.equals("1")) {
			String text = editText.getText().toString();
			if (text == null || text.equals("")) {
				// L.out("text: " + text);
				contentValues.put(field.customHeader, "Not Entered!");
			}
		}
		return contentValues;
	}

	@Override
	public String getValue() {
		String text = editText.getText().toString();
		return text;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(field.customHeader, getValue());
	}

	@Override
	void onRestoreInstanceState(Bundle outState) {
		String text = outState.getString(field.customHeader);
		L.out("text: " + text);
		editText.setText(outState.getString(field.customHeader));
	}
}
