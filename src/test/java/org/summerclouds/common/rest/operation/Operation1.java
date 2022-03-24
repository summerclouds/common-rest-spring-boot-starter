package org.summerclouds.common.rest.operation;

import org.summerclouds.common.core.error.ErrorException;
import org.summerclouds.common.core.operation.AbstractOperation;
import org.summerclouds.common.core.operation.OperationComponent;
import org.summerclouds.common.core.operation.OperationResult;
import org.summerclouds.common.core.operation.TaskContext;
import org.summerclouds.common.core.operation.util.SuccessfulMap;
import org.summerclouds.common.core.tool.MSystem;

@OperationComponent
public class Operation1 extends AbstractOperation {

	@Override
	protected OperationResult execute(TaskContext context) throws Exception {
		log().i("Operation Started");
		if (context.getParameters().getString("error", null) != null)
			throw new ErrorException( context.getParameters().getString("error") );
		return new SuccessfulMap(this, "ok", "object", MSystem.getObjectId(this),"timestamp", ""+System.currentTimeMillis());
	}

}
