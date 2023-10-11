package com.kiryu1223.baseDao.ExpressionV2;

public class ValueExpression implements IExpression
{
    private final Object value;

    ValueExpression(Object value)
    {
        this.value = value;
    }

    public Object getValue()
    {
        return value;
    }
}
