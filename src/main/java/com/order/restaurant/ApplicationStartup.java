package com.order.restaurant;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {
    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationStartup.class);
    private final String swaggerURL;

    @Autowired
    public ApplicationStartup(Environment environment) {
        swaggerURL = String.format(
                "http://localhost:%d%s",
                environment.getProperty("server.port", int.class),
                environment.getProperty("springdoc.swagger-ui.path")
        );

    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        LOGGER.info("Swagger UI is available at the link: " + swaggerURL);
    }
}
