package io.github.kiryu1223.baseDao.ExpressionV2;

import java.util.ArrayList;
import java.util.List;

public class BlockExpression implements IExpression
{
    private final List<IExpression> expressions;

    BlockExpression(List<IExpression> expressions)
    {
        this.expressions = expressions;
    }

    public List<IExpression> getExpressions()
    {
        return expressions;
    }

    @Override
    public String toString()
    {
        StringBuilder sb=new StringBuilder();
        for (IExpression expression : expressions)
        {
            sb.append(expression.toString()).append("\n");
        }
        return sb.toString();
    }
}
