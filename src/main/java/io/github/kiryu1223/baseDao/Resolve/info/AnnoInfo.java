package io.github.kiryu1223.baseDao.Resolve.info;

import io.github.kiryu1223.baseDao.ExpressionV2.IExpression;

public class AnnoInfo
{
    private final String AnnoName;
    private final Class<? extends IExpression> expressionType;

    public AnnoInfo(String annoName, Class<? extends IExpression> expressionType)
    {
        AnnoName = annoName;
        this.expressionType = expressionType;
    }

    public String getAnnoName()
    {
        return AnnoName;
    }

    public Class<? extends IExpression> getExpressionType()
    {
        return expressionType;
    }

    @Override
    public String toString()
    {
        return "AnnoInfo{" +
                "AnnoName='" + AnnoName + '\'' +
                ", expressionType=" + expressionType +
                '}';
    }
}
