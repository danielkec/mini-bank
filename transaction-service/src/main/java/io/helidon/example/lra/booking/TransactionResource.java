package io.helidon.example.lra.booking;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.lra.LRAResponse;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import static java.lang.System.Logger.Level.DEBUG;

@Path("/transaction")
@ApplicationScoped
public class TransactionResource {

    private static final System.Logger LOG = System.getLogger(TransactionResource.class.getName());

    @Inject
    @ConfigProperty(name = "account.service.url")
    private URI accountServiceUrl;

    @PUT
    @Path("/transfer")
    @LRA(value = LRA.Type.REQUIRES_NEW, timeLimit = 60)
    public Response transfer(@HeaderParam(LRA.LRA_HTTP_CONTEXT_HEADER) Optional<URI> lraId,
                             TransferOperation operation) {
        var withdrawOperation = new AccountOperation(operation.from(), operation.amount());
        try (var c = ClientBuilder.newClient()) {
            var target = c.target(accountServiceUrl);
            try (var res = target
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
            try (var res = target
                    .path("/account/enter")
                    .request()
                    .put(Entity.entity(enterOperation, MediaType.APPLICATION_JSON_TYPE))) {
                if (res.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                    return Response.status(res.getStatusInfo().getStatusCode())
                            .tag("Money enter failed.")
                            .build();
                }
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
