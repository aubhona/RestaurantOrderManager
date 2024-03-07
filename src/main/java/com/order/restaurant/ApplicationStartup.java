package com.order.restaurant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {
    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationStartup.class);
    private final static String SWAGGER_URL = "http://localhost:8080/swagger-ui-custom.html";

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        LOGGER.info("Swagger UI is available at the link: " + SWAGGER_URL);
    }
}
