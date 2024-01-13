package io.github.kiryu1223.baseDao.core.Base;

import io.github.kiryu1223.expressionTree.expressions.LambdaExpression;

public class SetData extends Base
{
    private final LambdaExpression lambdaExpression;

    public SetData(LambdaExpression lambdaExpression)
    {
        this.lambdaExpression = lambdaExpression;
    }

    public LambdaExpression getLambdaExpression()
    {
        return lambdaExpression;
    }
}
