package com.ii.mobile.soap.gson;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.ii.mobile.soap.BaseSoap;
import com.ii.mobile.util.L;

public class Logger extends GJon {

	public GetEmployeeAndTaskStatusByEmployeeID getEmployeeAndTaskStatusByEmployeeID = new GetEmployeeAndTaskStatusByEmployeeID();

	public Logger(GetEmployeeAndTaskStatusByEmployeeID getEmployeeAndTaskStatusByEmployeeID) {
		this.getEmployeeAndTaskStatusByEmployeeID = getEmployeeAndTaskStatusByEmployeeID;
	}

	@Override
	public boolean validate() {
		if (getEmployeeAndTaskStatusByEmployeeID != null
				&& getEmployeeAndTaskStatusByEmployeeID.employeeAndTaskStatusDetails.employeeAndTaskStatus != null)
			validated = true;
		else
			L.out("Unable to validate Logger");
		return validated;
	}

	static public GetEmployeeAndTaskStatusByEmployeeID getGJon(String json) {

		json = makeSureIsAnArray("EmployeeAndTaskStatus", json);
		GetEmployeeAndTaskStatusByEmployeeID status;
		status = (GetEmployeeAndTaskStatusByEmployeeID) getJSonObjectArray(json, GetEmployeeAndTaskStatusByEmployeeID.class);
		// L.out("getEmployeeAndTaskStatusByEmployeeID: " + status);
		// Gson gson = new Gson();
		// String foo = gson.toJson(status);
		// L.out("json: " + foo);
		return status;
	}

	static protected GJon getJSonObjectArray(String json, Class<?> className) {
		BaseSoap.debugOutput("\n\n*** " + className + " ***\n");
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE);
		// gsonBuilder.registerTypeAdapter(MobileMessage[].class, new
		// MobileMessageDeserializer());

		Gson gson = gsonBuilder.create();
		JsonParser parser = new JsonParser();
		GJon gJon = null;
		try {
			gJon = (GJon) gson.fromJson(parser.parse(json).getAsJsonObject().toString(), className);
		} catch (Exception e) {

			L.out("*** ERROR Failed: " + e + "\njson: " + json + " " + className);
		}
		if (gJon == null) {
			L.out("Failed to parse json for: " + className);
			return null;
		}
		gJon.json = json;
		if (gJon.validate()) {
			// need to uncomment to print to console
			BaseSoap.debugOutput(gJon.toString());
		}
		return gJon;
	}

}
