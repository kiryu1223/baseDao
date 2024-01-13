package io.github.kiryu1223.baseDao.core.Base;

import io.github.kiryu1223.expressionTree.expressions.LambdaExpression;

public class Where extends Base
{
    private final LambdaExpression lambdaExpression;

    public Where(LambdaExpression lambdaExpression)
    {
        this.lambdaExpression = lambdaExpression;
    }

    public LambdaExpression getLambdaExpression()
    {
        return lambdaExpression;
    }
}
