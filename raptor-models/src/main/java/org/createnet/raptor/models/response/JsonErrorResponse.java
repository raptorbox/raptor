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
package org.createnet.raptor.models.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 *
 * @author Luca Capra <luca.capra@create-net.org>
 */
public class JsonErrorResponse {

    public static ResponseEntity<?> entity(HttpStatus code) {
        return entity(code, null);
    }

    public static ResponseEntity<?> entity(HttpStatus code, String message) {
        if (message == null || message.isEmpty()) {
            message = code.getReasonPhrase();
        }
        return ResponseEntity.status(code).body(new JsonError(code.value(), message));
    }

    public static ResponseEntity<?> notFound(String message) {
        return entity(HttpStatus.NOT_FOUND, message);
    }

    public static ResponseEntity<?> notFound() {
        return notFound(HttpStatus.NOT_FOUND.getReasonPhrase());
    }

    public static ResponseEntity<?> badRequest(String message) {
        return entity(HttpStatus.BAD_REQUEST, message);
    }

    public static ResponseEntity<?> badRequest() {
        return badRequest(HttpStatus.BAD_REQUEST.getReasonPhrase());
    }

    public static ResponseEntity<?> internalError(String message) {
        return entity(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public static ResponseEntity<?> internalError() {
        return badRequest(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    }

    public static ResponseEntity<?> unauthorized(String message) {
        return entity(HttpStatus.UNAUTHORIZED, message);
    }

    public static ResponseEntity<?> unauthorized() {
        return unauthorized(HttpStatus.UNAUTHORIZED.getReasonPhrase());
    }

    public static ResponseEntity<?> conflict(String message) {
        return entity(HttpStatus.CONFLICT, message);
    }

    public static ResponseEntity<?> conflict() {
        return entity(HttpStatus.CONFLICT, HttpStatus.CONFLICT.getReasonPhrase());
    }
}
