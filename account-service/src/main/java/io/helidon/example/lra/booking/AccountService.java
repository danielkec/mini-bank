package io.helidon.example.lra.booking;

import io.helidon.example.lra.booking.dao.Account;
import io.helidon.example.lra.booking.dao.PendingOperation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AccountService {

    @PersistenceContext(unitName = "accounts")
    protected EntityManager entityManager;

    Optional<Account> getAccountByNumber(String accountNumber) {
        return entityManager.createNamedQuery("getAccountByNumber", Account.class)
                .setParameter("number", accountNumber)
                .getResultStream()
                .findFirst();
    }

    List<PendingOperation> getPendingOperationsByLraId(String lraId) {
        return entityManager.createNamedQuery("getPendingOperationByLraId", PendingOperation.class)
                .setParameter("lraId", lraId)
                .getResultList();
    }

    public List<Account> getAllAccounts() {
        return entityManager.createNamedQuery("getAllAccounts", Account.class)
                .getResultList();
    }

    void save(Account account) {
        entityManager.persist(account);
    }

    public void remove(PendingOperation po) {
        po.getAccount().getOperations().remove(po);
        entityManager.persist(po.getAccount());
        entityManager.remove(po);
    }
}
