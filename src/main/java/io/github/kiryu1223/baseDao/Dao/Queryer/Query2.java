package io.github.kiryu1223.baseDao.Dao.Queryer;

import io.github.kiryu1223.baseDao.Dao.Base.*;
import io.github.kiryu1223.baseDao.Dao.Statement.Statement2;
import io.github.kiryu1223.baseDao.Dao.JoinType;
import io.github.kiryu1223.baseDao.Error.NoWayException;
import io.github.kiryu1223.expressionTree.Expression;
import io.github.kiryu1223.expressionTree.FunctionalInterface.ExpressionTree;
import io.github.kiryu1223.expressionTree.FunctionalInterface.IReturnBoolean;
import io.github.kiryu1223.expressionTree.FunctionalInterface.IReturnGeneric;
import io.github.kiryu1223.expressionTree.expressionV2.NewExpression;

import java.util.List;

public class Query2<T1, T2> extends Statement2<T1, T2>
{
    public Query2(List<Base> bases, List<Class<?>> joins, Class<T1> c1, Class<T2> c2)
    {
        super(bases, joins, c1, c2);
    }

    public Query2<T1, T2> on(@Expression IReturnBoolean.B2<T1, T2> func)
    {
        throw new NoWayException();
    }

    public Query2<T1, T2> where(@Expression IReturnBoolean.B2<T1, T2> func)
    {
        throw new NoWayException();
    }

    public <R> Query2<T1, T2> orderBy(@Expression IReturnGeneric.G2<T1, T2, R> func)
    {
        throw new NoWayException();
    }

    public <R> Query2<T1, T2> descOrderBy(@Expression IReturnGeneric.G2<T1, T2, R> func)
    {
        throw new NoWayException();
    }

    public <R> QueryResult<R> select(@Expression(NewExpression.class) IReturnGeneric.G2<T1, T2, R> func)
    {
        throw new NoWayException();
    }

    public Query2<T1, T2> on(ExpressionTree.E2<Void, T1, T2> e2)
    {
        bases.add(new On(e2.invoke(null, t1, t2)));
        return this;
    }

    public Query2<T1, T2> where(ExpressionTree.E2<Void, T1, T2> e2)
    {
        bases.add(new Where(e2.invoke(null, t1, t2)));
        return this;
    }

    public Query2<T1, T2> orderBy(ExpressionTree.E2<Void, T1, T2> e2)
    {
        bases.add(new OrderBy(e2.invoke(null, t1, t2), false));
        return this;
    }

    public Query2<T1, T2> descOrderBy(ExpressionTree.E2<Void, T1, T2> e2)
    {
        bases.add(new OrderBy(e2.invoke(null, t1, t2), true));
        return this;
    }

    public <R> QueryResult<R> select(ExpressionTree.NR2<Void, T1, T2, R> r)
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
