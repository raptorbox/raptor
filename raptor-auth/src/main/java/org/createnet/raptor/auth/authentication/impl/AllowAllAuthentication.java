/*
 * Copyright 2016 CREATE-NET
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
package org.createnet.raptor.auth.authentication.impl;

import org.createnet.raptor.auth.authentication.AbstractAuthentication;
import org.createnet.raptor.auth.authentication.Authentication;
import org.createnet.raptor.auth.authorization.impl.AllowAllAuthorization;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class AllowAllAuthentication extends AbstractAuthentication {

  @Override
  public Authentication.UserInfo getUser(String accessToken) throws AuthenticationException {
    return new Authentication.UserInfo(AllowAllAuthorization.defaultUserId, AllowAllAuthorization.defaultAccessToken);
  }
  
}
