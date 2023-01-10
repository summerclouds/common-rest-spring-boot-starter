/**
 * Copyright (C) 2022 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    @CmdOption private String opt;

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
