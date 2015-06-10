/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ii.mobile.soap;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.ii.mobile.database.AbstractDbAdapter;
import com.ii.mobile.database.NetworkUploader;
import com.ii.mobile.home.MyToast;
//import com.ii.mobile.model.TaskModel;
import com.ii.mobile.soap.StaticSoap.StaticSoapColumns;
import com.ii.mobile.soap.gson.GJon;
import com.ii.mobile.soap.gson.GetTaskInformationByTaskNumberAndFacilityID;
import com.ii.mobile.soap.gson.ValidateUser;
import com.ii.mobile.tab.IMActivity;
import com.ii.mobile.tab.TabNavigationActivity;
import com.ii.mobile.tab.TaskActivity;
import com.ii.mobile.tickle.Tickler;
import com.ii.mobile.users.User;
import com.ii.mobile.util.L;
import com.ii.mobile.util.SecurityUtils;

public class SoapDbAdapter extends AbstractDbAdapter {
	public static SoapDbAdapter soapDbAdapter;

	// public static TaskModel taskModel;

	public static SoapDbAdapter getSoapDbAdapter() {
		return soapDbAdapter;
	}

	public final static boolean WANT_SECURITY = true;
	public final static boolean WANT_SECURITY_DEBUG = false;

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param context
	 *            the Context within which to work
	 */
	public SoapDbAdapter(Context context) {
		super(context);
		setTableName(TABLE_SOAP);
		// L.out("created: " + this);
		SoapDbAdapter.setSoapDbAdapter(this);
		// taskModel = new TaskModel(this);
	}

	public int somethingToUpdate() {
		L.out("somethingToUpdate");
		SQLiteDatabase db = getWritableDatabase();
		if (db == null) {
			L.out("db: " + db);
			return 0;
		}

		String where = AbstractDbAdapter.TIME_STAMP + " is not null";
		// L.out("where: " + where);
		Cursor cursor = db.query(getTableName(), null, where, null, null,
				null, TIME_STAMP + " ASC");
		// // L.out("raw c: " + c.getCount());
		// if (c.getCount() > 0) {
		// L.out("count to update: " + cursor.getCount());
		// }
		return cursor.getCount();

	}

	public long create(String soapMethod, String json, String employeeID, String facilityID, String taskNumber) {
		ContentValues values = new ContentValues();
		values.put(StaticSoapColumns.SOAP_METHOD, soapMethod);
		values.put(StaticSoapColumns.EMPLOYEE_ID, employeeID);
		values.put(StaticSoapColumns.FACILITY_ID, facilityID);
		values.put(StaticSoapColumns.TASK_NUMBER, taskNumber);
		// L.out("create values: " + values);
		// MyToast.show("create: " + soapMethod + " " + employeeID + " " +
		// facilityID + " " + taskNumber);
		values.put(StaticSoapColumns.JSON, json);
		if (soapMethod.equals(ParsingSoap.VALIDATE_USER))
			values.put(TICKLED, GJon.TRUE_STRING);
		else
			values.put(TICKLED, GJon.TRUE_STRING);
		// if (WANT_SECURITY && false) {
		// out("before create json: " + json);
		// json = SecurityUtils.encryptAES(User.getUser().getPassword(), json);
		// values.put(StaticSoapColumns.JSON, json);
		// out("after create json: " + json);
		// if (WANT_SECURITY_DEBUG)
		// out("test : " +
		// SecurityUtils.decryptAES(User.getUser().getPassword(), json));
		// }
		long result = update(values, getWhere(soapMethod, employeeID, facilityID, taskNumber));
		// long result = getDB().create(getTableName(), null, values);
		// L.out("result: " + result);
		// constantly resetting this. Stop the output for this method
		// if (!soapMethod.equals(ParsingSoap.GET_CURRENT_TASK_BY_EMPLOYEE_ID))
		// showEvents(getTableName());
		// This one for when inserting
		// showEvents(getTableName());
		if (networkUploader != null) {
			networkUploader.onResume();
		}
		return result;
	}

	public static void out(String output) {
		if (WANT_SECURITY_DEBUG)
			L.out(output);
	}

	/**
	 * Delete the route with the given rowId
	 * 
	 * @param rowId
	 * @return true if deleted, false otherwise
	 */
	@Override
	public boolean delete(long rowID) {
		L.out("delete: " + rowID);
		// showEvents(getTableName());
		boolean result = getDB().delete(getTableName(), KEY_ROWID + "=" + rowID, null) > 0;
		// L.out("after delete: " + result);
		// showEvents(getTableName());
		if (!result) {
			L.out("Deletion failed on " + rowID);
			showEvents(getTableName());
		}
		return result;
	}

	public static String getWhere(String soapMethod, String employeeId, String facilityID, String taskNumber) {
		// return StaticSoapColumns.SOAP_METHOD + "='" + soapMethod + "' AND "
		// + StaticSoapColumns.EMPLOYEE_ID + "='" + employeeID + "' AND "
		// + StaticSoapColumns.FACILITY_ID + "='" + facilityID + "' AND "
		// + StaticSoapColumns.TASK_NUMBER + "='" + taskNumber + "'";

		String temp = StaticSoapColumns.SOAP_METHOD + "='" + soapMethod + "' AND "
				+ StaticSoapColumns.FACILITY_ID + "='" + facilityID + "'";
		if (employeeId != null)
			temp += " AND " + StaticSoapColumns.EMPLOYEE_ID + "='" + employeeId + "'";
		if (taskNumber != null)
			temp += " AND " + StaticSoapColumns.TASK_NUMBER + "='" + taskNumber + "'";
		// L.out("where: " + temp);
		return temp;
	}

	/**
	 * Return a Cursor over the list of all SOAP JSON in the database
	 * 
	 * @return Cursor over all SOAP JSON
	 */
	public List<GJon> parse(Uri uri, String employeeID, String facilityID, String taskNumber) {
		// L.out("uri: " + uri);
		// showEvents(getTableName());
		String where = null;

		String soapMethod = uri.getPathSegments().get(0);
		if (uri != null) {
			where = getWhere(soapMethod, employeeID, facilityID, taskNumber);
			if (soapMethod.equals(ParsingSoap.VALIDATE_USER)) {
				where = getWhere(soapMethod, User.getUser().getUsername(), User.getUser().getPassword(), taskNumber);
			}
		}
		if (!soapMethod.equals(ParsingSoap.GET_TASK_INFORMATION_BY_TASK_NUMBER_AND_FACILITY_ID)) {
			L.out("Parse where: " + uri + "\n " + where);
		}

		Cursor cursor = getDB().query(getTableName(), null, where, null, null, null, null);
		L.out("cursor: " + cursor.getCount());
		// is in database already
		// force the
		// if (cursor.getCount() != 0 && soapMethod.equals("ValidateUser"))
		List<GJon> soapList = null;
		if (cursor.getCount() != 0 && !soapMethod.equals(ParsingSoap.VALIDATE_USER)) {
			// if (cursor.getCount() != 0 &&
			// !soapMethod.equals(ParsingSoap.VALIDATE_USER)) {
			soapList = getFromDatabase(cursor, employeeID, facilityID, taskNumber);
			if (soapList != null)
				return soapList;
			L.out("failed to decrypt employeeID: " + employeeID);
		}
		return getFromServer(cursor, uri, employeeID, facilityID, taskNumber);
		// && !NetworkUploader.isConnectedToInternet()))

		// get from the server

	}

	private List<GJon> getFromDatabase(Cursor cursor, String employeeID, String facilityID, String taskNumber) {

		List<GJon> soapList = new ArrayList<GJon>();
		cursor.moveToFirst();
		do {
			String json = cursor.getString(cursor.getColumnIndexOrThrow(StaticSoapColumns.JSON));
			if (WANT_SECURITY) {
				out("parse json: " + json);
				json = SecurityUtils.decryptAES(User.getUser().getPassword(), json);
				out("uncompressed: " + json);
				if (json == null) {
					L.out("cannot decrypt with: " + User.getUser().getPassword());
					return null;
				}
			}
			// json = Encrypt.decryptIt(json, User.getUser().getPassword());
			GJon gJon = null;
			gJon = new GJon(employeeID, facilityID, taskNumber, json);
			soapList.add(gJon);
			if (true)
				return soapList;
		} while (cursor.moveToNext());
		// L.out("finished: " + cursor.getCount());
		return soapList;

	}

	private List<GJon> getFromServer(Cursor cursor, Uri uri, String employeeID, String facilityID,
			String taskNumber) {
		L.out("getFromServer: " + uri);
		String soapMethod = uri.getPathSegments().get(0);
		if (NetworkUploader.isConnectedToInternet()) {
			// MyToast.show("ERROR getting from legacy...: " + taskNumber);
			List<GJon> soapList = new ParsingSoap().build(uri, employeeID, facilityID, taskNumber);

			if (soapList == null)
				return null;
			if (soapList != null) {
				L.out("SoapList: " + soapList.size());
				// put in db
				for (GJon gjon : soapList) {
					// need the employeeID and facilityID
					if (soapMethod.equals(ParsingSoap.VALIDATE_USER)) {
						ValidateUser validateUser = ValidateUser.getGJon(gjon.getJson());
						employeeID = validateUser.getEmployeeID();
						facilityID = validateUser.getFacilityID();
						taskNumber = User.getUser().getPlatform();

						create(soapMethod, gjon.getJson(), User.getUser().getUsername(), User.getUser().getPassword(), taskNumber);
					} else {
						if (gjon == null) {
							MyToast.show("Failed to load JSON for: " + soapMethod + " and facilityID: "
									+ facilityID);
						} else {
							String json = gjon.getJson();
							String password = User.getUser().getPassword();
							// L.out("password: " + password);

							create(soapMethod, json, employeeID, facilityID, taskNumber);
						}
					}
				}
				// MyToast.show("...Loaded " + L.getPlural(soapList.size(),
				// "Soap"));
				// showEvents(getTableName());
				// L.out("soapList: " + soapList);
				return soapList;
			}
		}
		// failure - no network!
		// Toast.makeText(context, "No network available for downloading Soap: "
		// + soapMethod, Toast.LENGTH_SHORT);
		L.out("no network available for downloading: " + soapMethod);
		return null;
	}

	public static Cursor fetch(long rowId) throws SQLException {
		// L.out("rowId: " + rowId);
		Cursor mCursor =
				getDB().query(SoapDbAdapter.getSoapDbAdapter().getTableName(), null, AbstractDbAdapter.KEY_ROWID
						+ "=" + rowId, null, null, null,
						null);

		// L.out("mCursor: " + mCursor);
		// if (mCursor != null) {
		// L.out("rows: " + mCursor.getCount());
		// }
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public final static String TICKLED = "tickled";

	@Override
	public long update(ContentValues values, String where) {
		// L.out("update values: " + values);
		boolean tickled = false;

		if (values.containsKey(TICKLED)) {
			String tickledString = values.getAsString(TICKLED);
			if (GJon.TRUE_STRING.equals(tickledString))
				tickled = true;
			values.remove(TICKLED);
		}
		// L.out("update tickled: " + tickled);
		if (!tickled)
			values.put(TIME_STAMP, new GregorianCalendar().getTimeInMillis() + "");
		// L.out("values: " + values);
		L.out("where: " + where);
		// mDbHelper.listTables();
		// L.out("values: " + values);
		String json = values.getAsString(StaticSoapColumns.JSON);
		if (WANT_SECURITY && json != null) {
			out("before update json: " + json);
			json = SecurityUtils.encryptAES(User.getUser().getPassword(), json);
			values.put(StaticSoapColumns.JSON, json);
			out("after update compressed: " + json);
			// test
			String test = SecurityUtils.decryptAES(User.getUser().getPassword(), json);
			out("uncompressed : " + test);
		}
		long updated = getDB().update(getTableName(), values, where, null);
		L.out("updated result: " + updated + " " + tickled);
		// not in table
		if (updated == 0) {
			long result = getDB().insert(getTableName(), null, values);
			L.out("insert result: " + result);
		}
		showEvents(getTableName());
		L.out("tickled: " + tickled);
		if (!tickled)
			super.update(values, where);
		return updated;
	}

	public static int haveSomethingToUpload() {
		if (soapDbAdapter == null)
			return 0;
		SQLiteDatabase db = soapDbAdapter.getWritableDatabase();
		if (db == null) {
			L.out("db: " + db);
			return 0;
		}

		String where = AbstractDbAdapter.TIME_STAMP + " is not null";
		// L.out("where: " + where);
		Cursor cursor = db.query(soapDbAdapter.getTableName(), null, where, null, null,
				null, null);
		// // L.out("raw c: " + c.getCount());
		if (cursor.getCount() > 0) {
			L.out("count to update: " + cursor.getCount());
		}
		return cursor.getCount();
	}

	@Override
	public boolean uploadIfNeeded() {

		// L.out("uploadIfNeeded");
		SQLiteDatabase db = getWritableDatabase();
		if (db == null) {
			L.out("db: " + db);
			return false;
		}

		String where = AbstractDbAdapter.TIME_STAMP + " is not null";
		// L.out("where: " + where);
		Cursor cursor = db.query(getTableName(), null, where, null, null,
				null, null);
		L.out("uploadIfNeeded: " + cursor.getCount());
		if (cursor.getCount() < 1) {
			return false;
		} else {
			cursor = getFirstToUpload(cursor);

			Cursor newCursor = findDispatcherTask();
			if (newCursor != null)
				cursor = newCursor;

			if (cursor == null)
				return false;

			String soapMethod = cursor.getString(cursor.getColumnIndexOrThrow(StaticSoapColumns.SOAP_METHOD));
			String json = cursor.getString(cursor.getColumnIndexOrThrow(StaticSoapColumns.JSON));
			// String timeStamp =
			// cursor.getString(cursor.getColumnIndexOrThrow(AbstractDbAdapter.TIME_STAMP));
			String taskNumber = cursor.getString(cursor.getColumnIndexOrThrow(StaticSoapColumns.TASK_NUMBER));
			String rowId = cursor.getString(cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_ROWID));
			// int id =
			// cursor.getInt(cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_ROWID));
			String localTaskNumber = cursor.getString(cursor.getColumnIndexOrThrow(StaticSoapColumns.LOCAL_TASK_NUMBER));
			String employeeID = cursor.getString(cursor.getColumnIndexOrThrow(StaticSoapColumns.EMPLOYEE_ID));
			String facilityID = cursor.getString(cursor.getColumnIndexOrThrow(StaticSoapColumns.FACILITY_ID));
			// L.out("timeStamp: " + timeStamp);
			L.out("taskNumber: " + taskNumber + " soapMethod: " + soapMethod + " employeeID: " + employeeID
					+ " localTaskNumber: " + localTaskNumber);
			if (WANT_SECURITY) {
				json = SecurityUtils.decryptAES(User.getUser().getPassword(), json);
				out("uploadIfNeeded json: " + json);
			}
			// if (soapMethod.equals(ParsingSoap.VALIDATE_USER) &&
			// User.getUser().getWantBreak())
			// return true;

			if (localTaskNumber == null) {
				// L.out("update the existing record:");
				// showEvents(getTableName());

				GJon gJonObject = new ParsingSoap().bind(soapMethod, json);
				if (gJonObject instanceof GetTaskInformationByTaskNumberAndFacilityID) {
					GetTaskInformationByTaskNumberAndFacilityID task = (GetTaskInformationByTaskNumberAndFacilityID) gJonObject;
					if (TabNavigationActivity.showToasts)
						if (TabNavigationActivity.showToasts)
							MyToast.show("Update task: " + task.getTaskNumber() + " to "
									+ task.getTaskStatusBrief());
					IMActivity.o("Update task: " + task.getTaskNumber() + " to "
							+ task.getTaskStatusBrief());
				} else {
					// ValidateUser validateUser = (ValidateUser) gJonObject;
					// MyToast.show("Update user: " +
					// validateUser.getEmployeeStatus());
				}
				if (false && canDoNow(localTaskNumber, soapMethod, taskNumber, rowId)) {
					IMActivity.p("Cancel task: " + taskNumber + "\n");
					ContentValues contentValues = new ContentValues();
					updateTimeStamp(cursor, rowId, contentValues, true);
					showEvents(getTableName());
					return false;
				}
				String result = new RestWriter().updateRecord(employeeID, facilityID, gJonObject, taskNumber, null);
				L.out("update result: " + result);
				if (result != null) {
					L.out("updateRecord success: " + result);
					ContentValues contentValues = new ContentValues();
					boolean notDone = false;
					if (gJonObject instanceof GetTaskInformationByTaskNumberAndFacilityID) {
						GetTaskInformationByTaskNumberAndFacilityID task = (GetTaskInformationByTaskNumberAndFacilityID) gJonObject;
						String updateStatus = task.getTaskStatusBrief();
						task = updateTask(rowId, task);
						if (!task.getTaskStatusBrief().equals(updateStatus))
							notDone = true;
						task.setJson(null);
						task.setJson(task.getNewJson());
						contentValues.put(StaticSoapColumns.JSON, doEncrypt(task.getJson()));
					}
					updateTimeStamp(cursor, rowId, contentValues, notDone);
					// updateTimeStamp(cursor, result, rowId, null, new
					// ContentValues());
				} else {
					if (gJonObject instanceof GetTaskInformationByTaskNumberAndFacilityID) {
						GetTaskInformationByTaskNumberAndFacilityID task =
								(GetTaskInformationByTaskNumberAndFacilityID) gJonObject;
						// if (TabNavigationActivity.showToasts)
						// MyToast.show("Maybe Failure Update task: " +
						// task.getTaskNumber() + " to "
						// + task.getTaskStatusBrief());
						IMActivity.o("Failure Update task: " + task.getTaskNumber() + " to "
								+ task.getTaskStatusBrief());
					} else {
						ValidateUser validateUser = (ValidateUser) gJonObject;
						if (TabNavigationActivity.showToasts)
							MyToast.show("Failure Update user: " + validateUser.getEmployeeStatus());
						IMActivity.o("Failure Update user: " + validateUser.getEmployeeStatus());
					}
				}
			} else {
				GetTaskInformationByTaskNumberAndFacilityID task =
						(GetTaskInformationByTaskNumberAndFacilityID) new ParsingSoap().bind(soapMethod, json);
				// MyToast.show("Create task: " + task.getTaskNumber());
				if (task == null) {
					delete(rowId);
					return false;
				}
				String oldTaskNumber = task.getTaskNumber();
				// task.setTaskNumber(null);
				// if (!canDoNow(localTaskNumber, soapMethod, taskNumber,
				// rowId))
				// return false;
				String tskNumber = new RestWriter().updateRecord(employeeID, facilityID, task, oldTaskNumber, rowId);
				L.out("create result: " + tskNumber);
				IMActivity.o("create result: " + tskNumber);
				if (RestWriter.COAST_IS_NOT_CLEAR.equals(tskNumber)) {
					L.out("COAST_IS_NOT_CLEAR: " + tskNumber);
					return false;
				}
				// MyToast.show("create result: " + tskNumber);
				if (tskNumber != null && tskNumber.equals("failure")) {
					L.out("Unable to create: " + taskNumber + " result: " + tskNumber);
					IMActivity.o("Unable to create: " + taskNumber + " result: " + tskNumber);
					return false;
				}
				if (tskNumber != null) {
					String oldStatus = task.getTaskStatusBrief();
					task = updateTask(rowId, task);
					// task.setTaskNumber(tskNumber);
					// task.setJson(null);
					task.setJson(task.getNewJson());
					// task.printJson();
					ContentValues contentValues = new ContentValues();
					contentValues.put(StaticSoapColumns.JSON, doEncrypt(task.getJson()));
					contentValues.put(StaticSoapColumns.TASK_NUMBER, tskNumber);
					contentValues.put(StaticSoapColumns.LOCAL_TASK_NUMBER, (String) null);
					L.out("task.getTaskStatusBrief(): " + task.getTaskStatusBrief());
					IMActivity.o("created taskNumber: " + tskNumber + " status: " + task.getTaskStatusBrief()
							+ " oldStatus: " + oldStatus);
					if (task.getTaskStatusBrief().equals(TaskActivity.ASSIGNED))
						updateTimeStamp(cursor, rowId, contentValues, false);
					else
						updateTimeStamp(cursor, rowId, contentValues, true);
					showEvents(getTableName());
				}
			}
			return true;
		}
	}

	public static GetTaskInformationByTaskNumberAndFacilityID updateTask(String rowId,
			GetTaskInformationByTaskNumberAndFacilityID updatedTask) {
		// L.out("rowId: " + rowId);
		Cursor cursor = fetch(Integer.parseInt(rowId));
		String json = cursor.getString(cursor.getColumnIndexOrThrow(StaticSoapColumns.JSON));
		if (WANT_SECURITY) {
			json = SecurityUtils.decryptAES(User.getUser().getPassword(), json);
			// out("uploadIfNeeded json: " + json);
		}
		GetTaskInformationByTaskNumberAndFacilityID task =
				(GetTaskInformationByTaskNumberAndFacilityID) new ParsingSoap().bind(ParsingSoap.GET_TASK_INFORMATION_BY_TASK_NUMBER_AND_FACILITY_ID, json);
		if (updatedTask != null) {
			// IMActivity.o("CurrentStatus: " + task.getTaskStatusBrief() +
			// " UpdatedStatus: "
			// + updatedTask.getTaskStatusBrief());
			task.setTaskNumber(updatedTask.getTaskNumber());
		}
		return task;
	}

	private static boolean canDoIt(String localTaskNumber, String soapMethod, String taskNumber, String rowId) {
		if (soapMethod.equals(ParsingSoap.VALIDATE_USER))
			return RestWriter.checkIfTheUserCoastIsClear();
		else {
			return RestWriter.checkIfTheTaskCoastIsClear(taskNumber,
					((localTaskNumber == null)) ? false : true, null);
		}
	}

	private static final String LOCAL_TASK_NUMBER = "localTaskNumber";
	private static final String TASK_NUMBER = "taskNumber";
	private static final String SOAP_METHOD = "soapMethod";
	private static final String ROW_ID = "rowId";

	private boolean canDoNow(String localTaskNumber, String soapMethod, String taskNumber, String rowId) {
		if (canDoIt(localTaskNumber, soapMethod, taskNumber, rowId))
			return true;
		if (Tickler.blocked) {
			L.out("Blocked and deleting: " + soapMethod + " " + taskNumber + " " + taskNumber);
			return false;
		}
		L.out("saving for later: " + soapMethod + " " + taskNumber + " " + taskNumber);
		Bundle bundle = new Bundle();
		bundle.putString(LOCAL_TASK_NUMBER, localTaskNumber);
		bundle.putString(TASK_NUMBER, taskNumber);
		bundle.putString(SOAP_METHOD, soapMethod);
		bundle.putString(ROW_ID, rowId);
		Message message = new Message();
		message.setData(bundle);

		waitHandler.removeMessages(0, null);
		waitHandler.sendMessageDelayed(message, 1000);
		return false;
	}

	public static final Handler waitHandler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			// Do task here
			L.out("msg: " + message.getData());
			Bundle bundle = message.getData();
			GetTaskInformationByTaskNumberAndFacilityID task = updateTask(bundle.getString(ROW_ID), null);
			L.out("task: " + task.getTaskNumber());
			if (L.getLong(task.getTaskNumber()) == 0)
				return;

			if (!canDoIt(bundle.getString(LOCAL_TASK_NUMBER),
					bundle.getString(SOAP_METHOD),
					bundle.getString(TASK_NUMBER),
					bundle.getString(ROW_ID))) {
				// IMActivity.o("The coast is still not clear for: " +
				// bundle.getString(SOAP_METHOD) + " "
				// + bundle.getString(TASK_NUMBER));

				waitHandler.removeMessages(0, null);
				Message newMessage = new Message();
				newMessage.setData(bundle);
				waitHandler.sendMessageDelayed(newMessage, 1000);
			} else {
				IMActivity.o("The coast is clear!");
				if (SoapDbAdapter.networkUploader != null) {
					SoapDbAdapter.networkUploader.onResume();
				}
			}
			// new Thread(new Runnable() {
			// @Override
			// public void run() {
			//
			// }
			// }).start();

			// if (message.what == DISPLAY_DATA) {
			// }
			// displayData();
		}
	};

	private Cursor getFirstToUpload(Cursor cursor) {

		// String selectedRow = null;
		String selectedMethod = null;
		String selectedLocalTaskNumber = null;
		String selectedTaskNumber = null;
		int selectedPosition = 0;
		int index = 0;

		cursor.moveToFirst();
		do {
			// int index = cursor.getColumnIndex(StaticSoapColumns.JSON);
			// L.out("index: " + index + " " + cursor.getClass());
			String rowId = cursor.getString(cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_ROWID));
			String soapMethod = cursor.getString(cursor.getColumnIndexOrThrow(StaticSoapColumns.SOAP_METHOD));
			String taskNumber = cursor.getString(cursor.getColumnIndexOrThrow(StaticSoapColumns.TASK_NUMBER));
			String localTaskNumber = cursor.getString(cursor.getColumnIndexOrThrow(StaticSoapColumns.LOCAL_TASK_NUMBER));
			if (soapMethod.equals(ParsingSoap.VALIDATE_USER))
				IMActivity.o("   " + index + ": Upload Actor" + soapMethod);
			else
				IMActivity.o("   " + index + ": Upload " + "Task" + " " + taskNumber + " "
						+ localTaskNumber);
			if (selectedMethod == null
					|| L.getLong(taskNumber) == 0 || selectedMethod.equals(ParsingSoap.VALIDATE_USER)) {
				// IMActivity.o(index + " Upload: " + soapMethod + " " +
				// taskNumber + " " + localTaskNumber);
				selectedPosition = index;
				selectedMethod = soapMethod;
				selectedLocalTaskNumber = localTaskNumber;
				selectedTaskNumber = taskNumber;
				L.out("selected selectedPosition: " + selectedPosition
						+ " rowId: " + rowId
						+ " selectedMethod: " + selectedMethod
						+ " selectedLocalTaskNumber: " + selectedLocalTaskNumber);
			}
			index += 1;
		} while (cursor.moveToNext());

		// GetEmployeeAndTaskStatusByEmployeeID status = ActorController.status;
		// if (status != null)
		// if (selectedLocalTaskNumber != null
		// && status.getTaskNumber() != null
		// && !status.getTaskNumber().equals(selectedLocalTaskNumber)) {
		// L.out("Save for later create: " + selectedLocalTaskNumber);
		// L.out("status: " + status);
		// return null;
		// }
		// showEvents(getTableName());
		L.out("position: " + selectedPosition + " value: " + selectedMethod);
		if (selectedMethod.equals(ParsingSoap.VALIDATE_USER))
			IMActivity.o("-> " + selectedPosition + " Try Upload: " + selectedMethod);
		else
			IMActivity.o("-> " + selectedPosition + " Try Upload: "
					+ selectedTaskNumber
					+ " "
					+ selectedLocalTaskNumber);
		cursor.moveToPosition(selectedPosition);
		return cursor;
	}

	private String doEncrypt(String json) {
		if (WANT_SECURITY) {
			out("updateStatus uncompressed: " + json);
			json = SecurityUtils.encryptAES(User.getUser().getPassword(), json);
			out("updateStatus compressed: " + json);
		}
		return json;
	}

	public void delete(String soapMethod) {
		String where = StaticSoapColumns.SOAP_METHOD + "='" + soapMethod + "'";
		Cursor cursor = getDB().query(getTableName(), null, where, null, null, null, null);
		int count = cursor.getCount();
		// L.out("delete cursor: " + count);

		if (count != 0) {
			cursor.moveToFirst();
			String rowID = cursor.getString(cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_ROWID));
			int rowIndex = (int) L.getLong(rowID);
			// L.out("delete cursor: " + count + " rowId: " + rowIndex);
			delete(rowIndex);
		} else {
			L.out("*** Unable to delete record: " + soapMethod);
		}
	}

	public static void setSoapDbAdapter(SoapDbAdapter soapDbAdapter) {
		SoapDbAdapter.soapDbAdapter = soapDbAdapter;
	}

	public GetTaskInformationByTaskNumberAndFacilityID isSameTask(
			GetTaskInformationByTaskNumberAndFacilityID newTask) {
		String where = StaticSoapColumns.SOAP_METHOD + "='"
				+ ParsingSoap.GET_TASK_INFORMATION_BY_TASK_NUMBER_AND_FACILITY_ID + "' and "
				+ AbstractDbAdapter.TIME_STAMP + " is not null";
		// L.out("where: " + where);
		SQLiteDatabase db = soapDbAdapter.getWritableDatabase();
		Cursor cursor = db.query(getTableName(), null, where, null, null,
				null, null);
		if (cursor.getCount() < 1)
			return null;
		cursor.moveToFirst();
		String soapMethod = cursor.getString(cursor.getColumnIndexOrThrow(StaticSoapColumns.SOAP_METHOD));
		String json = cursor.getString(cursor.getColumnIndexOrThrow(StaticSoapColumns.JSON));
		String timeStamp = cursor.getString(cursor.getColumnIndexOrThrow(AbstractDbAdapter.TIME_STAMP));
		String taskNumber = cursor.getString(cursor.getColumnIndexOrThrow(StaticSoapColumns.TASK_NUMBER));
		String rowId = cursor.getString(cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_ROWID));
		int id = cursor.getInt(cursor.getColumnIndexOrThrow(AbstractDbAdapter.KEY_ROWID));
		String localTaskNumber = cursor.getString(cursor.getColumnIndexOrThrow(StaticSoapColumns.LOCAL_TASK_NUMBER));
		String employeeID = cursor.getString(cursor.getColumnIndexOrThrow(StaticSoapColumns.EMPLOYEE_ID));
		String facilityID = cursor.getString(cursor.getColumnIndexOrThrow(StaticSoapColumns.FACILITY_ID));
		L.out("rowId: " + rowId);
		L.out("id: " + id);
		// L.out("timeStamp: " + timeStamp);
		L.out("taskNumber: " + taskNumber + " soapMethod: " + soapMethod + " employeeID: " + employeeID
				+ " localTaskNumber: " + localTaskNumber + " newTaskNumber: " + newTask.getTaskNumber());
		if (WANT_SECURITY) {
			json = SecurityUtils.decryptAES(User.getUser().getPassword(), json);
			out("uploadIfNeeded json: " + json);
		}

		GetTaskInformationByTaskNumberAndFacilityID task = (GetTaskInformationByTaskNumberAndFacilityID) new ParsingSoap().bind(soapMethod, json);
		L.out("task.isSameTask(newTask): " + task.isSameTask(newTask));
		if (!task.isSameTask(newTask))
			return null;

		ContentValues contentValues = new ContentValues();
		// boolean notDone = task.getTickled();
		task.oldTaskNumber = task.getTaskNumber();
		task.setTaskNumber(newTask.getTaskNumber());
		task.setJson(task.getNewJson());
		contentValues.put(StaticSoapColumns.JSON, doEncrypt(task.getJson()));
		contentValues.put(StaticSoapColumns.TASK_NUMBER, task.getTaskNumber());
		contentValues.put(StaticSoapColumns.LOCAL_TASK_NUMBER, (String) null);
		boolean notDone = false;
		if (!newTask.getTaskStatusBrief().equals(task.getTaskStatusBrief()))
			notDone = true;
		updateTimeStamp(cursor, rowId, contentValues, notDone);
		showEvents(getTableName());

		return task;
	}

	public Cursor findDispatcherTask() {

		SQLiteDatabase db = getWritableDatabase();
		if (db == null) {
			L.out("db: " + db);
			return null;
		}

		String where = StaticSoapColumns.SOAP_METHOD + "='"
				+ ParsingSoap.GET_TASK_INFORMATION_BY_TASK_NUMBER_AND_FACILITY_ID + "' and "
				+ AbstractDbAdapter.TIME_STAMP + " is not null";
		// L.out("where: " + where);
		Cursor cursor = db.query(getTableName(), null,
				where, null, null,
				null, null);
		// L.out("cursor.getCount(): " + cursor.getCount());

		if (cursor.getCount() > 0) {
			cursor.moveToLast();
			do {
				String taskNumber = cursor.getString(cursor.getColumnIndexOrThrow(StaticSoapColumns.TASK_NUMBER));
				L.out("taskNumber: " + taskNumber);

				if (L.getLong(taskNumber) == 0) {
					L.out("taskNumber: " + taskNumber);
					return cursor;
				}
			} while (cursor.moveToPrevious());

		}
		return null;
	}

}
