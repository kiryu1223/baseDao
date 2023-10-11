package com.kiryu1223.baseDao.ExpressionV2;

import java.util.Arrays;
import java.util.stream.Collectors;

public interface IExpression
{
    public static BinaryExpression binary(IExpression left, IExpression right, Operator operator)
    {
        return new BinaryExpression(left, right, operator);
    }
    public static ValueExpression value(Object value)
    {
        return new ValueExpression(value);
    }
    public static DbRefExpression dbRef(int index,String refName)
    {
        return new DbRefExpression(index, refName);
    }
    public static UnaryExpression unary(IExpression expression, Operator operator)
    {
        return new UnaryExpression(expression, operator);
    }
    public static <T> NewExpression<T> New(Class<T> target,IExpression...expressions)
    {
        return new NewExpression<T>(target, Arrays.stream(expressions).collect(Collectors.toList()));
    }
    public static MappingExpression mapping(String source,IExpression value)
    {
        return new MappingExpression(source,value);
    }
    public static DbFuncExpression dbFunc(DbFuncType funcType,IExpression expression)
    {
        return new DbFuncExpression(funcType,expression);
    }
    enum Type
    {
        Binary,
        Value,
        DbRef,
        Unary,
        New,
        Mapping,
        DbFunc,
    }
}
