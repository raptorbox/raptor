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
package org.createnet.raptor.api.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.createnet.raptor.api.common.dispatcher.RaptorMessageHandler;
import org.createnet.raptor.models.configuration.RaptorConfiguration;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.createnet.raptor.models.payload.DispatcherPayload;
import org.createnet.raptor.sdk.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@Profile("default")
@EnableAutoConfiguration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public abstract class BaseApplication {

    final String configFile = "/etc/raptor/raptor.yml";

    static public Logger log;
    static final public ObjectMapper mapper = new ObjectMapper();

    final private static ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    static private ConfigurableApplicationContext instance;
    static public String appName;

    protected RaptorMessageHandler messageHandler;

    public static void close() {
        if (instance != null) {
            instance.close();
        }
    }

    public static void start(Class clazz, String[] args) {
        if (instance == null) {

            log = LoggerFactory.getLogger(clazz);

            String[] parts = clazz.getCanonicalName().split("\\.");
            appName = parts[parts.length - 2];
            String name = "--spring.config.name=" + appName;

            String[] args2 = new String[args.length + 1];
            System.arraycopy(args, 0, args2, 0, args.length);
            args2[args2.length - 1] = name;

            instance = SpringApplication.run(clazz, args2);
        }
    }

    @Bean
    @ConfigurationProperties(prefix = "raptor")
    public RaptorConfiguration getConfiguration() {
        return new RaptorConfiguration();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .maxAge(3600);
            }
        };
    }

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {

        RaptorConfiguration.Dispatcher config = getConfiguration().getDispatcher();

        DefaultMqttPahoClientFactory f = new DefaultMqttPahoClientFactory();

        f.setUserName(config.getUsername());
        f.setPassword(config.getPassword());
        f.setServerURIs(config.getUri());
        f.setCleanSession(true);
        f.setPersistence(new MemoryPersistence());

        return f;
    }

    // Add inbound MQTT support
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    /**
     * Create a MQTT connection to the broker listening to all events
     *
     * @param messageHandler
     * @return
     */
    public MessageProducer createMqttClient(RaptorMessageHandler messageHandler) {
        return createMqttClient(messageHandler, String.format(Topics.DEVICE, "+"));
    }

    /**
     * Create a MQTT connection to the broker
     *
     * @param messageHandler
     * @param topic
     * @return
     */
    public MessageProducer createMqttClient(RaptorMessageHandler messageHandler, String topic) {

        this.messageHandler = messageHandler;

        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(this.getClass().getName().replace(".", "-"), mqttClientFactory(), topic);
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(0);
        adapter.setRecoveryInterval(1000);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return (Message<?> message) -> {
            if (messageHandler != null) {
                messageHandler.handle((DispatcherPayload) message.getPayload());
            }
        };
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();

        propertySourcesPlaceholderConfigurer.setIgnoreResourceNotFound(false);

        final List<Resource> resourceLst = new ArrayList();

        resourceLst.add(new FileSystemResource("/etc/raptor/raptor.yml"));
        resourceLst.add(new FileSystemResource("/etc/raptor/" + appName + ".yml"));

        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(resourceLst.toArray(new Resource[]{}));

        propertySourcesPlaceholderConfigurer.setProperties(yaml.getObject());
        return propertySourcesPlaceholderConfigurer;
    }

}
