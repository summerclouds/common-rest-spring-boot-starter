package org.summerclouds.common.rest.operation;

import org.summerclouds.common.core.error.ErrorException;
import org.summerclouds.common.core.operation.OperationComponent;
import org.summerclouds.common.core.operation.cmd.CmdArgument;
import org.summerclouds.common.core.operation.cmd.CmdOperation;
import org.summerclouds.common.core.operation.cmd.CmdOption;
import org.summerclouds.common.core.tool.MThread;

@OperationComponent
public class Cmd1 extends CmdOperation {

	@CmdArgument(index = 0)
	private String arg0;

	@CmdOption
	private String opt;
	
	@Override
	protected String executeCmd() throws Exception {
		System.out.println("Execute Cmd " + getClass());
		System.out.println("Opt: " + opt);
		MThread.sleepForSure(1000);
		System.out.println("Finish Cmd " + getClass());
		if ("error".equals(opt)) {
			throw new ErrorException("test error");
		}
		return "Hello";
	}

}
