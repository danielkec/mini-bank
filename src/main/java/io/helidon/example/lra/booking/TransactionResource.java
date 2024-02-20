package io.helidon.example.lra.booking;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.lra.LRAResponse;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;

@Path("/transaction")
@ApplicationScoped
public class TransactionResource {

    private static final System.Logger LOG = System.getLogger(TransactionResource.class.getName());
    private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

    @PUT
    @Path("/transfer")
    @LRA(value = LRA.Type.REQUIRES_NEW, timeLimit = 30)
    public Response transfer(@HeaderParam(LRA.LRA_HTTP_CONTEXT_HEADER) Optional<URI> lraId,
                             TransferOperation operation) {

        var withdrawOperation = new AccountOperation(operation.from(), operation.amount());
        try (var c = ClientBuilder.newClient();
             var res = c.target("http://localhost:8080")
                     .path("/account/withdraw")
                     .request()
                     .put(Entity.entity(withdrawOperation, MediaType.APPLICATION_JSON_TYPE))) {
            if (res.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                return Response.status(res.getStatusInfo().getStatusCode())
                        .tag("Money withdrawal failed.")
                        .build();
            }
        }

        var enterOperation = new AccountOperation(operation.to(), operation.amount());
        try (var c = ClientBuilder.newClient();
             var res = c.target("http://localhost:8080")
                     .path("/account/enter")
                     .request()
                     .put(Entity.entity(enterOperation, MediaType.APPLICATION_JSON_TYPE))) {
            if (res.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                return Response.status(res.getStatusInfo().getStatusCode())
                        .tag("Money enter failed.")
                        .build();
            }
        }

        return Response.accepted().build();
    }

    @Compensate
    public Response compensate(URI lraId) {
        LOG.log(DEBUG, "LRA ID: {0} failed!", lraId);
        return LRAResponse.compensated();
    }

    public record TransferOperation(BigDecimal amount, String from, String to) {
    }

    public record AccountOperation(String number, BigDecimal amount) {
    }
}
