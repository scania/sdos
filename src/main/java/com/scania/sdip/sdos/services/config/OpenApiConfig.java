package com.scania.sdip.sdos.services.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(OpenApiConfig.class);
  @Value("${spring.application.name}")
  private String applicationName;
  @Value("${sdos.swagger.desc}")
  private String swaggerDesc;
  @Value("${sdos.swagger.contact.name}")
  private String name;
  @Value("${sdos.swagger.contact.url}")
  private String url;
  @Value("${sdos.swagger.contact.email}")
  private String email;
  @Value("${version_tag:LOCAL}")
  private String version;
  @Value("${service_url:LOCAL}")
  private String service_url;

  @Bean
  public OpenAPI customOpenAPI() {
    LOGGER.debug("Loading Swagger Configuration.");
    if (service_url.equals("LOCAL")) {
      return new OpenAPI().components(new Components()).info(
          new Info().title(applicationName).description(swaggerDesc).version(version)
              .contact(new Contact().name(name).email(email).url(url)).version(version));

    } else {
      return new OpenAPI().addServersItem(new Server().url(service_url))
          .components(new Components()).info(
              new Info().title(applicationName).description(swaggerDesc).version(version)
                  .contact(new Contact().name(name).email(email).url(url)).version(version));

    }

  }
}
