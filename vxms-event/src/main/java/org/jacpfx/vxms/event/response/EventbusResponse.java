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

package org.jacpfx.vxms.event.response;

import io.vertx.core.eventbus.Message;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.ExecutionStep;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.common.encoder.Encoder;
import org.jacpfx.vxms.common.throwable.ThrowableFutureConsumer;
import org.jacpfx.vxms.event.response.basic.ExecuteEventChainResponse;
import org.jacpfx.vxms.event.response.basic.ExecuteEventbusByteResponse;
import org.jacpfx.vxms.event.response.basic.ExecuteEventbusObjectResponse;
import org.jacpfx.vxms.event.response.basic.ExecuteEventbusStringResponse;

/**
 * Created by Andy Moncsek on 12.01.16. Fluent API to define a Task and to reply the request with
 * the output of your task.
 */
public class EventbusResponse {

  private final String methodId;
  private final VxmsShared vxmsShared;
  private final Throwable failure;
  private final Consumer<Throwable> errorMethodHandler;
  private final Message<Object> message;

  /**
   * The constructor to pass all needed members
   *
   * @param methodId the method identifier
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   *     objects per instance
   * @param failure the failure thrown while task execution
   * @param errorMethodHandler the error handler
   * @param message the event-bus message to respond to
   */
  public EventbusResponse(
      String methodId,
      VxmsShared vxmsShared,
      Throwable failure,
      Consumer<Throwable> errorMethodHandler,
      Message<Object> message) {
    this.methodId = methodId;
    this.vxmsShared = vxmsShared;
    this.failure = failure;
    this.errorMethodHandler = errorMethodHandler;
    this.message = message;
  }

  /**
   * Switch to blocking mode
   *
   * @return {@link EventbusResponseBlocking}
   */
  public EventbusResponseBlocking blocking() {
    return new EventbusResponseBlocking(methodId, message, vxmsShared, failure, errorMethodHandler);
  }

  /**
   * starts a supply chain to create a response
   *
   * @param chainconsumer the initial supplier
   * @param <T> the type of the return value
   * @return {@link ExecuteEventChainResponse}
   */
  public <T> ExecuteEventChainResponse<T> supply(ThrowableFutureConsumer<T> chainconsumer) {
    final List<ExecutionStep> chain = new ArrayList<>();
    chain.add(new ExecutionStep(chainconsumer));
    return new ExecuteEventChainResponse<>(
        methodId, vxmsShared, failure, errorMethodHandler, message, chain);
  }

  /**
   * Returns a byte array to the target type
   *
   * @param byteConsumer consumes a io.vertx.core.Future to complete with a byte response
   * @return {@link ExecuteEventbusByteResponse}
   */
  public ExecuteEventbusByteResponse byteResponse(ThrowableFutureConsumer<byte[]> byteConsumer) {
    return new ExecuteEventbusByteResponse(
        methodId, vxmsShared, failure, errorMethodHandler, message, null, byteConsumer);
  }

  /**
   * Returns a String to the target type
   *
   * @param stringConsumer consumes a io.vertx.core.Future to complete with a String response
   * @return {@link ExecuteEventbusStringResponse}
   */
  public ExecuteEventbusStringResponse stringResponse(
      ThrowableFutureConsumer<String> stringConsumer) {
    return new ExecuteEventbusStringResponse(
        methodId, vxmsShared, failure, errorMethodHandler, message, null, stringConsumer);
  }

  /**
   * Returns a Serializable to the target type
   *
   * @param objectConsumer consumes a io.vertx.core.Future to complete with a Serialized Object
   *     response
   * @param encoder the encoder to serialize the response object
   * @return {@link ExecuteEventbusObjectResponse}
   */
  public ExecuteEventbusObjectResponse objectResponse(
      ThrowableFutureConsumer<Serializable> objectConsumer, Encoder encoder) {
    return new ExecuteEventbusObjectResponse(
        methodId, vxmsShared, failure, errorMethodHandler, message, null, objectConsumer, encoder);
  }
}
