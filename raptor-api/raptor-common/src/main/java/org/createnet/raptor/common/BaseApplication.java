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
package org.createnet.raptor.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.createnet.raptor.common.dispatcher.RaptorMessageHandler;
import org.createnet.raptor.models.configuration.AuthConfiguration;
import org.createnet.raptor.models.configuration.DispatcherConfiguration;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.Banner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.messaging.MessagingException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

/**
 *
 * @author Luca Capra <luca.capra@gmail.com>
 */
@Profile("default")
@EnableAutoConfiguration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableScheduling
public abstract class BaseApplication {

    static public Logger log = null;

    static final String defaultBasePath = "/etc/raptor/";
    static String basepath = defaultBasePath;
    
    static final public ObjectMapper mapper = new ObjectMapper();

    static private ConfigurableApplicationContext instance;
    static public String appName;

    static public boolean developmentMode = false;

    protected RaptorMessageHandler messageHandler;

    public static SpringApplicationBuilder createInstance(Class clazz) {

        if (log == null) {
            log = LoggerFactory.getLogger(clazz);
        }

        return new SpringApplicationBuilder()
                .addCommandLineProperties(true)
                .bannerMode(Banner.Mode.OFF)
                .registerShutdownHook(true)
                .sources(clazz)
                .web(true);
    }

    public static void start(SpringApplicationBuilder builder, String[] args) {
        if (instance == null) {
            instance = builder.run(args);
        }
    }

    public static void start(Class clazz, String[] args) {
        if (instance == null) {
            createInstance(clazz).run(buildArgs(clazz, args));
        }
    }

    public static ConfigurableApplicationContext getInstance() {
        return instance;
    }

    public static void close() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }

    static public String[] buildArgs(Class clazz, String[] args) {

        String[] parts = clazz.getCanonicalName().split("\\.");
        appName = parts[parts.length - 2];
        String name = "--spring.config.name=" + appName;

        log.debug("Set application name to match package: {}", appName);
        log.debug("Listening to path /{}", appName);

        for (String arg : args) {
            if (arg.equals("--dev")) {
                log.debug("Development mode enabled");
                developmentMode = true;
            }
        }
        
        String[] args2 = new String[args.length + 1];
        System.arraycopy(args, 0, args2, 0, args.length);
        args2[args2.length - 1] = name;

        return args2;
    }
    
    static public boolean isDevelopmentMode() {
        return developmentMode;
    }
    
    @Bean
    @ConfigurationProperties(prefix = "raptor")
    public RaptorConfiguration raptorConfiguration() {
        return new RaptorConfiguration();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {

                registry.addResourceHandler("swagger-ui.html")
                        .addResourceLocations("classpath:/META-INF/resources/");

                registry.addResourceHandler("/webjars/**")
                        .addResourceLocations("classpath:/META-INF/resources/webjars/");
            }

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

        AuthConfiguration.AdminUser defaultUser = raptorConfiguration().getAuth().getServiceUser();

        if (defaultUser == null) {
            throw new RuntimeException("Missing service user. Review raptor.yml configuration file under auth.users section");
        }

        DispatcherConfiguration dispatcherConfig = raptorConfiguration().getDispatcher();

        DefaultMqttPahoClientFactory f = new DefaultMqttPahoClientFactory();

        log.debug("Using local broker user {}", defaultUser.getUsername());

        f.setUserName(defaultUser.getUsername());
        f.setPassword(defaultUser.getPassword());
        f.setServerURIs(dispatcherConfig.getUri());
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
     * Create a MQTT connection to the broker
     *
     * @param messageHandler
     * @param topics
     * @return
     */
    public MessageProducer createMqttClient(String[] topics, RaptorMessageHandler messageHandler) {

        this.messageHandler = messageHandler;

        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(this.getClass().getName().replace(".", "-"), mqttClientFactory(), topics);
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(2);
        adapter.setRecoveryInterval(1000);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                if (messageHandler != null) {
                    try {
                        DispatcherPayload payload = DispatcherPayload.parseJSON(message.getPayload().toString());
                        messageHandler.handle(payload, message.getHeaders());
                    } catch (Exception e) {
                        throw new MessagingException("Exception handling message", e);
                    }
                }
            }
        };
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();

        propertySourcesPlaceholderConfigurer.setIgnoreResourceNotFound(false);

        ArrayList<String> defaultSources = new ArrayList(Arrays.asList("raptor.yml", appName + ".yml"));
        ArrayList<String> sources = new ArrayList(defaultSources);
        
        if (developmentMode) {
            defaultSources.forEach((source) -> {
                sources.add(source.replace(".yml", ".dev.yml"));
            });
        }

        try {
            basepath = System.getenv("CONFIG_BASEPATH");
        }
        catch(Exception e) {
            log.warn("Failed to read environment variable CONFIG_BASEPATH: %s", e.getMessage());
        }
        
        List<Resource> resources = sources.stream()
                .filter(f -> new File(basepath + f).exists())
                .map(f -> new FileSystemResource(basepath + f))
                .collect(Collectors.toList());

        log.debug("Configuration sources: {}", resources.toString());

        if (resources.isEmpty()) {
            throw new RuntimeException("Cannot find a loadable property file in: " + basepath);
        }

        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(resources.toArray(new Resource[]{}));

        propertySourcesPlaceholderConfigurer.setProperties(yaml.getObject());
        return propertySourcesPlaceholderConfigurer;
    }

}
