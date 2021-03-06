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

package org.jacpfx.entity;

import java.io.Serializable;

/** Created by Andy Moncsek on 25.11.15. */
public class Payload<T extends Serializable> implements Serializable {

  private final T value;

  public Payload(T value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Payload)) {
      return false;
    }

    Payload<?> payload = (Payload<?>) o;

    return !(value != null ? !value.equals(payload.value) : payload.value != null);
  }

  public T getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    return value != null ? value.hashCode() : 0;
  }
}
