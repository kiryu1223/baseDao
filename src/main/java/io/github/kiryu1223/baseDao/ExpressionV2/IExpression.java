package io.github.kiryu1223.baseDao.ExpressionV2;

import com.sun.org.apache.bcel.internal.generic.NEW;

import java.util.ArrayList;
import java.util.Arrays;

public interface IExpression
{
    public static BinaryExpression binary(IExpression left, IExpression right, Operator operator)
    {
        return new BinaryExpression(left, right, operator);
    }

    public static <T> ValueExpression<T> value(T value)
    {
        return new ValueExpression<T>(value);
    }

    public static UnaryExpression unary(IExpression expression, Operator operator)
    {
        return new UnaryExpression(expression, operator);
    }

    public static <T> NewExpression<T> New(Class<T> target, IExpression... expressions)
    {
        return new NewExpression<T>(target, Arrays.asList(expressions));
    }

    public static <T> NewExpression<T> New(T t, IExpression... expressions)
    {
        return new NewExpression<T>((Class<T>) t.getClass(), Arrays.asList(expressions));
    }

    public static MappingExpression mapping(String source, IExpression value)
    {
        return new MappingExpression(source, value);
    }

    public static MappingsExpression mappings(MappingExpression... mappingExpressions)
    {
        return new MappingsExpression(Arrays.asList(mappingExpressions));
    }

    public static DbFuncExpression dbFunc(DbFuncType funcType, IExpression expression)
    {
        return new DbFuncExpression(funcType, expression);
    }

    public static ParensExpression parens(IExpression expression)
    {
        return new ParensExpression(expression);
    }

    public static FieldSelectExpression fieldSelect(IExpression selector, String selected)
    {
        return new FieldSelectExpression(selector, selected);
    }

    public static MethodCallExpression methodCall(IExpression selector, String selected, IExpression... params)
    {
        return new MethodCallExpression(selector, selected, new ArrayList<>(Arrays.asList(params)));
    }

    public static ReferenceExpression reference(Object t)
    {
        return new ReferenceExpression(t);
    }

    public static BlockExpression block(IExpression... expression)
    {
        return new BlockExpression(Arrays.asList(expression));
    }

    public static AssignExpression assign(IExpression left, IExpression right)
    {
        return new AssignExpression(left, right);
    }

    enum Type
    {
        Binary,
        Value,
        Unary,
        New,
        Mapping,
        Mappings,
        DbFunc,
        Parens,
        FieldSelect,
        MethodCall,
        Reference,
        Block,
        Assign,
    }
}