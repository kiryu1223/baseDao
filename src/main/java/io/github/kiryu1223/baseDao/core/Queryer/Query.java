package io.github.kiryu1223.baseDao.core.Queryer;

import io.github.kiryu1223.baseDao.core.*;
import io.github.kiryu1223.baseDao.core.Base.*;
import io.github.kiryu1223.expressionTree.delegate.Func1;
import io.github.kiryu1223.expressionTree.expressions.ExprTree;

import java.util.*;

public class Query<T> extends QueryBase
{
    private final Class<T> c1;

    public Query(Class<T> c1)
    {
        this.c1 = c1;
    }

    public Query<T> where(ExprTree<Func1<T, Boolean>> e1)
    {
        bases.add(new Where(e1.getTree()));
        return this;
    }

    public <R> Select<T,R> select(ExprTree<Func1<T, R>> r)
    {
        return new Select<T,R>(bases, Collections.emptyList(), r);
    }

    public <R> Query<T> orderBy(ExprTree<Func1<T, R>> e1)
    {
        bases.add(new OrderBy(e1.getTree(), false));
        return this;
    }

    public <R> Query<T> orderByDesc(ExprTree<Func1<T, R>> e1)
    {
        bases.add(new OrderBy(e1.getTree(), true));
        return this;
    }

    public Query<T> take(int count)
    {
        bases.add(new Take(count));
        return this;
    }

    public Query<T> skip(int count)
    {
        bases.add(new Skip(count));
        return this;
    }

    public <T2> Query2<T, T2> innerJoin(Class<T2> c)
    {
        bases.add(new Join(c, JoinType.Inner, 1));
        List<Class<?>> list = new ArrayList<>();
        list.add(c);
        return new Query2<>(bases, list, c1, c);
    }

    public <T2> Query2<T, T2> leftJoin(Class<T2> c)
    {
        bases.add(new Join(c, JoinType.Left, 1));
        List<Class<?>> list = new ArrayList<>();
        list.add(c);
        return new Query2<>(bases, list, c1, c);
    }

    public <T2> Query2<T, T2> rightJoin(Class<T2> c)
    {
        bases.add(new Join(c, JoinType.Right, 1));
        List<Class<?>> list = new ArrayList<>();
        list.add(c);
        return new Query2<>(bases, list, c1, c);
    }

    public <T2> Query2<T, T2> fullJoin(Class<T2> c)
    {
        bases.add(new Join(c, JoinType.Full, 1));
        List<Class<?>> list = new ArrayList<>();
        list.add(c);
        return new Query2<>(bases, list, c1, c);
    }
}
