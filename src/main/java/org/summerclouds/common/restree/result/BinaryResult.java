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
package org.summerclouds.common.restree.result;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;

import org.summerclouds.common.core.tool.MFile;
import org.summerclouds.common.restree.CallContext;
import org.summerclouds.common.restree.api.RestResult;

public class BinaryResult implements RestResult {

    private InputStream is;
    private String contentType;
    private Reader reader;
    private int returnCode = 0;

    public BinaryResult(InputStream is, String contentType) {
        this.contentType = contentType;
        this.is = is;
    }

    public BinaryResult(Reader reader, String contentType) {
        this.contentType = contentType;
        this.reader = reader;
    }

    @Override
    public void write(CallContext context, PrintWriter writer) throws Exception {
        if (is != null) {
            while (true) {
                int b = is.read();
                if (b < 0) break;
                writer.write(b);
            }
            is.close();
            is = null;
        } else if (reader != null) {
            MFile.copyFile(reader, writer);
            reader.close();
            reader = null;
            onClose();
        }
    }

    protected void onClose() {}

    @Override
    public String getContentType(CallContext context) {
        return contentType;
    }

    @Override
    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }
}
