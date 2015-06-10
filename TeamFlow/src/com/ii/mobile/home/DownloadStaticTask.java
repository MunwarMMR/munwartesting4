package com.ii.mobile.home;

import java.util.Enumeration;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import com.ii.mobile.home.StaticLoader.StaticState;
import com.ii.mobile.soap.StaticSoap;
import com.ii.mobile.users.User;
import com.ii.mobile.util.L;

class DownloadStaticTask extends AsyncTask<Void, Integer, Long> {
	private final long LOADING = 0;
	private final long ERROR = 1;
	private final long LOADED_ALREADY = 2;
	private ProgressDialog progressDialog = null;
	private final StaticLoaderCallBack staticLoaderCallBack;
	private final Activity activity;

	public DownloadStaticTask(Activity activity, StaticLoaderCallBack staticLoaderCallBack) {
		this.staticLoaderCallBack = staticLoaderCallBack;
		this.activity = activity;
	}

	@Override
	protected Long doInBackground(Void... arg0) {
		Thread.currentThread().setName("StaticLoaderThread");
		// printTable();
		long count = LOADING;
		Enumeration<StaticState> e = StaticLoader.hashtable.keys();

		while (e.hasMoreElements()) {
			StaticState staticState = e.nextElement();
			// L.out("staticState: " + staticState);
			String facilityID = User.getUser().getFacilityID();
			if (staticState.cursor == null
					|| staticState.facilityID == null
					|| facilityID == null
					|| !facilityID.equals(staticState.facilityID)) {
				staticState.cursor = getCursor(staticState.methodName, null);
				staticState.facilityID = facilityID;
				// if (Math.random() > .55) {
				// L.out("failure inserted for: " + staticState);
				// staticState.cursor = null;
				// }
				if (staticState.cursor == null || staticState.cursor.getCount() == 0) {
					L.out("Failed to load: " + staticState.methodName);
					count = ERROR;
				}

			} else {
				// L.out("already have: " + staticState);
				// count = LOADED_ALREADY;
			}
		}

		// MyToast.show("...Loaded static data");
		// printTable();
		L.out("count: " + count);
		return count;
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		L.out("probably not used but executes in UI thread");
	}

	@Override
	protected void onPreExecute() {
		new StaticLoader(activity);
		if (missingAny() || true) {
			// MyToast.show("Loading static content ...");
			progressDialog = new ProgressDialog(activity);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage("Loading of static content ...");
			progressDialog.setCancelable(false);
			progressDialog.show();

		}
	}

	private boolean missingAny() {
		Enumeration<StaticState> e = StaticLoader.hashtable.keys();
		while (e.hasMoreElements()) {
			StaticState staticState = e.nextElement();
			if (staticState.cursor == null)
				return true;
		}
		L.out("not missing any!");
		return false;
	}

	@Override
	protected void onPostExecute(Long l) {
		if (progressDialog != null) {
			try {
				progressDialog.dismiss();
			} catch (Exception e) {
				L.out("dismissed exception: " + e);
			}
		}
		if (l == LOADING) {
			// MyToast.show("... Loaded static content  ");
			StaticLoader.finished = true;
			staticLoaderCallBack.staticLoaderSuccess();
		}
		if (l == ERROR) {
			staticLoaderCallBack.staticLoaderFail();

		}
		if (l == LOADED_ALREADY) {
			staticLoaderCallBack.staticLoaderSuccess();
		}

	}

	public Cursor getCursor(String methodName, String taskNumber) {
		// L.out("getCursor: " + methodName);

		String facilityID = User.getUser().getFacilityID();
		String employeeID = User.getUser().getEmployeeID();
		// L.out("facilityID: " + facilityID);
		// L.out("employeeID: " + employeeID);
		if (User.getUser().getValidateUser() == null) {
			// L.out("intentional crash!");
			// int i = 100 / 0;
			return null;
		}
		Intent intent = activity.getIntent();
		// if
		// (methodName.equals(ParsingSoap.GET_TASK_DEFINITION_FIELDS_DATA_FOR_SCREEN_BY_FACILITY_ID)
		// ||
		// methodName.equals(ParsingSoap.GET_TASK_DEFINITION_FIELDS_FOR_SCREEN_BY_FACILITYID)
		// || methodName.equals(ParsingSoap.LIST_TASK_CLASSES_BY_FACILITY_ID)
		// || methodName.equals(ParsingSoap.LIST_DELAY_TYPES))
		// employeeID = null;
		String[] selectionArgs = new String[] { employeeID, facilityID, taskNumber };
		intent.setData(Uri.parse("content://" + StaticSoap.AUTHORITY + "/" +
				methodName));
		Cursor cursor = activity.managedQuery(activity.getIntent().getData(), null, null, selectionArgs, null);
		if (cursor != null && cursor.getCount() > 0)
			return cursor;
		return null;
	}

}