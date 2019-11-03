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
package org.b3log.latke.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * HTTP response.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Nov 3, 2019
 * @since 2.5.9
 */
public class Response {

    private ChannelHandlerContext ctx;
    private HttpRequest req;
    private HttpResponse res;
    private boolean commited;
    private byte[] content;
    private List<Cookie> cookies;

    public Response(final ChannelHandlerContext ctx, final HttpRequest req, final HttpResponse res) {
        this.ctx = ctx;
        this.req = req;
        this.res = res;
        cookies = new ArrayList<>();
    }

    public boolean isCommitted() {
        return commited;
    }

    public String getHeader(final String name) {
        return res.headers().get(name);
    }

    public void addHeader(final String name, final String value) {
        res.headers().add(name, value);
    }

    public void setHeader(final String name, final String value) {
        res.headers().set(name, value);
    }

    public void setStatus(final int status) {
        res.setStatus(HttpResponseStatus.valueOf(status));
    }

    public String getContentType() {
        return res.headers().get(HttpHeaderNames.CONTENT_TYPE);
    }

    public void setContentType(final String contentType) {
        res.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
    }

    public Iterator<String> getHeaderNames() {
        return res.headers().names().iterator();
    }

    public void addCookie(final Cookie cookie) {
        cookies.add(cookie);
    }

    public void sendError(final int status) {
        setStatus(status);
        writeResponse();
    }

    public void sendRedirect(final String location) {
        setHeader(HttpHeaderNames.LOCATION.toString(), location);
        setStatus(HttpResponseStatus.FOUND.code());
        writeResponse();
    }

    public void sendContent(final byte[] content) {
        this.content = content;
        writeResponse();
    }

    private void writeResponse() {
        ByteBuf contentBuf = Unpooled.EMPTY_BUFFER;
        if (null != content) {
            contentBuf = Unpooled.copiedBuffer(content);
        }
        res = ((FullHttpResponse) res).replace(contentBuf);
        final boolean keepAlive = HttpUtil.isKeepAlive(req);
        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            res.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, ((FullHttpResponse) res).content().readableBytes());
            // Add keep alive header as per:
            // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            res.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        // Encode the cookie.
        final String cookieString = req.headers().get(HttpHeaderNames.COOKIE);
        if (cookieString != null) {
            Set<io.netty.handler.codec.http.cookie.Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieString);
            if (!cookies.isEmpty()) {
                // Reset the cookies if necessary.
                for (io.netty.handler.codec.http.cookie.Cookie cookie : cookies) {
                    res.headers().add(HttpHeaderNames.SET_COOKIE, io.netty.handler.codec.http.cookie.ServerCookieEncoder.STRICT.encode(cookie));
                }
            }
        } else {
            // Browser sent no cookie.  Add some.
            res.headers().add(HttpHeaderNames.SET_COOKIE, io.netty.handler.codec.http.cookie.ServerCookieEncoder.STRICT.encode("key1", "value1"));
            res.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode("key2", "value2"));
        }

        commited = true;

        if (null != ctx) {
            ctx.write(res);
            if (!keepAlive) {
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }
}