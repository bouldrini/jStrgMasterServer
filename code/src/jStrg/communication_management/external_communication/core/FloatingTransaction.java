package jStrg.communication_management.external_communication.core;


import java.util.UUID;

public class FloatingTransaction {

    public int m_id;
    public String m_transaction_id;
    public FloatingTransaction(int _id) {
        m_transaction_id = FloatingTransaction.get_new_unique_id();
    }

    static String get_new_unique_id() {
        return UUID.randomUUID().toString();
    }
}
