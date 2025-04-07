package ethio.com.dao;

import ethio.com.models.CustomerTransactions;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Stateless
public class CustomerTransactionsDao {
    @PersistenceContext(unitName = "primary")
    EntityManager em;

    @Transactional
    public boolean persistTransaction(String phone,String account, double amount, String currency) {
        try {
            boolean status;
            CustomerTransactions transaction = new CustomerTransactions("TOPUP", phone, account, amount, "D", currency);
            em.persist(transaction);
            status = em.contains(transaction);
            return status;
        } catch (Exception e) {
            System.out.println("Failed to insert p2p record: " + e.getMessage());
            return false;
        }
    }
}
