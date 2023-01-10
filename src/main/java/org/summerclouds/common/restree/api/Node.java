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

import java.util.List;

import org.summerclouds.common.restree.CallContext;
import org.summerclouds.common.restree.RestSocket;

public interface Node {

    String ACTION = "_action";

    String PUBLIC_NODE_NAME = "public";

    String PUBLIC_PARENT = "de.mhus.rest.core.nodes.PublicRestNode";

    String ROOT_PARENT = "";

    Node lookup(List<String> parts, CallContext callContext) throws Exception;

    RestResult doRead(CallContext callContext) throws Exception;

    RestResult doAction(CallContext callContext) throws Exception;

    RestResult doCreate(CallContext callContext) throws Exception;

    RestResult doUpdate(CallContext callContext) throws Exception;

    RestResult doDelete(CallContext callContext) throws Exception;

    boolean streamingAccept(RestSocket socket);

    void streamingText(RestSocket socket, String message);

    void streamingBinary(RestSocket socket, byte[] payload, int offset, int len);
}
