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
            throw new ErrorException(context.getParameters().getString("error"));
        return new SuccessfulMap(
                this,
                "ok",
                "object",
                MSystem.getObjectId(this),
                "timestamp",
                "" + System.currentTimeMillis());
    }
}
