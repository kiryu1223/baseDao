package io.github.kiryu1223.baseDao.Dao.Base;


import io.github.kiryu1223.expressionTree.expressionV2.IExpression;

public class Where extends Base
{
    private final IExpression expression;

    public Where(IExpression expression)
    {
        this.expression = expression;
    }

    public IExpression getExpression()
    {
        return expression;
    }
}
