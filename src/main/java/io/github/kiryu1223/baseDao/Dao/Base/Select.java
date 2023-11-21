package io.github.kiryu1223.baseDao.Dao.Base;


import io.github.kiryu1223.baseDao.ExpressionV2.NewExpression;

public class Select<R> extends Base
{
    private final NewExpression<R> newExpression;

    public Select(NewExpression<R> newExpression)
    {
        this.newExpression = newExpression;
    }

    public NewExpression<R> getNewExpression()
    {
        return newExpression;
    }
}
