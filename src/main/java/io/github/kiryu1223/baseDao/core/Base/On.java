package io.github.kiryu1223.baseDao.core.Base;

import io.github.kiryu1223.expressionTree.expressions.LambdaExpression;

public class On extends Base
{
    private final LambdaExpression lambdaExpression;

    public On(LambdaExpression lambdaExpression)
    {
        this.lambdaExpression = lambdaExpression;
    }

    public LambdaExpression getLambdaExpression()
    {
        return lambdaExpression;
    }
}
