package ethio.com.models;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "customerTransactions", catalog = "USSDV2_3")
public class CustomerTransactions {
    @Id
    @Column(insertable = false)
    private int id;
    @Column
    private String source;
    @Column
    private String phone;
    @Column
    private String account;
    @Column
    private double amount;
    @Column
    private String type;
    @Column
    private String currency;
    @Column(updatable = false,insertable = false,nullable = false,columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp updatedOn;

    public CustomerTransactions(String source, String phone, String account, double amount, String type, String currency) {
        this.source = source;
        this.phone = phone;
        this.account = account;
        this.amount = amount;
        this.type = type;
        this.currency = currency;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Timestamp getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Timestamp updatedOn) {
        this.updatedOn = updatedOn;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
