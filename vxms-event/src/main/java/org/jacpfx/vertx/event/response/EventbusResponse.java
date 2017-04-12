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

package org.jacpfx.vertx.event.response;

import io.vertx.core.eventbus.Message;
import java.io.Serializable;
import java.util.function.Consumer;
import org.jacpfx.common.VxmsShared;
import org.jacpfx.common.encoder.Encoder;
import org.jacpfx.common.throwable.ThrowableFutureConsumer;
import org.jacpfx.vertx.event.response.basic.ExecuteEventbusBasicByteResponse;
import org.jacpfx.vertx.event.response.basic.ExecuteEventbusBasicObjectResponse;
import org.jacpfx.vertx.event.response.basic.ExecuteEventbusBasicStringResponse;

/**
 * Created by Andy Moncsek on 12.01.16.
 * Fluent API to define a Task and to reply the request with the output of your task.
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
   * @param message the event-bus message to respond to
   * @param vxmsShared the vxmsShared instance, containing the Vertx instance and other shared
   * objects per instance
   * @param failure the failure thrown while task execution
   * @param errorMethodHandler the error handler
   */
  public EventbusResponse(String methodId, Message<Object> message, VxmsShared vxmsShared,
      Throwable failure, Consumer<Throwable> errorMethodHandler) {
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
   * Returns a byte array to the target type
   *
   * @param byteConsumer consumes a io.vertx.core.Future to compleate with a byte response
   * @return {@link ExecuteEventbusBasicByteResponse}
   */
  public ExecuteEventbusBasicByteResponse byteResponse(
      ThrowableFutureConsumer<byte[]> byteConsumer) {
    return new ExecuteEventbusBasicByteResponse(methodId, vxmsShared, failure, errorMethodHandler,
        message, byteConsumer, null, null, null, null, 0, 0l, 0l);
  }

  /**
   * Returns a String to the target type
   *
   * @param stringConsumer consumes a io.vertx.core.Future to compleate with a String response
   * @return {@link ExecuteEventbusBasicStringResponse}
   */
  public ExecuteEventbusBasicStringResponse stringResponse(
      ThrowableFutureConsumer<String> stringConsumer) {
    return new ExecuteEventbusBasicStringResponse(methodId, vxmsShared, failure, errorMethodHandler,
        message, stringConsumer, null, null, null, null, 0, 0l, 0l);
  }

  /**
   * Returns a Serializable to the target type
   *
   * @param objectConsumer consumes a io.vertx.core.Future to compleate with a Serialized Object
   * response
   * @param encoder the encoder to serialize the response object
   * @return {@link ExecuteEventbusBasicObjectResponse}
   */
  public ExecuteEventbusBasicObjectResponse objectResponse(
      ThrowableFutureConsumer<Serializable> objectConsumer, Encoder encoder) {
    return new ExecuteEventbusBasicObjectResponse(methodId, vxmsShared, failure, errorMethodHandler,
        message, objectConsumer, null, encoder, null, null, null, 0, 0l, 0l);
  }


}
