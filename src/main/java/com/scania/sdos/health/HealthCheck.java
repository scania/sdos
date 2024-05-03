package com.scania.sdos.health;

import com.scania.sdos.model.ServiceArguments;
import com.scania.sdos.utils.SDOSConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * The HealthCheck class is a customized Health Check that sends the service ID.
 */
@Component
public class HealthCheck implements HealthIndicator {

  private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheck.class);
  private final ServiceArguments service;

  public HealthCheck(ServiceArguments service) {
    this.service = service;
  }

  /**
   * Return an indication of health and the service ID.
   *
   * @return the health of the service and the service ID.
   */
  @Override
  public Health health() {
    LOGGER.trace("Health Check Called.");
    Object serviceId = service.getServiceId();
    return Health.up().withDetail(SDOSConstants.SERVICEID, serviceId).build();
  }
}
