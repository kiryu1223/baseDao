package io.github.kiryu1223.baseDao.ExpressionV2;

public class AssignExpression implements IExpression
{
    private final IExpression left;
    private final IExpression right;

    AssignExpression(IExpression left, IExpression right)
    {
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
