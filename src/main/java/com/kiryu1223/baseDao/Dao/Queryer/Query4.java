package com.kiryu1223.baseDao.Dao.Queryer;


import com.kiryu1223.baseDao.Dao.Base.*;
import com.kiryu1223.baseDao.Dao.DBUtil;
import com.kiryu1223.baseDao.Dao.Func.Func0000;
import com.kiryu1223.baseDao.Dao.Func.Func1000;
import com.kiryu1223.baseDao.Dao.Func.Func2;
import com.kiryu1223.baseDao.Dao.Statement.Statement4;
import com.kiryu1223.baseDao.ExpressionV2.IExpression;
import com.kiryu1223.baseDao.ExpressionV2.NewExpression;
import com.kiryu1223.baseDao.ExpressionV2.OperatorExpression;
import com.kiryu1223.baseDao.Error.NoWayException;


import java.util.List;

public class Query4<T1, T2, T3, T4> extends Statement4<T1, T2, T3, T4>
{
    public Query4(DBUtil dbUtil, List<Base> bases, List<Class<?>> joins, Class<T1> c1, Class<T2> c2, Class<T3> c3, Class<T4> c4)
    {
        super(dbUtil, bases, joins, c1, c2, c3, c4);
    }

    public Query4<T1, T2, T3, T4> on(Func1000<T1, T2, T3, T4> func)
    {
        throw new NoWayException();
    }

    public Query4<T1, T2, T3, T4> where(Func1000<T1, T2, T3, T4> func)
    {
        throw new NoWayException();
    }

    public Query4<T1, T2, T3, T4> on(OperatorExpression operatorExpression)
    {
        bases.add(new On(operatorExpression));
        return this;
    }

    public <R> QueryResult<R> select(Func0000<T1, T2, T3, T4, R> func)
    {
        throw new NoWayException();
    }

    public <R> QueryResult<R> selectDistinct(Func0000<T1, T2, T3, T4, R> func)
    {
        throw new NoWayException();
    }

    public Query4<T1, T2, T3, T4> where(OperatorExpression operatorExpression)
    {
        bases.add(new Where(operatorExpression));
        return this;
    }

    public <R> QueryResult<R> select(NewExpression<R> newExpression)
    {
        return new QueryResult<R>(dbUtil, bases, getQueryClasses(), getQueryTargets(), joins, newExpression);
    }

    public <R> QueryResult<R> selectDistinct(NewExpression<R> newExpression)
    {
        return new QueryResult<R>(dbUtil, bases, getQueryClasses(), getQueryTargets(), joins, newExpression);
    }

    public <R> Query4<T1, T2, T3, T4> orderBy(Func0000<T1, T2, T3, T4, R> func)
    {
        throw new NoWayException();
    }

    public <R> Query4<T1, T2, T3, T4> descOrderBy(Func0000<T1, T2, T3, T4, R> func)
    {
        throw new NoWayException();
    }

    public Query4<T1, T2, T3, T4> orderBy(IExpression expression)
    {
        bases.add(new OrderBy(expression, false));
        return this;
    }

    public Query4<T1, T2, T3, T4> descOrderBy(IExpression expression)
    {
        bases.add(new OrderBy(expression, true));
        return this;
    }

    public Query4<T1, T2, T3, T4> take(int count)
    {
        bases.add(new Take(count));
        return this;
    }

    public Query4<T1, T2, T3, T4> skip(int count)
    {
        bases.add(new Skip(count));
        return this;
    }

    public Query4<T1, T2, T3, T4> If(boolean flag, Func2<Query4<T1, T2, T3, T4>> If)
    {
        if (flag) If.invoke(this);
        return this;
    }

    public Query4<T1, T2, T3, T4> IfElse(boolean flag, Func2<Query4<T1, T2, T3, T4>> If, Func2<Query4<T1, T2, T3, T4>> Else)
    {
        if (flag)
        {
            If.invoke(this);
        }
        else
        {
            Else.invoke(this);
        }
        return this;
    }
}
