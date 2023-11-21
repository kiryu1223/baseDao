package io.github.kiryu1223.baseDao.Dao.Base;

import io.github.kiryu1223.baseDao.ExpressionV2.IExpression;

public class OrderBy extends Base
{
    private final IExpression expression;
    private final boolean isDesc;

    public OrderBy(IExpression expression, boolean isDesc)
    {
        this.expression = expression;
        this.isDesc = isDesc;
    }

    public IExpression getExpression()
    {
        return expression;
    }

    public boolean isDesc()
    {
        return isDesc;
    }
}
