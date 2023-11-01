package com.kiryu1223.baseDao.Dao.Base;

import com.kiryu1223.baseDao.ExpressionV2.IExpression;
import com.kiryu1223.baseDao.ExpressionV2.OperatorExpression;

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
