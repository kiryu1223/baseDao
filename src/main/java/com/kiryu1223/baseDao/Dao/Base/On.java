package com.kiryu1223.baseDao.Dao.Base;

import com.kiryu1223.baseDao.ExpressionV2.IExpression;
import com.kiryu1223.baseDao.ExpressionV2.OperatorExpression;

public class On extends Base
{
    private final IExpression expression;

    public On(IExpression expression)
    {
        this.expression = expression;
    }

    public IExpression getExpression()
    {
        return expression;
    }
}
