package com.kiryu1223.baseDao.ExpressionV2;

import java.util.List;

public class MappingsExpression implements IExpression
{
    private final List<MappingExpression> expressions;

    public MappingsExpression(List<MappingExpression> expressions)
    {
        this.expressions = expressions;
    }

    public List<MappingExpression> getExpressions()
    {
        return expressions;
    }
}
