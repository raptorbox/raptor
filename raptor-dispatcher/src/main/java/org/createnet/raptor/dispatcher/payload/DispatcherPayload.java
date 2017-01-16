/*
 * Copyright 2017 Luca Capra <luca.capra@fbk.eu>.
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
package org.createnet.raptor.dispatcher.payload;

/**
 *
 * @author Luca Capra <luca.capra@fbk.eu>
 */
public interface DispatcherPayload {

    public enum MessageType {
        object, stream, action, data, 
        user, permission, role
    }
    
    /**
     * Return the type of message
     * 
     * @return type of message
     */
    public MessageType getType();
    
    /**
     * Return the operation label
     * 
     * @return label of the operation
     */    
    public String getOp();
    
    @Override
    public String toString();
}
