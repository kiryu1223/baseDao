package com.kiryu1223.baseDao.Dao;

import java.sql.Connection;

public enum TransactionType
{
    None(Connection.TRANSACTION_NONE),
    ReadUncommitted(Connection.TRANSACTION_READ_UNCOMMITTED),
    ReadCommitted(Connection.TRANSACTION_READ_COMMITTED),
    RepeatableRead(Connection.TRANSACTION_REPEATABLE_READ),
    Serializable(Connection.TRANSACTION_SERIALIZABLE),
    ;
    private final int transactionIsolation;
    TransactionType(int transactionIsolation)
    {
        this.transactionIsolation=transactionIsolation;
    }

    public int getTransactionIsolation()
    {
        return transactionIsolation;
    }
}
