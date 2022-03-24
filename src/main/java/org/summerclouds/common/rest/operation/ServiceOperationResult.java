package org.summerclouds.common.rest.operation;

import org.summerclouds.common.core.operation.OperationResult;

public class ServiceOperationResult {

	private String message;
	private int returnCode;
	private Object result;
	private String operation;

	@SuppressWarnings("deprecation")
	public ServiceOperationResult(OperationResult res) {
		message = res.getMessage();
		returnCode = res.getReturnCode();
		result = res.getResult();
		operation = res.getOperationPath();
	}

	public String getMessage() {
		return message;
	}

	public int getReturnCode() {
		return returnCode;
	}

	public Object getResult() {
		return result;
	}

	public String getOperation() {
		return operation;
	}


}
