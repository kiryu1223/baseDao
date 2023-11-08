package com.kiryu1223.baseDao.Resolve;

import com.kiryu1223.baseDao.ExpressionV2.IExpression;

public class Info
{
    private final Class<? extends IExpression> type;
    private final int count;

    public Info(Class<? extends IExpression> type, int count)
    {
        this.type = type;
        this.count = count;
    }

    public Class<? extends IExpression> getType()
    {
        return type;
    }

    public int getCount()
    {
        return count;
    }

    @Override
    public String toString()
    {
        return "Info{" +
                "type=" + type +
                ", count=" + count +
                '}';
    }
}
