package com.kiryu1223.baseDao.ExpressionV2;

public class ParensExpression implements IExpression
{
    private final IExpression expression;

    ParensExpression(IExpression expression)
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
