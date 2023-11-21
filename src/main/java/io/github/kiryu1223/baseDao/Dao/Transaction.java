package io.github.kiryu1223.baseDao.Dao;

import io.github.kiryu1223.baseDao.Dao.Func.Func2;
import io.github.kiryu1223.baseDao.Dao.Statement.Statement;

import java.util.ArrayList;
import java.util.List;

public class Transaction
{
    private final List<Statement<?>> commands = new ArrayList<>();
    private TransactionType transactionType = null;

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
        for (Statement<?> command : commands)
        {
            entities.add(Resolve.cud(command));
        }
        return DBUtil.transactionCud(entities, transactionType.getTransactionIsolation());
    }

    public void debug(Func2<List<Entity>> func)
    {
        List<Entity> entities = new ArrayList<>(commands.size());
        for (Statement<?> command : commands)
        {
            entities.add(Resolve.cud(command));
        }
        func.invoke(entities);
    }
}
