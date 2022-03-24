/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
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
package org.summerclouds.common.restree.util;

import java.util.UUID;

import org.summerclouds.common.core.pojo.PojoModel;
import org.summerclouds.common.core.pojo.PojoModelFactory;
import org.summerclouds.common.core.pojo.PojoParser;
import org.summerclouds.common.core.pojo.Public;
import org.summerclouds.common.restree.CallContext;

public class RestUtil {

    public static final int MAX_RETURN_SIZE = 1000;

    private static final PojoModelFactory POJO_FACTORY =
            new PojoModelFactory() {

                @Override
                public PojoModel createPojoModel(Class<?> clazz) {
                    return new PojoParser()
                            .parse(clazz, "_", Public.class)
                            .filter(true, false, true, false, true)
                            .getModel();
                }
            };

    public static String getObjectIdParameterName(Class<?> clazz) {
        return clazz.getSimpleName().toLowerCase() + "Id";
    }

    public static UUID getObjectUuid(CallContext callContext, Class<?> clazz) {
        return UUID.fromString(callContext.getParameter(getObjectIdParameterName(clazz)));
    }

    public static String getObjectId(CallContext callContext, Class<?> clazz) {
        return callContext.getParameter(getObjectIdParameterName(clazz));
    }

    public static PojoModelFactory getPojoModelFactory() {
        return POJO_FACTORY;
    }
}
