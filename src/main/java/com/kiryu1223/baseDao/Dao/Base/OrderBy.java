package com.kiryu1223.baseDao.Dao.Base;

import com.kiryu1223.baseDao.ExpressionV2.DbRefExpression;

public class OrderBy extends Base
{
    private final DbRefExpression dbRefExpression;

    public OrderBy(DbRefExpression dbRefExpression)
    {
        this.dbRefExpression = dbRefExpression;
    }

    public DbRefExpression getDbRefExpression()
    {
        return dbRefExpression;
    }
}
