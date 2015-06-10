package com.ii.mobile.home;

import java.util.GregorianCalendar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import com.ii.mobile.soap.ParsingSoap;
import com.ii.mobile.soap.StaticSoap;
import com.ii.mobile.soap.gson.ValidateUser;
import com.ii.mobile.users.User;
import com.ii.mobile.util.L;

class DoLogin extends AsyncTask<Void, Integer, Long> {

	boolean successfulLogin = false;
	ProgressDialog progressDialog;
	private LoginActivity activity = null;
	private final LoginCallBack loginCallBack;

	public DoLogin(LoginActivity activity, LoginCallBack loginCallBack) {
		this.activity = activity;
		this.loginCallBack = loginCallBack;
	}

	@Override
	protected Long doInBackground(Void... arg0) {

		Thread.currentThread().setName("LoginThread");
		Intent intent = activity.getIntent();
		intent.setData(Uri.parse("content://" + StaticSoap.AUTHORITY + "/" +
				ParsingSoap.VALIDATE_USER));

		String employeeID = User.getUser().getUsername();
		String facilityID = User.getUser().getPassword();
		String platform = User.getUser().getPlatform();
		// L.out("Platform: " + platform);

		String[] selectionArgs = new String[] { employeeID, facilityID, platform };
		Cursor cursor = activity.managedQuery(activity.getIntent().getData(), null, null, selectionArgs, null);
		// if (cursor != null)
		// L.out("cursor: " + cursor.getCount());
		// MyToast.show("cursor: " + cursor.getCount());
		User user = User.getUser();
		// L.out("user: " + user.toString());
		String password = user.getPassword();
		// L.out("password: " + password);

		ValidateUser validateUser = user.getValidateUser();
		// L.out("validateUser: " + validateUser);
		if (validateUser == null) {
			L.out("validateUser is null??");
			return 0l;
		}
		// L.out("ValidateUser: " + validateUser.toString());
		// if (!platform.equals(validateUser.getPlatform())) {
		// L.out("different Platform have: "+platform+" got "+validateUser.getPlatform());
		// }
		// remove true!
		if (validateUser != null && password.equals(validateUser.getMobilePIN())) {
			// txtPassword.setText("");f
			user.setReload(true);
			LoginActivity.loginTime = new GregorianCalendar().getTimeInMillis();
			successfulLogin = true;
			return 0l;
		}
		// MyToast.show("Validate: " + validateUser);
		return 0l;
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		L.out("probably not used but executes in UI thread");
	}

	@Override
	protected void onPreExecute() {
		progressDialog = new ProgressDialog(activity);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("Validating Credentials...");
		progressDialog.setCancelable(false);
		progressDialog.show();
	}

	@Override
	protected void onPostExecute(Long l) {
		// L.out("dismiss");
		try {
			progressDialog.dismiss();
		} catch (Exception e) {
			L.out("ERROR on dismiss: " + e);
		}

		if (successfulLogin) {
			loginCallBack.loginSuccess();

		} else {
			loginCallBack.loginFailed();
		}
	}

}
