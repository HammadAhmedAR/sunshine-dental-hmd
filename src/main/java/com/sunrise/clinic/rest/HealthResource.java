package com.sunrise.clinic.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Application liveness only; intentionally does not claim database readiness. */
@Path("/health")
public class HealthResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String health() {
        return "{\"status\":\"UP\",\"application\":\"Sunrise Dental Clinic\"}";
    }
}
