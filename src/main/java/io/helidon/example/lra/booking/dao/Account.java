package io.helidon.example.lra.booking.dao;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;

@Entity(name = "Account")
@Access(AccessType.PROPERTY)
@NamedQueries({
        @NamedQuery(name = "getAccountByNumber", query = "SELECT a FROM Account a WHERE a.number = :number"),
        @NamedQuery(name = "getAllAccounts", query = "SELECT a FROM Account a")
})
public class Account implements Serializable {
    private Long id;
    private String number;
    private BigDecimal balance;

    private List<PendingOperation> operations;

    public void setId(Long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @OneToMany(mappedBy = "account", fetch = EAGER, cascade = ALL)
    public List<PendingOperation> getOperations() {
        return operations;
    }

    public void setOperations(List<PendingOperation> operations) {
        this.operations = operations;
    }
}
