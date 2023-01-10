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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.summerclouds.common.core.node.MNode;
import org.summerclouds.common.core.operation.OperationDescription;
import org.summerclouds.common.core.operation.OperationManager;
import org.summerclouds.common.core.operation.OperationResult;
import org.summerclouds.common.core.operation.cmd.CmdOperation;
import org.summerclouds.common.core.util.EnumerationIterator;

@RestController
@ConditionalOnProperty(name = "org.summerclouds.operations.enabled", havingValue = "true")
public class OperationController {

    @Autowired private OperationManager manager;

    //	@RequestMapping(value="cmd", method=RequestMethod.POST)
    //	public void cmd(HttpServletResponse response) {
    //		ServletOutputStream os = response.getOutputStream();
    //		manager.execute();
    //	}

    /**
     * Execute operation
     *
     * @param response
     * @return
     * @throws Exception
     */
    @PostMapping("/operation/{uri}")
    public ServiceOperationResult operationExecute(
            @PathVariable("uri") String uri, HttpServletRequest request) throws Exception {
        uri = "operation://" + uri;
        MNode node = new MNode();
        for (String key : new EnumerationIterator<String>(request.getParameterNames())) {
            String value = request.getParameter(key);
            node.setString(key, value);
        }
        OperationResult res = manager.execute(uri, node);
        return new ServiceOperationResult(res);
    }

    @GetMapping("/operation")
    public String[] operationList() {
        return manager.getOperations();
    }

    @GetMapping("/operation/{uri}")
    public ServiceOperationDescription operationDescription(@PathVariable("uri") String uri)
            throws Exception {
        uri = "operation://" + uri;
        return new ServiceOperationDescription(manager.getDescription(uri));
    }

    @GetMapping("/cmd")
    public List<ServiceOperationDescription> cmdGet() throws Exception {
        List<ServiceOperationDescription> list = new ArrayList<>();
        for (String name : manager.getCommands()) {
            OperationDescription desc = manager.getDescription(name);
            list.add(new ServiceOperationDescription(desc).setPath(name.substring(6)));
        }

        return list;
    }

    @PostMapping("/cmd/{uri}")
    public void cmdExecute(
            @PathVariable("uri") String uri,
            HttpServletRequest request,
            HttpServletResponse response)
            throws Exception {
        MNode node = new MNode();
        for (String key : new EnumerationIterator<String>(request.getParameterNames())) {
            String value = request.getParameter(key);
            node.setString(key, value);
        }
        node.put(CmdOperation.PARAMETER_OUTPUT_STREAM, response.getOutputStream());

        uri = "cmd://" + uri;
        OperationResult res = manager.execute(uri, node);
        if (!response.isCommitted() && !res.isSuccessful()) {
            response.sendError(res.getReturnCode(), res.getMessage());
        }
    }
}
