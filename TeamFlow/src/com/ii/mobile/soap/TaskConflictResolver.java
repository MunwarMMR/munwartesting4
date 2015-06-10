package com.ii.mobile.soap;

import com.ii.mobile.soap.gson.GetEmployeeAndTaskStatusByEmployeeID;
import com.ii.mobile.soap.gson.GetTaskInformationByTaskNumberAndFacilityID;

public class TaskConflictResolver {

	public GetTaskInformationByTaskNumberAndFacilityID resolve(String taskNumber,
			GetEmployeeAndTaskStatusByEmployeeID taskStatus) {
		// GetTaskInformationByTaskNumberAndFacilityID task =
		// SoapDbAdapter.getSoapDbAdapter().findTaskNumber(taskNumber);
		// if (task == null)
		// return null;
		// return task;
		return null;
	}

}
