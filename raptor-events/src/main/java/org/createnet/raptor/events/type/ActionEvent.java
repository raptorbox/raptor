/*
 * Copyright 2017 FBK/CREATE-NET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.createnet.raptor.events.type;

import org.createnet.raptor.models.data.ActionStatus;
import org.createnet.raptor.models.objects.Action;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class ActionEvent  extends ObjectEvent {
  
  final private Action action;
  final private ActionStatus status;
  
  public ActionEvent(Action action, ActionStatus status) {
    super(action.getServiceObject());
    this.action = action;
    this.status = status;
  }

  public Action getAction() {
    return action;
  }

  public ActionStatus getActionStatus() {
    return status;
  }
  
}
