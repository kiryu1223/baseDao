package io.github.kiryu1223.baseDao.core.Base;

import io.github.kiryu1223.expressionTree.expressions.LambdaExpression;

public class OrderBy extends Base
{
    private final LambdaExpression lambdaExpression;
    private final boolean isDesc;

    public OrderBy(LambdaExpression lambdaExpression, boolean isDesc)
    {
        this.lambdaExpression = lambdaExpression;
        this.isDesc = isDesc;
    }

    public LambdaExpression getLambdaExpression()
    {
        return lambdaExpression;
    }

    public boolean isDesc()
    {
        return isDesc;
    }
}
