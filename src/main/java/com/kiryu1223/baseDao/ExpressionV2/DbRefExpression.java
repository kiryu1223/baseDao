package com.kiryu1223.baseDao.ExpressionV2;

public class DbRefExpression implements IExpression
{
    private final int index;
    private final String ref;

    DbRefExpression(int index, String ref)
    {
        this.index = index;
        this.ref = ref;
    }

    public String getRef()
    {
        return ref;
    }

    public int getIndex()
    {
        return index;
    }
}
