package io.github.kiryu1223.baseDao.Dao.Queryer;

import io.github.kiryu1223.baseDao.Dao.Base.*;
import io.github.kiryu1223.baseDao.Dao.Statement.Statement3;
import io.github.kiryu1223.baseDao.Error.NoWayException;
import io.github.kiryu1223.baseDao.Dao.JoinType;
import io.github.kiryu1223.expressionTree.Expression;
import io.github.kiryu1223.expressionTree.FunctionalInterface.ExpressionTree;
import io.github.kiryu1223.expressionTree.FunctionalInterface.IReturnBoolean;
import io.github.kiryu1223.expressionTree.FunctionalInterface.IReturnGeneric;
import io.github.kiryu1223.expressionTree.expressionV2.NewExpression;

import java.util.List;

public class Query3<T1, T2, T3> extends Statement3<T1, T2, T3>
{
    public Query3(List<Base> bases, List<Class<?>> joins, Class<T1> c1, Class<T2> c2, Class<T3> c3)
    {
        super(bases, joins, c1, c2, c3);
    }

    public Query3<T1, T2, T3> on(@Expression IReturnBoolean.B3<T1, T2, T3> func)
    {
        throw new NoWayException();
    }

    public Query3<T1, T2, T3> where(@Expression IReturnBoolean.B3<T1, T2, T3> func)
    {
        throw new NoWayException();
    }

    public <R> Query3<T1, T2, T3> orderBy(@Expression IReturnGeneric.G3<T1, T2, T3, R> func)
    {
        throw new NoWayException();
    }

    public <R> Query3<T1, T2, T3> descOrderBy(@Expression IReturnGeneric.G3<T1, T2, T3, R> func)
    {
        throw new NoWayException();
    }

    public <R> QueryResult<R> select(@Expression(NewExpression.class) IReturnGeneric.G3<T1, T2, T3, R> func)
    {
        throw new NoWayException();
    }

    public Query3<T1, T2, T3> on(ExpressionTree.E3<Void, T1, T2, T3> e3)
    {
        bases.add(new On(e3.invoke(null, t1, t2, t3)));
        return this;
    }


    public Query3<T1, T2, T3> where(ExpressionTree.E3<Void, T1, T2, T3> e3)
    {
        bases.add(new Where(e3.invoke(null, t1, t2, t3)));
        return this;
    }

    public <R> QueryResult<R> select(ExpressionTree.NR3<Void, T1, T2,T3, R> r)
    {
        return new QueryResult<R>(bases, getQueryClasses(), getQueryTargets(), joins, r.invoke(null,t1,t2,t3));
    }

    public Query3<T1, T2, T3> orderBy(ExpressionTree.E3<Void, T1, T2, T3> e3)
    {
        bases.add(new OrderBy(e3.invoke(null, t1, t2, t3), false));
        return this;
    }

    public Query3<T1, T2, T3> descOrderBy(ExpressionTree.E3<Void, T1, T2, T3> e3)
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
