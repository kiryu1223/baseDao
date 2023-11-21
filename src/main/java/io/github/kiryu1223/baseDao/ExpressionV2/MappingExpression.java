package io.github.kiryu1223.baseDao.ExpressionV2;


public class MappingExpression implements IExpression
{
    private final String source;
    private final IExpression value;

    MappingExpression(String source, IExpression value)
    {
        this.source = source;
        this.value = value;
    }

    public String getSource()
    {
        return source;
    }

    public IExpression getValue()
    {
        return value;
    }
}
