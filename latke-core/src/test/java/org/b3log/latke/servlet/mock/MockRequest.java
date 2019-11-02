/*
 * Copyright (c) 2009-present, b3log.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.latke.servlet.mock;

import io.netty.handler.codec.http.HttpRequest;
import org.b3log.latke.http.Request;

/**
 * Mock HTTP request.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.0, Nov 2, 2019
 */
public class MockRequest extends Request {

    private String requestURI;

    public MockRequest() {
        super(null);
    }

    public String getRequestURI() {
        return requestURI;
    }

    public void setRequestURI(final String requestURI) {
        this.requestURI = requestURI;
    }

}