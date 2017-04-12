/*
 * Copyright [2017] [Andy Moncsek]
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

package org.jacpfx.vertx.registry;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpFrame;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.net.NetSocket;
import java.util.List;

/**
 * Created by Andy Moncsek on 09.05.16.
 */
public class HttpClientResponseError implements HttpClientResponse {

  private final int code;

  public HttpClientResponseError(int errorCode) {
    code = errorCode;
  }

  @Override
  public HttpClientResponse resume() {
    return null;
  }

  @Override
  public HttpClientResponse exceptionHandler(Handler<Throwable> handler) {
    return null;
  }

  @Override
  public HttpClientResponse handler(Handler<Buffer> handler) {
    return null;
  }

  @Override
  public HttpClientResponse pause() {
    return null;
  }

  @Override
  public HttpClientResponse endHandler(Handler<Void> handler) {
    return null;
  }

  @Override
  public HttpVersion version() {
    return null;
  }

  @Override
  public int statusCode() {
    return code;
  }

  @Override
  public String statusMessage() {
    return null;
  }

  @Override
  public MultiMap headers() {
    return null;
  }

  @Override
  public String getHeader(String s) {
    return null;
  }

  @Override
  public String getHeader(CharSequence charSequence) {
    return null;
  }

  @Override
  public String getTrailer(String s) {
    return null;
  }

  @Override
  public MultiMap trailers() {
    return null;
  }

  @Override
  public List<String> cookies() {
    return null;
  }

  @Override
  public HttpClientResponse bodyHandler(Handler<Buffer> handler) {
    return null;
  }

  @Override
  public HttpClientResponse customFrameHandler(Handler<HttpFrame> handler) {
    return null;
  }

  @Override
  public NetSocket netSocket() {
    return null;
  }

  @Override
  public HttpClientRequest request() {
    return null;
  }


}
