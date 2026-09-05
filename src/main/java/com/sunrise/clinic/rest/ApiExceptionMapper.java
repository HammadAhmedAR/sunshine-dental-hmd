package com.sunrise.clinic.rest;

import com.sunrise.clinic.service.*;
import jakarta.json.Json;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;
import java.sql.SQLException;
import java.util.logging.Logger;

/** Never serializes exception messages containing database or container details. */
@Provider
public class ApiExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger LOG = Logger.getLogger(ApiExceptionMapper.class.getName());
    @Override public Response toResponse(Exception error) {
        int status;
        String message;
        if (error instanceof RecordNotFoundException) { status = 404; message = "Appointment not found."; }
        else if (error instanceof ValidationException) { status = 400; message = error.getMessage(); }
        else if (error instanceof SQLException sql) {
            status = 503; message = "The service is temporarily unavailable.";
            LOG.warning("API database failure; SQLState=" + sql.getSQLState());
        } else if (error instanceof WebApplicationException web) {
            status = web.getResponse().getStatus(); message = "The API request could not be completed.";
        } else {
            status = 500; message = "The request could not be completed.";
            LOG.warning("API request failure: " + error.getClass().getName());
        }
        return Response.status(status).type(MediaType.APPLICATION_JSON)
                .entity(Json.createObjectBuilder().add("error", message).build().toString()).build();
    }
}
