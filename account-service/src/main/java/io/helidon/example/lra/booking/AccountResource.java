package io.helidon.example.lra.booking;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import io.helidon.example.lra.booking.dao.Account;
import io.helidon.example.lra.booking.dao.PendingOperation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.lra.LRAResponse;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;

import static java.lang.System.Logger.Level.INFO;

@Path("/account")
@ApplicationScoped
public class AccountResource {

    private static final System.Logger LOG = System.getLogger(AccountResource.class.getName());
    @Inject
    AccountService repository;

    @PUT
    @Path("/withdraw")
    @LRA(value = LRA.Type.REQUIRED, end = false)
    @Transactional
    public Response withdraw(@HeaderParam(LRA.LRA_HTTP_CONTEXT_HEADER) Optional<URI> lraId,
                             AccountOperation operation) {
        Account account = repository.getAccountByNumber(operation.number())
                .orElseThrow(() -> new NotFoundException("No account found! Account Number: " + operation.number()));

        BigDecimal balance = account.getBalance();
        if (balance.compareTo(operation.amount()) < 0) {
            return Response.status(409).tag("Not enough money").build();
        }

        balance = balance.subtract(operation.amount());
        account.setBalance(balance);
        PendingOperation po = new PendingOperation();
        po.setAmount(operation.amount());
        lraId.map(URI::toString).ifPresent(po::setLraId);
        account.getOperations().add(po);
        po.setAccount(account);

        LOG.log(INFO, "Account {0} balance: {1}", account.getNumber(), balance);
        repository.save(account);

        return Response.ok().build();
    }

    @PUT
    @Path("/enter")
    @LRA(value = LRA.Type.REQUIRED, end = false)
    @Transactional
    public Response enter(@HeaderParam(LRA.LRA_HTTP_CONTEXT_HEADER) Optional<URI> lraId,
                          AccountOperation operation) {
        Account account = repository.getAccountByNumber(operation.number())
                .orElseThrow(() -> new NotFoundException("No account found! Account Number: " + operation.number()));

        PendingOperation po = new PendingOperation();
        po.setAmount(operation.amount());
        po.setAccount(account);
        lraId.map(URI::toString).ifPresent(po::setLraId);
        account.getOperations().add(po);
        BigDecimal balance = account.getBalance();
        balance = balance.add(operation.amount());
        account.setBalance(balance);
        repository.save(account);
        LOG.log(INFO, "Account {0} balance: {1}", account.getNumber(), balance);

        return Response.ok().build();
    }

    @PUT
    @Path("/create/{number}")
    @Transactional
    public Response create(@PathParam("number") String accountNumber,
                           @HeaderParam("balance") Optional<BigDecimal> balance) {
        Account account = new Account();
        account.setNumber(accountNumber);
        balance.ifPresent(account::setBalance);
        repository.save(account);
        return Response.ok().build();
    }

    @GET
    @Transactional
    public Response list() {
        return Response.ok(repository.getAllAccounts()).build();
    }

    @Compensate
    @Transactional
    public Response compensate(URI lraId) {
        List<PendingOperation> pendingOperations = repository.getPendingOperationsByLraId(lraId.toString());
        for (PendingOperation po : pendingOperations) {
            Account account = po.getAccount();
            BigDecimal amount = po.getAmount();
            BigDecimal balance = account.getBalance();
            BigDecimal compensatedBalance = balance.add(amount);
            LOG.log(INFO, "Compensating cancelled pending operation {0}$ of account {1}", amount, account.getNumber());
            account.setBalance(compensatedBalance);
            repository.remove(po);
            repository.save(account);
            LOG.log(INFO, "Account {0}; balance: {1}", account.getNumber(), compensatedBalance);
        }
        return LRAResponse.compensated();
    }

    @Complete
    @Transactional
    public Response onComplete(URI lraId) {
        AccountService accountService = repository;
        for (PendingOperation po : repository.getPendingOperationsByLraId(lraId.toString())) {
            LOG.log(INFO, "Clearing pending operation for account {0}", po.getAccount().getNumber());
            accountService.remove(po);
        }
        return LRAResponse.completed();
    }

    public record AccountOperation(String number, BigDecimal amount) {
    }
}
