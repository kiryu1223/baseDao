package io.github.kiryu1223.baseDao.Dao.Base;

import io.github.kiryu1223.baseDao.ExpressionV2.IExpression;

public class On extends Base
{
    private final IExpression expression;

    public On(IExpression expression)
    {
        this.expression = expression;
    }

    public IExpression getExpression()
    {
        return expression;
    }
}
