package com.kiryu1223.baseDao.Dao.Base;

import com.kiryu1223.baseDao.ExpressionV2.OperatorExpression;

public class On extends Base
{
    private final OperatorExpression operatorExpression;

    public On(OperatorExpression operatorExpression)
    {
        this.operatorExpression = operatorExpression;
    }

    public OperatorExpression getOperatorExpression()
    {
        return operatorExpression;
    }
}
