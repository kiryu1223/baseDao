package io.github.kiryu1223.baseDao.Dao.Queryer;

import io.github.kiryu1223.baseDao.Dao.Base.*;
import io.github.kiryu1223.baseDao.Dao.Statement.Statement4;
import io.github.kiryu1223.baseDao.Error.NoWayException;
import io.github.kiryu1223.expressionTree.Expression;
import io.github.kiryu1223.expressionTree.FunctionalInterface.ExpressionTree;
import io.github.kiryu1223.expressionTree.FunctionalInterface.IReturnBoolean;
import io.github.kiryu1223.expressionTree.FunctionalInterface.IReturnGeneric;
import io.github.kiryu1223.expressionTree.expressionV2.NewExpression;


import java.util.List;

public class Query4<T1, T2, T3, T4> extends Statement4<T1, T2, T3, T4>
{
    public Query4(List<Base> bases, List<Class<?>> joins, Class<T1> c1, Class<T2> c2, Class<T3> c3, Class<T4> c4)
    {
        super(bases, joins, c1, c2, c3, c4);
    }

    public Query4<T1, T2, T3, T4> on(@Expression IReturnBoolean.B4<T1, T2, T3, T4> func)
    {
        throw new NoWayException();
    }

    public Query4<T1, T2, T3, T4> where(@Expression IReturnBoolean.B4<T1, T2, T3, T4> func)
    {
        throw new NoWayException();
    }

    public <R> Query4<T1, T2, T3, T4> orderBy(@Expression IReturnGeneric.G4<T1, T2, T3, T4, R> func)
    {
        throw new NoWayException();
    }

    public <R> Query4<T1, T2, T3, T4> descOrderBy(@Expression IReturnGeneric.G4<T1, T2, T3, T4, R> func)
    {
        throw new NoWayException();
    }

    public <R> QueryResult<R> select(@Expression(NewExpression.class) IReturnGeneric.G4<T1, T2, T3, T4, R> func)
    {
        throw new NoWayException();
    }

    public Query4<T1, T2, T3, T4> on(ExpressionTree.E4<Void, T1, T2, T3, T4> e4)
    {
        bases.add(new On(e4.invoke(null, t1, t2, t3, t4)));
        return this;
    }


    public Query4<T1, T2, T3, T4> where(ExpressionTree.E4<Void, T1, T2, T3, T4> e4)
    {
        bases.add(new Where(e4.invoke(null, t1, t2, t3, t4)));
        return this;
    }

    public <R> QueryResult<R> select(ExpressionTree.NR4<Void, T1, T2, T3, T4, R> r)
    {
        return new QueryResult<R>(bases, getQueryClasses(), getQueryTargets(), joins, r.invoke(null, t1, t2, t3, t4));
    }


    public Query4<T1, T2, T3, T4> orderBy(ExpressionTree.E4<Void, T1, T2, T3, T4> e4)
    {
        bases.add(new OrderBy(e4.invoke(null, t1, t2, t3, t4), false));
        return this;
    }

    public Query4<T1, T2, T3, T4> descOrderBy(ExpressionTree.E4<Void, T1, T2, T3, T4> e4)
    {
        bases.add(new OrderBy(e4.invoke(null, t1, t2, t3, t4), true));
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
}
