package com.kiryu1223.baseDao.ExpressionV2;

public class BinaryExpression extends OperatorExpression
{
    private final IExpression left;
    private final IExpression right;
    BinaryExpression(IExpression left, IExpression right, Operator operator)
    {
        super(operator);
        this.left = left;
        this.right = right;
    }

    public IExpression getLeft()
    {
        return left;
    }

    public IExpression getRight()
    {
        return right;
    }
}
