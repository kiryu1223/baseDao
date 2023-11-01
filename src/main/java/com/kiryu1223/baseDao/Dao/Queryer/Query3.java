package com.kiryu1223.baseDao.Dao.Queryer;

import com.kiryu1223.baseDao.Dao.Base.*;
import com.kiryu1223.baseDao.Dao.DBUtil;
import com.kiryu1223.baseDao.Dao.Func.Func000;
import com.kiryu1223.baseDao.Dao.Func.Func100;
import com.kiryu1223.baseDao.Dao.Func.Func2;
import com.kiryu1223.baseDao.Dao.Statement.Statement3;
import com.kiryu1223.baseDao.ExpressionV2.IExpression;
import com.kiryu1223.baseDao.ExpressionV2.NewExpression;
import com.kiryu1223.baseDao.ExpressionV2.OperatorExpression;
import com.kiryu1223.baseDao.Error.NoWayException;
import com.kiryu1223.baseDao.Dao.JoinType;

import java.util.List;

public class Query3<T1, T2, T3> extends Statement3<T1, T2, T3>
{
    public Query3(DBUtil dbUtil, List<Base> bases, List<Class<?>> joins, Class<T1> c1, Class<T2> c2, Class<T3> c3)
    {
        super(dbUtil, bases, joins, c1, c2, c3);
    }

    public Query3<T1, T2, T3> on(Func100<T1, T2, T3> func)
    {
        throw new NoWayException();
    }

    public Query3<T1, T2, T3> where(Func100<T1, T2, T3> func)
    {
        throw new NoWayException();
    }

    public Query3<T1, T2, T3> on(OperatorExpression operatorExpression)
    {
        bases.add(new On(operatorExpression));
        return this;
    }

    public <R> QueryResult<R> select(Func000<T1, T2, T3,R> func)
    {
        throw new NoWayException();
    }

    public <R> QueryResult<R> selectDistinct(Func000<T1, T2, T3,R> func)
    {
        throw new NoWayException();
    }

    public Query3<T1, T2, T3> where(OperatorExpression operatorExpression)
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
        return new QueryResult<R>(dbUtil,bases, getQueryClasses(), getQueryTargets(), joins, newExpression);
    }

    public <R> Query3<T1, T2, T3> orderBy(Func000<T1, T2, T3, R> func)
    {
        throw new NoWayException();
    }
    public <R> Query3<T1, T2, T3> descOrderBy(Func000<T1, T2, T3, R> func)
    {
        throw new NoWayException();
    }

    public Query3<T1, T2, T3> orderBy(IExpression expression)
    {
        bases.add(new OrderBy(expression, false));
        return this;
    }
    public Query3<T1, T2, T3> descOrderBy(IExpression expression)
    {
        bases.add(new OrderBy(expression, true));
        return this;
    }

    public Query3<T1, T2, T3> take(int count)
    {
        bases.add(new Take(count));
        return this;
    }

    public Query3<T1, T2, T3> skip(int count)
    {
        bases.add(new Skip(count));
        return this;
    }

    public Query3<T1, T2, T3> If(boolean flag, Func2<Query3<T1, T2, T3>> If)
    {
        if (flag) If.invoke(this);
        return this;
    }

    public Query3<T1, T2, T3> IfElse(boolean flag, Func2<Query3<T1, T2, T3>> If, Func2<Query3<T1, T2, T3>> Else)
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

    public <T4> Query4<T1, T2, T3, T4> innerJoin(Class<T4> c)
    {
        bases.add(new Join(c, JoinType.Inner));
        joins.add(c);
        return new Query4<>(dbUtil, bases, joins, c1, c2, c3, c);
    }

    public <T4> Query4<T1, T2, T3, T4> leftJoin(Class<T4> c)
    {
        bases.add(new Join(c, JoinType.Left));
        joins.add(c);
        return new Query4<>(dbUtil, bases, joins, c1, c2, c3, c);
    }

    public <T4> Query4<T1, T2, T3, T4> rightJoin(Class<T4> c)
    {
        bases.add(new Join(c, JoinType.Right));
        joins.add(c);
        return new Query4<>(dbUtil, bases, joins, c1, c2, c3, c);
    }

    public <T4> Query4<T1, T2, T3, T4> fullJoin(Class<T4> c)
    {
        bases.add(new Join(c, JoinType.Full));
        joins.add(c);
        return new Query4<>(dbUtil, bases, joins, c1, c2, c3, c);
    }
}
