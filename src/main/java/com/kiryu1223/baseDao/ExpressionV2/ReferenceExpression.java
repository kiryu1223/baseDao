package com.kiryu1223.baseDao.ExpressionV2;

public class ReferenceExpression implements IExpression
{
    private final Object reference;

    public ReferenceExpression(Object reference)
    {
        this.reference = reference;
    }

    public Object getReference()
    {
        return reference;
    }

    @Override
    public String toString()
    {
        return reference.toString();
    }
}
