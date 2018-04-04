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

package org.jacpfx.vxms.k8s.api;

import io.fabric8.kubernetes.client.Config;
import io.vertx.core.Vertx;

/**
 * interface to define a custom implementation for a kubernetes client configuration
 */
public interface CustomClientConfig {

  /**
   * Return a custom client implementation to connect to the kubernetes Master
   * @param vertx the vert.x instance
   * @return the configuration object
   */
  default Config createCustomConfiguration(Vertx vertx){
    return  null;
  }

}
