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
