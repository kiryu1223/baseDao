package io.github.kiryu1223.baseDao.core.Statement;

import io.github.kiryu1223.baseDao.core.Base.Base;

import java.util.ArrayList;
import java.util.List;

public abstract class Statement2<T1, T2>
{
    protected final Class<T1> c1;
    protected final Class<T2> c2;
    protected final List<Class<?>> joins = new ArrayList<>();
    protected final List<Base> bases = new ArrayList<>();

    public Statement2(List<Base> bases,List<Class<?>> joins, Class<T1> c1, Class<T2> c2)
    {
        if(bases!=null)this.bases.addAll(bases);
        if(joins!=null)this.joins.addAll(joins);
        this.c1=c1;
        this.c2=c2;
    }

    public List<Base> getBases()
    {
        return bases;
    }

    public List<Class<?>> getJoins()
    {
        return joins;
    }

    public List<Class<?>> getQueryClasses()
    {
        List<Class<?>> list = new ArrayList<>();
        list.add(c1);
        list.add(c2);
        return list;
    }
}
