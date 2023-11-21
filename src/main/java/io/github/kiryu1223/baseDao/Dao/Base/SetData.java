package io.github.kiryu1223.baseDao.Dao.Base;

import io.github.kiryu1223.baseDao.ExpressionV2.IExpression;

public class SetData extends Base
{
    private final IExpression expression;

    public SetData(IExpression expression)
    {
        this.expression = expression;
    }

    public IExpression getExpression()
    {
        return expression;
    }

    @Override
    public String toString()
    {
        return expression.toString();
    }
}
