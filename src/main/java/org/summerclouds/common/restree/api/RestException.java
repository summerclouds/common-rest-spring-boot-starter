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
package org.summerclouds.common.restree.api;

import org.summerclouds.common.core.error.MException;
import org.summerclouds.common.core.error.RC;
import org.summerclouds.common.core.node.IReadProperties;

public class RestException extends MException {

    private static final long serialVersionUID = 1L;
    private IReadProperties param;

    public RestException(String msg, int rc, IReadProperties param) {
        super(rc, msg);
        this.param = param;
    }

    public RestException(int rc, Object... in) {
        super(rc, RC.toString(rc), in);
    }

    public IReadProperties getParameters() {
        return param;
    }
}
