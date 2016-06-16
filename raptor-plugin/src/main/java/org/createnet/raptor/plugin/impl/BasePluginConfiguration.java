/*
 * Copyright 2016 CREATE-NET http://create-net.org
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
package org.createnet.raptor.plugin.impl;

import org.createnet.raptor.plugin.PluginConfiguration;


/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 * @param <T> The configuration type for that plugin to be mapped to a yml file. Set to null to disable configuration loading
 * 
 */
public class BasePluginConfiguration<T> implements PluginConfiguration {

  final private Class<T> clazz;
  final private String name;
  final private String basePath;

  public BasePluginConfiguration(String name) {
    this(name, null, null);
  }
  
  public BasePluginConfiguration(String name, Class<T> clazz) {
    this(name, clazz, null);
  }
  
  public BasePluginConfiguration(String name, Class<T> clazz, String basePath) {
    this.name = name;
    this.clazz = clazz;
    this.basePath = basePath;
  }
  
  @Override
  public String getName() {
    return name;
  };

  @Override
  public String getPath() {
    
    return (this.basePath == null) ? "" : this.basePath + this.getName() + ".yml";
  }

  @Override
  public Class<T> getConfigurationClass() {
    return clazz;
  }

  @Override
  public String toString() {
    return "Plugin " + getName() + (getConfigurationClass() != null ? "(" + getConfigurationClass().getName() + ")" : "");
  }
  
}
