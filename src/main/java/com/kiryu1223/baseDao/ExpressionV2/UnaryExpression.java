package com.kiryu1223.baseDao.ExpressionV2;

public class UnaryExpression extends OperatorExpression
{
    private final IExpression expression;

    UnaryExpression(IExpression expression, Operator operator)
    {
        super(operator);
        this.expression = expression;
    }

    public IExpression getExpression()
    {
        return expression;
    }

}
