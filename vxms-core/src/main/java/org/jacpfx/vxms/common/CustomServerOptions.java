/*
 * Copyright [2018] [Andy Moncsek]
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

package org.jacpfx.vxms.common;

import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;

/**
 * Created by Andy Moncsek on 18.07.16.
 * Interface for custom http server option definition
 */
public interface CustomServerOptions {

  /**
   * Return the HttpServerOptions for a specific vxms service
   * @param config the verticle configuration object
   * @return the serverOptions {@link io.vertx.core.http.HttpServerOptions}
   */
  default HttpServerOptions getServerOptions(JsonObject config) {
    return new HttpServerOptions();
  }
}
