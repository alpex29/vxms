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

package org.jacpfx.vertx.event;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import or.jacpfx.spi.EventhandlerSPI;
import org.jacpfx.common.VxmsShared;

/**
 * Created by amo on 05.08.16.
 */
public class Eventhandler implements EventhandlerSPI {
    @Override
    public void initEventHandler(VxmsShared vxmsShared,  AbstractVerticle service) {
        EventInitializer.initEventbusHandling(vxmsShared,  service);
    }
}
