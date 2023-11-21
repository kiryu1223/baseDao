package io.github.kiryu1223.baseDao.Dao.Queryer;

import io.github.kiryu1223.baseDao.Dao.Base.*;
import io.github.kiryu1223.baseDao.Dao.ExpressionFunc;
import io.github.kiryu1223.baseDao.Dao.Statement.Statement3;
import io.github.kiryu1223.baseDao.Resolve.Expression;
import io.github.kiryu1223.baseDao.Dao.Func.Func000;
import io.github.kiryu1223.baseDao.Dao.Func.Func100;
import io.github.kiryu1223.baseDao.ExpressionV2.NewExpression;
import io.github.kiryu1223.baseDao.Error.NoWayException;
import io.github.kiryu1223.baseDao.Dao.JoinType;

import java.util.List;

public class Query3<T1, T2, T3> extends Statement3<T1, T2, T3>
{
    public Query3(List<Base> bases, List<Class<?>> joins, Class<T1> c1, Class<T2> c2, Class<T3> c3)
    {
        super(bases, joins, c1, c2, c3);
    }

    public Query3<T1, T2, T3> on(@Expression Func100<T1, T2, T3> func)
    {
        throw new NoWayException();
    }

    public Query3<T1, T2, T3> where(@Expression Func100<T1, T2, T3> func)
    {
        throw new NoWayException();
    }

    public <R> Query3<T1, T2, T3> orderBy(@Expression Func000<T1, T2, T3, R> func)
    {
        throw new NoWayException();
    }

    public <R> Query3<T1, T2, T3> descOrderBy(@Expression Func000<T1, T2, T3, R> func)
    {
        throw new NoWayException();
    }

    public <R> QueryResult<R> select(@Expression(NewExpression.class) Func000<T1, T2, T3, R> func)
    {
        throw new NoWayException();
    }

    public Query3<T1, T2, T3> on(ExpressionFunc.E3<Void, T1, T2, T3> e3)
    {
        bases.add(new On(e3.invoke(null, t1, t2, t3)));
        return this;
    }


    public Query3<T1, T2, T3> where(ExpressionFunc.E3<Void, T1, T2, T3> e3)
    {
        bases.add(new Where(e3.invoke(null, t1, t2, t3)));
        return this;
    }

    public <R> QueryResult<R> select(ExpressionFunc.NR3<Void, T1, T2,T3, R> r)
    {
        return new QueryResult<R>(bases, getQueryClasses(), getQueryTargets(), joins, r.invoke(null,t1,t2,t3));
    }

    public Query3<T1, T2, T3> orderBy(ExpressionFunc.E3<Void, T1, T2, T3> e3)
    {
        bases.add(new OrderBy(e3.invoke(null, t1, t2, t3), false));
        return this;
    }

    public Query3<T1, T2, T3> descOrderBy(ExpressionFunc.E3<Void, T1, T2, T3> e3)
    {
        bases.add(new OrderBy(e3.invoke(null, t1, t2, t3), true));
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

    public <T4> Query4<T1, T2, T3, T4> innerJoin(Class<T4> c)
    {
        bases.add(new Join(c, JoinType.Inner));
        joins.add(c);
        return new Query4<>(bases, joins, c1, c2, c3, c);
    }

    public <T4> Query4<T1, T2, T3, T4> leftJoin(Class<T4> c)
    {
        bases.add(new Join(c, JoinType.Left));
        joins.add(c);
        return new Query4<>(bases, joins, c1, c2, c3, c);
    }

    public <T4> Query4<T1, T2, T3, T4> rightJoin(Class<T4> c)
    {
        bases.add(new Join(c, JoinType.Right));
        joins.add(c);
        return new Query4<>(bases, joins, c1, c2, c3, c);
    }

    public <T4> Query4<T1, T2, T3, T4> fullJoin(Class<T4> c)
    {
        bases.add(new Join(c, JoinType.Full));
        joins.add(c);
        return new Query4<>(bases, joins, c1, c2, c3, c);
    }
}
