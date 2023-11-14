package com.kiryu1223.baseDao.ExpressionV2;

public class ValueExpression<T> implements IExpression
{
    private final T value;

    ValueExpression(T value)
    {
        this.value = value;
    }

    public T getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return value.toString();
    }
}
