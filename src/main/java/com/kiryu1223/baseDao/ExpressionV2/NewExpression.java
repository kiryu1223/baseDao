package com.kiryu1223.baseDao.ExpressionV2;

import java.util.List;

public class NewExpression<T> implements IExpression
{
    private final Class<T> target;

    private final List<IExpression> expressions;

    NewExpression(Class<T> target, List<IExpression> expressions)
    {
        this.target = target;
        this.expressions = expressions;
    }

    public Class<T> getTarget()
    {
        return target;
    }

    public List<IExpression> getExpressions()
    {
        return expressions;
    }
}
