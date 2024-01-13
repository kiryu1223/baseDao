package io.github.kiryu1223.baseDao.core.Queryer;

import io.github.kiryu1223.baseDao.core.Base.*;
import io.github.kiryu1223.baseDao.core.JoinType;
import io.github.kiryu1223.expressionTree.delegate.Func2;
import io.github.kiryu1223.expressionTree.expressions.ExprTree;

import java.util.List;

public class Query2<T1, T2> extends QueryBase
{
    private final Class<T1> c1;
    private final Class<T2> c2;

    public Query2(Class<T1> c1, Class<T2> c2)
    {
        this.c1 = c1;
        this.c2 = c2;
    }

    public Query2(List<Base> bases, List<Class<?>> joins, Class<T1> c1, Class<T2> c2)
    {
        super(bases, joins);
        this.c1 = c1;
        this.c2 = c2;
    }

    public Query2<T1, T2> on(ExprTree<Func2<T1, T2, Boolean>> e2)
    {
        bases.add(new On(e2.getTree()));
        return this;
    }

    public Query2<T1, T2> where(ExprTree<Func2<T1, T2, Boolean>> e2)
    {
        bases.add(new Where(e2.getTree()));
        return this;
    }

    public Query2<T1, T2> orderBy(ExprTree<Func2<T1, T2, Object>> e2)
    {
        bases.add(new OrderBy(e2.getTree(), false));
        return this;
    }

    public Query2<T1, T2> descOrderBy(ExprTree<Func2<T1, T2, Object>> e2)
    {
        bases.add(new OrderBy(e2.getTree(), true));
        return this;
    }

    public <R> Select2<T1, T2, R> select(ExprTree<Func2<T1, T2, R>> r)
    {
        return new Select2<>(r, bases, joins);
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
        bases.add(new Join(c, JoinType.Inner, 2));
        joins.add(c);
        return new Query3<>(bases, joins, c1, c2, c);
    }

    public <T3> Query3<T1, T2, T3> leftJoin(Class<T3> c)
    {
        bases.add(new Join(c, JoinType.Left, 2));
        joins.add(c);
        return new Query3<>(bases, joins, c1, c2, c);
    }

    public <T3> Query3<T1, T2, T3> rightJoin(Class<T3> c)
    {
        bases.add(new Join(c, JoinType.Right, 2));
        joins.add(c);
        return new Query3<>(bases, joins, c1, c2, c);
    }

    public <T3> Query3<T1, T2, T3> fullJoin(Class<T3> c)
    {
        bases.add(new Join(c, JoinType.Full, 2));
        joins.add(c);
        return new Query3<>(bases, joins, c1, c2, c);
    }
}
