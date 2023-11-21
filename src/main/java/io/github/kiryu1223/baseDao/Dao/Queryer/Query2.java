package io.github.kiryu1223.baseDao.Dao.Queryer;

import io.github.kiryu1223.baseDao.Dao.Base.*;
import io.github.kiryu1223.baseDao.Dao.ExpressionFunc;
import io.github.kiryu1223.baseDao.Dao.Statement.Statement2;
import io.github.kiryu1223.baseDao.Resolve.Expression;
import io.github.kiryu1223.baseDao.Dao.Func.Func00;
import io.github.kiryu1223.baseDao.Dao.Func.Func10;
import io.github.kiryu1223.baseDao.Dao.JoinType;
import io.github.kiryu1223.baseDao.ExpressionV2.NewExpression;
import io.github.kiryu1223.baseDao.Error.NoWayException;

import java.util.List;

public class Query2<T1, T2> extends Statement2<T1, T2>
{
    public Query2(List<Base> bases, List<Class<?>> joins, Class<T1> c1, Class<T2> c2)
    {
        super(bases, joins, c1, c2);
    }

    public Query2<T1, T2> on(@Expression Func10<T1, T2> func)
    {
        throw new NoWayException();
    }

    public Query2<T1, T2> where(@Expression Func10<T1, T2> func)
    {
        throw new NoWayException();
    }

    public <R> Query2<T1, T2> orderBy(@Expression Func00<T1, T2, R> func)
    {
        throw new NoWayException();
    }

    public <R> Query2<T1, T2> descOrderBy(@Expression Func00<T1, T2, R> func)
    {
        throw new NoWayException();
    }

    public <R> QueryResult<R> select(@Expression(NewExpression.class) Func00<T1, T2, R> func)
    {
        throw new NoWayException();
    }

    public Query2<T1, T2> on(ExpressionFunc.E2<Void, T1, T2> e2)
    {
        bases.add(new On(e2.invoke(null, t1, t2)));
        return this;
    }

    public Query2<T1, T2> where(ExpressionFunc.E2<Void, T1, T2> e2)
    {
        bases.add(new Where(e2.invoke(null, t1, t2)));
        return this;
    }

    public Query2<T1, T2> orderBy(ExpressionFunc.E2<Void, T1, T2> e2)
    {
        bases.add(new OrderBy(e2.invoke(null, t1, t2), false));
        return this;
    }

    public Query2<T1, T2> descOrderBy(ExpressionFunc.E2<Void, T1, T2> e2)
    {
        bases.add(new OrderBy(e2.invoke(null, t1, t2), true));
        return this;
    }

    public <R> QueryResult<R> select(ExpressionFunc.NR2<Void, T1, T2, R> r)
    {
        return new QueryResult<R>(bases, getQueryClasses(), getQueryTargets(), joins, r.invoke(null, t1, t2));
    }

    public Query2<T1, T2> take(int count)
    {
        bases.add(new Take(count));
        return this;
    }

    public Query2<T1, T2> skip(int count)
    {
        bases.add(new Skip(count));
        return this;
    }

    public <T3> Query3<T1, T2, T3> innerJoin(Class<T3> c)
    {
        bases.add(new Join(c, JoinType.Inner));
        joins.add(c);
        return new Query3<>(bases, joins, c1, c2, c);
    }

    public <T3> Query3<T1, T2, T3> leftJoin(Class<T3> c)
    {
        bases.add(new Join(c, JoinType.Left));
        joins.add(c);
        return new Query3<>(bases, joins, c1, c2, c);
    }

    public <T3> Query3<T1, T2, T3> rightJoin(Class<T3> c)
    {
        bases.add(new Join(c, JoinType.Right));
        joins.add(c);
        return new Query3<>(bases, joins, c1, c2, c);
    }

    public <T3> Query3<T1, T2, T3> fullJoin(Class<T3> c)
    {
        bases.add(new Join(c, JoinType.Full));
        joins.add(c);
        return new Query3<>(bases, joins, c1, c2, c);
    }
}
