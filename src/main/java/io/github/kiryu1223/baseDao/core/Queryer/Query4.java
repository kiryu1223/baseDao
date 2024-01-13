package io.github.kiryu1223.baseDao.core.Queryer;

import io.github.kiryu1223.baseDao.core.Base.*;
import io.github.kiryu1223.expressionTree.delegate.Func4;
import io.github.kiryu1223.expressionTree.expressions.ExprTree;


import java.util.List;

public class Query4<T1, T2, T3, T4> extends QueryBase
{
    private final Class<T1> c1;
    private final Class<T2> c2;
    private final Class<T3> c3;
    private final Class<T4> c4;

    public Query4(Class<T1> c1, Class<T2> c2, Class<T3> c3, Class<T4> c4)
    {
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.c4 = c4;
    }

    public Query4(List<Base> bases, List<Class<?>> joins, Class<T1> c1, Class<T2> c2, Class<T3> c3, Class<T4> c4)
    {
        super(bases, joins);
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.c4 = c4;
    }

    public Query4<T1, T2, T3, T4> on(ExprTree<Func4<T1, T2, T3, T4, Boolean>> e4)
    {
        bases.add(new On(e4.getTree()));
        return this;
    }


    public Query4<T1, T2, T3, T4> where(ExprTree<Func4<T1, T2, T3, T4, Boolean>> e4)
    {
        bases.add(new Where(e4.getTree()));
        return this;
    }

    public <R> Select4<T1, T2, T3, T4, R> select(ExprTree<Func4<T1, T2, T3, T4, R>> r)
    {
        return new Select4<>(r, bases, joins);
    }


    public Query4<T1, T2, T3, T4> orderBy(ExprTree<Func4<T1, T2, T3, T4, Object>> e4)
    {
        bases.add(new OrderBy(e4.getTree(), false));
        return this;
    }

    public Query4<T1, T2, T3, T4> descOrderBy(ExprTree<Func4<T1, T2, T3, T4, Object>> e4)
    {
        bases.add(new OrderBy(e4.getTree(), true));
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
