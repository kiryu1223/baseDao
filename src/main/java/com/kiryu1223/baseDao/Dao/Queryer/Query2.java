package com.kiryu1223.baseDao.Dao.Queryer;

import com.kiryu1223.baseDao.Dao.Base.*;
import com.kiryu1223.baseDao.Dao.DBUtil;
import com.kiryu1223.baseDao.Dao.Func.Func00;
import com.kiryu1223.baseDao.Dao.Func.Func10;
import com.kiryu1223.baseDao.Dao.Func.Func2;
import com.kiryu1223.baseDao.Dao.JoinType;
import com.kiryu1223.baseDao.Dao.Statement.Statement2;
import com.kiryu1223.baseDao.ExpressionV2.DbRefExpression;
import com.kiryu1223.baseDao.ExpressionV2.NewExpression;
import com.kiryu1223.baseDao.ExpressionV2.OperatorExpression;
import com.kiryu1223.baseDao.Error.NoWayException;

import java.util.List;

public class Query2<T1, T2> extends Statement2<T1, T2>
{
    public Query2(DBUtil dbUtil, List<Base> bases, List<Class<?>> joins, Class<T1> c1, Class<T2> c2)
    {
        super(dbUtil, bases, joins, c1, c2);
    }

    public Query2<T1, T2> on(Func10<T1, T2> func)
    {
        throw new NoWayException();
    }

    public Query2<T1, T2> where(Func10<T1, T2> func)
    {
        throw new NoWayException();
    }

    public Query2<T1, T2> on(OperatorExpression operatorExpression)
    {
        bases.add(new On(operatorExpression));
        return this;
    }

    public <R> QueryResult<R> select(Func00<T1, T2,R> func)
    {
        throw new NoWayException();
    }

    public <R> QueryResult<R> selectDistinct(Func00<T1, T2,R> func)
    {
        throw new NoWayException();
    }

    public Query2<T1, T2> where(OperatorExpression operatorExpression)
    {
        bases.add(new Where(operatorExpression));
        return this;
    }

    public <R> QueryResult<R> select(NewExpression<R> newExpression)
    {
        return new QueryResult<R>(dbUtil, false, bases, getQueryClasses(), joins, newExpression);
    }

    public <R> QueryResult<R> selectDistinct(NewExpression<R> newExpression)
    {
        return new QueryResult<R>(dbUtil, true, bases, getQueryClasses(), joins, newExpression);
    }

    public <R> Query2<T1, T2> orderBy(Func00<T1, T2, R> func)
    {
        throw new NoWayException();
    }

    public Query2<T1, T2> orderBy(DbRefExpression dbRefExpression)
    {
        bases.add(new OrderBy(dbRefExpression));
        return this;
    }

    public Query2<T1, T2> limit(int offset, int rows)
    {
        bases.add(new Limit(offset, rows));
        return this;
    }

    public Query2<T1, T2> limit(int rows)
    {
        bases.add(new Limit(rows));
        return this;
    }

    public Query2<T1, T2> If(boolean flag, Func2<Query2<T1, T2>> If)
    {
        if (flag) If.invoke(this);
        return this;
    }

    public Query2<T1, T2> IfElse(boolean flag, Func2<Query2<T1, T2>> If, Func2<Query2<T1, T2>> Else)
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

    public <T3> Query3<T1, T2, T3> innerJoin(Class<T3> c)
    {
        bases.add(new Join(c, JoinType.Inner));
        joins.add(c);
        return new Query3<>(dbUtil, bases, joins, c1, c2, c);
    }

    public <T3> Query3<T1, T2, T3> leftJoin(Class<T3> c)
    {
        bases.add(new Join(c, JoinType.Left));
        joins.add(c);
        return new Query3<>(dbUtil, bases, joins, c1, c2, c);
    }

    public <T3> Query3<T1, T2, T3> rightJoin(Class<T3> c)
    {
        bases.add(new Join(c, JoinType.Right));
        joins.add(c);
        return new Query3<>(dbUtil, bases, joins, c1, c2, c);
    }

    public <T3> Query3<T1, T2, T3> fullJoin(Class<T3> c)
    {
        bases.add(new Join(c, JoinType.Full));
        joins.add(c);
        return new Query3<>(dbUtil, bases, joins, c1, c2, c);
    }
}
