/*
 * 
 */
package com.ii.mobile.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.ii.mobile.users.User;

public class ApplicationContext extends Application {

	// used by toast
	public static Activity activity = null;
	public static String PREFERENCE_FILE = "crothallMobile";

	private static ApplicationContext applicationContext = null;
	public static User user;

	public static boolean deleteDataBase = true;

	// public static String lastPassword = "";

	/**
	 * Enum used to identify the tracker that needs to be used for tracking.
	 * 
	 * A single tracker is usually enough for most purposes. In case you do need
	 * multiple trackers, storing them all in Application object helps ensure
	 * that they are created only once per application instance.
	 */
	public enum TrackerName {
		APP_TRACKER, // Tracker used only in this app.
		GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg:
						// roll-up tracking.
		ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a
							// company.
	}

	public ApplicationContext() {
		super();
		applicationContext = this;

	}

	public static Context getAppContext() {
		// L.out("applicationContext: " + applicationContext);
		return applicationContext;
	}

}
