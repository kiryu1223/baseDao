package com.kiryu1223.baseDao.Dao.Base;

import com.kiryu1223.baseDao.ExpressionV2.DbRefExpression;

public class OrderBy extends Base
{
    private final DbRefExpression dbRefExpression;
    private final boolean isDesc;

    public OrderBy(DbRefExpression dbRefExpression, boolean isDesc)
    {
        this.dbRefExpression = dbRefExpression;
        this.isDesc = isDesc;
    }

    public DbRefExpression getDbRefExpression()
    {
        return dbRefExpression;
    }

    public boolean isDesc()
    {
        return isDesc;
    }
}
