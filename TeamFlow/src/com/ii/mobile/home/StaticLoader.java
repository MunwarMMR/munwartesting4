package com.ii.mobile.home;

import java.util.Enumeration;
import java.util.Hashtable;

import android.app.Activity;
import android.database.Cursor;

import com.ii.mobile.soap.ParsingSoap;
import com.ii.mobile.util.L;

public class StaticLoader {

	// private static boolean singleTime = false;
	Activity activity;
	public static Hashtable<StaticState, String> hashtable = null;
	public static boolean finished = false;

	String[] soapMethods = new String[] {
			// ParsingSoap.VALIDATE_USER, // 0
			// ParsingSoap.LIST_RECENT_TASKS_BY_EMPLOYEE_ID, // 2
			// ParsingSoap.LIST_ROOMS_BY_FACILITY_ID, // 3
			// old step 3
			// ParsingSoap.GET_CURRENT_TASK_BY_EMPLOYEE_ID, // 4
			ParsingSoap.LIST_TASK_CLASSES_BY_FACILITY_ID, // 7
			ParsingSoap.GET_TASK_DEFINITION_FIELDS_DATA_FOR_SCREEN_BY_FACILITY_ID, // 5
			// ParsingSoap.LIST_ROOMS_BY_FACILITY_ID, // 6

			ParsingSoap.LIST_DELAY_TYPES, // 9
			ParsingSoap.GET_TASK_DEFINITION_FIELDS_FOR_SCREEN_BY_FACILITYID, // 8

	};

	class StaticState {
		String methodName;
		Cursor cursor = null;
		String facilityID = null;

		public StaticState(String methodName) {
			this.methodName = methodName;
		}

		@Override
		public String toString() {
			return "StaticState: " + methodName + " cursor: " + cursor + " " + facilityID;
		}
	}

	public StaticLoader(Activity activity) {
		this.activity = activity;
		getHashTable();
	}

	private void printTable() {
		Hashtable<StaticState, String> hashtable = getHashTable();
		Enumeration<StaticState> elements = hashtable.keys();
		while (elements.hasMoreElements()) {
			StaticState foo = elements.nextElement();
			L.out("foo: " + foo);
		}
	}

	private Hashtable<StaticState, String> getHashTable() {
		if (hashtable != null)
			return hashtable;
		hashtable = new Hashtable<StaticState, String>();
		for (int i = 0; i < soapMethods.length; i++) {
			// L.out("soapMethods: " + soapMethods[i]);
			hashtable.put(new StaticState(soapMethods[i]), soapMethods[i]);
		}
		return hashtable;
	}

	public static void initDataCache() {
		hashtable = null;
		finished = false;
	}

}
