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

import java.util.Map;

import org.summerclouds.common.core.form.DefRoot;
import org.summerclouds.common.core.node.IReadProperties;
import org.summerclouds.common.core.operation.OperationDescription;

public class ServiceOperationDescription {

    private String path;
    private String version;
    private IReadProperties labels;
    private Map<String, Object> form;

    public ServiceOperationDescription(OperationDescription desc) {
        path = desc.getPath();
        version = desc.getVersionString();
        labels = desc.getLabels();
        DefRoot f = desc.getForm();
        if (f != null) form = f.toMap();
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public IReadProperties getLabels() {
        return labels;
    }

    public Map<String, Object> getForm() {
        return form;
    }

    public ServiceOperationDescription setPath(String path) {
        this.path = path;
        return this;
    }
}
