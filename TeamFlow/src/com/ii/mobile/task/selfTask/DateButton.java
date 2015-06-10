package com.ii.mobile.task.selfTask;

import android.content.Context;
import android.graphics.Color;
import android.widget.Button;

public class DateButton extends Button {

	public DateButton(Context context) {
		super(context);
		setTextSize(20);
		setTextColor(Color.parseColor("#000000"));
	}

	public void update(int year, int month, int day, DatePickerField datePickerField) {
		// L.out("date: " + month + 1 + "/" + day + "/" + year + L.p());
		if (datePickerField.edited)
			setText(getFlowDate(year, month, day));
		else
			setText("None");
	}

	private String getFlowDate(int year, int month, int day) {
		// L.out("getFlowDate: " + (month + 1) + "/" + day + "/" + year);
		return (month + 1) + "/" + day + "/" + year;
		// SimpleDateFormat originalFormat = new SimpleDateFormat("mm/dd/yyyy");
		// SimpleDateFormat targetFormat = new SimpleDateFormat("mm-dd-yyyy");
		// Date date;
		// String text = (month + 1) + "/" + day + "/" + year;
		// String temp = "";
		// try {
		// date = originalFormat.parse(text);
		// System.out.println("Old Format :   " + originalFormat.format(date));
		// System.out.println("New Format :   " + targetFormat.format(date));
		// temp = targetFormat.format(date);
		//
		// } catch (ParseException ex) {
		//
		// }
		// return temp;
	}

}
