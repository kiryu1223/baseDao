package io.github.kiryu1223.baseDao.core.Queryer;

import io.github.kiryu1223.baseDao.core.Base.*;
import io.github.kiryu1223.baseDao.core.JoinType;
import io.github.kiryu1223.expressionTree.delegate.Func3;
import io.github.kiryu1223.expressionTree.expressions.ExprTree;

import java.util.List;

public class Query3<T1, T2, T3> extends QueryBase
{
    private final Class<T1> c1;
    private final Class<T2> c2;
    private final Class<T3> c3;

    public Query3(Class<T1> c1, Class<T2> c2, Class<T3> c3)
    {
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
    }

    public Query3(List<Base> bases, List<Class<?>> joins, Class<T1> c1, Class<T2> c2, Class<T3> c3)
    {
        super(bases, joins);
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
    }

    public Query3<T1, T2, T3> on(ExprTree<Func3<T1, T2, T3, Boolean>> e3)
    {
        bases.add(new On(e3.getTree()));
        return this;
    }


    public Query3<T1, T2, T3> where(ExprTree<Func3<T1, T2, T3, Boolean>> e3)
    {
        bases.add(new Where(e3.getTree()));
        return this;
    }

    public <R> Select3<T1, T2, T3, R> select(ExprTree<Func3<T1, T2, T3, R>> r)
    {
        return new Select3<>(r, bases, joins);
    }

    public Query3<T1, T2, T3> orderBy(ExprTree<Func3<T1, T2, T3, Object>> e3)
    {
        bases.add(new OrderBy(e3.getTree(), false));
        return this;
    }

    public Query3<T1, T2, T3> descOrderBy(ExprTree<Func3<T1, T2, T3, Object>> e3)
    {
        bases.add(new OrderBy(e3.getTree(), true));
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
        bases.add(new Join(c, JoinType.Inner, 3));
        joins.add(c);
        return new Query4<>(bases, joins, c1, c2, c3, c);
    }

    public <T4> Query4<T1, T2, T3, T4> leftJoin(Class<T4> c)
    {
        bases.add(new Join(c, JoinType.Left, 3));
        joins.add(c);
        return new Query4<>(bases, joins, c1, c2, c3, c);
    }

    public <T4> Query4<T1, T2, T3, T4> rightJoin(Class<T4> c)
    {
        bases.add(new Join(c, JoinType.Right, 3));
        joins.add(c);
        return new Query4<>(bases, joins, c1, c2, c3, c);
    }

    public <T4> Query4<T1, T2, T3, T4> fullJoin(Class<T4> c)
    {
        bases.add(new Join(c, JoinType.Full, 3));
        joins.add(c);
        return new Query4<>(bases, joins, c1, c2, c3, c);
    }
}
