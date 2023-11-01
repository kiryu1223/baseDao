package com.kiryu1223.baseDao.Dao;

import com.kiryu1223.baseDao.Dao.Func.Func0;
import com.kiryu1223.baseDao.Dao.Func.Func1;
import com.kiryu1223.baseDao.Dao.Func.Func2;
import com.kiryu1223.baseDao.Dao.Statement.Statement;

import java.util.ArrayList;
import java.util.List;

public class Transaction
{
    private final List<Statement<?>> commands = new ArrayList<>();
    private final DBUtil dbUtil;
    private TransactionType transactionType = null;

    public Transaction(DBUtil dbUtil)
    {
        this.dbUtil = dbUtil;
    }

    public void push(Statement<?> statement)
    {
        commands.add(statement);
    }

    public List<Statement<?>> getCommands()
    {
        return commands;
    }

    public Transaction setTransactionIsolation(TransactionType transactionType)
    {
        this.transactionType = transactionType;
        return this;
    }

    public boolean commit()
    {
        if (commands.isEmpty()) return false;
        List<Entity> entities = new ArrayList<>(commands.size());
        for (var command : commands)
        {
            entities.add(Resolve.cud(command));
        }
        return dbUtil.transactionCud(entities, transactionType.getTransactionIsolation());
    }

    public void debug(Func2<List<Entity>> func)
    {
        List<Entity> entities = new ArrayList<>(commands.size());
        for (var command : commands)
        {
            entities.add(Resolve.cud(command));
        }
        func.invoke(entities);
    }
}
