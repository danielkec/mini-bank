package io.helidon.example.lra.booking.dao;


import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;

import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URI;

@Entity(name = "PendingOperation")
@Access(AccessType.PROPERTY)
@NamedQueries({
        @NamedQuery(name = "getPendingOperationByLraId",
                query = "SELECT po FROM PendingOperation po WHERE po.lraId = :lraId")
})
public class PendingOperation implements Serializable {
    private Long id;
    private String lraId;
    private BigDecimal amount;
    private Account account;

    public void setId(Long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public String getLraId() {
        return lraId;
    }

    public void setLraId(String lraId) {
        this.lraId = lraId;
    }


    @ManyToOne
    @JoinColumn(name="account_id", nullable=false)
    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
