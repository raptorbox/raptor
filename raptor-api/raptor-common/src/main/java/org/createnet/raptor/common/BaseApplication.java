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

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.createnet.raptor.common.dispatcher.RaptorMessageHandlerWrapper;
import org.createnet.raptor.models.configuration.RaptorConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.Banner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableScheduling;

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
    static boolean enableDebugLogging = false;

    static protected List<String> additionalConfigNames = null;

    static final public ObjectMapper mapper = new ObjectMapper();

    static private ConfigurableApplicationContext instance;
    static public String appName;

    static public boolean developmentMode = false;

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
            //enable debug log if requested
            enableDebugLogging();
        }
    }

    public static void start(Class clazz, String[] args) {
        if (instance == null) {
            createInstance(clazz).run(buildArgs(clazz, args));
            //enable debug log if requested
            enableDebugLogging();
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
//        log.debug("Listening to path /{}", appName);

        for (String arg : args) {
            if (arg.equals("--dev")) {
                log.debug("Development mode enabled");
                developmentMode = true;
            }
            if (arg.equals("--debug")) {
                log.debug("Debug logging enabled");
                enableDebugLogging = true;
            }
        }

        String[] args2 = new String[args.length + 1];
        System.arraycopy(args, 0, args2, 0, args.length);
        args2[args2.length - 1] = name;

        return args2;
    }

    static public void enableDebugLogging() {
        if (enableDebugLogging) {

            ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.INFO);

            ch.qos.logback.classic.Logger raptorLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.createnet.raptor");
            raptorLogger.setLevel(Level.DEBUG);

        }
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
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .maxAge(3600);
            }
        };
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();

        propertySourcesPlaceholderConfigurer.setIgnoreResourceNotFound(false);

        ArrayList<String> defaultSources = new ArrayList(Arrays.asList("raptor.yml", appName + ".yml"));

        if (additionalConfigNames != null && !additionalConfigNames.isEmpty()) {
            defaultSources.addAll(additionalConfigNames);
        }

        ArrayList<String> sources = new ArrayList(defaultSources);

        if (developmentMode) {
            defaultSources.forEach((source) -> {
                sources.add(source.replace(".yml", ".dev.yml"));
            });
        }

        try {
            String envPath = System.getenv("CONFIG_BASEPATH");
            if (envPath != null && envPath.isEmpty()) {
                log.debug("Using CONFIG_BASEPATH={}", envPath);
                basepath = envPath;
            }
        } catch (Exception e) {
            log.warn("Failed to read environment variable CONFIG_BASEPATH: %s", e.getMessage());
        }

        log.debug("Configuration path {}", basepath);

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

    @Bean
    protected RaptorMessageHandlerWrapper raptorMessageHandlerWrapper() {
        return new RaptorMessageHandlerWrapper();
    }

    @Autowired
    MqttPahoClientFactory mqttClientFactory;
    @Autowired
    MessageChannel mqttInputChannel;

    /**
     * Create a MQTT connection to the broker
     *
     * @param messageHandler
     * @param topics
     * @return
     */
    public MessageProducer createMqttClient(String[] topics) {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter("raptor" + (System.currentTimeMillis() + Math.random()), mqttClientFactory, topics);
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(2);
        adapter.setRecoveryInterval(2500);
        adapter.setOutputChannel(mqttInputChannel);

        return adapter;
    }

}
