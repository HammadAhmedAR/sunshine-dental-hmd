package com.sunrise.clinic.rest;

import com.sunrise.clinic.config.Services;
import com.sunrise.clinic.model.*;
import com.sunrise.clinic.service.QueryValidation;
import jakarta.json.*;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.sql.SQLException;

/** Read-only staff API; uses the same validated queries as the HTML workflow. */
@Path("/appointments")
@Produces(MediaType.APPLICATION_JSON)
public class AppointmentResource {
    @Context private ServletContext context;

    @GET @Path("/{number}")
    public Response find(@PathParam("number") String number) throws SQLException {
        return Response.ok(json(Services.get(context).appointmentQueries().find(number)).toString()).build();
    }

    @GET
    public Response list(@QueryParam("date") String date, @QueryParam("page") String page) throws SQLException {
        QueryValidation.date(date, true);
        AppointmentPage result = Services.get(context).appointmentQueries().list(date, null, page);
        JsonArrayBuilder items = Json.createArrayBuilder();
        result.appointments().forEach(a -> items.add(json(a)));
        return Response.ok(Json.createObjectBuilder().add("appointments", items)
                .add("page", result.page()).add("hasNext", result.hasNext()).build().toString()).build();
    }

    private static JsonObject json(AppointmentDetails a) {
        return Json.createObjectBuilder().add("appointmentNumber", a.appointmentNumber())
                .add("patientName", a.patientName()).add("address", a.address() == null ? JsonValue.NULL : Json.createValue(a.address())).add("phone", a.phone())
                .add("dentist", a.dentistName()).add("treatment", a.treatmentName())
                .add("date", a.getDate()).add("time", a.getTime()).add("status", a.status().name())
                .add("treatmentFee", a.treatmentFee()).add("billed", a.billed()).build();
    }
}
