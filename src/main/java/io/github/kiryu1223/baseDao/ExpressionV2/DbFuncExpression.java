package io.github.kiryu1223.baseDao.ExpressionV2;

public class DbFuncExpression implements IExpression
{
    private final DbFuncType dbFuncType;
    private final IExpression expression;

    DbFuncExpression(DbFuncType dbFuncType, IExpression expression)
    {
        this.dbFuncType = dbFuncType;
        this.expression = expression;
    }

    public IExpression getExpression()
    {
        return expression;
    }

    public DbFuncType getDbFuncType()
    {
        return dbFuncType;
    }
}
