package io.github.kiryu1223.baseDao.Dao.Statement;

import io.github.kiryu1223.baseDao.Dao.Base.Base;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public abstract class Statement2<T1, T2>
{
    protected final T1 t1;
    protected final T2 t2;
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
        try
        {
            t1=c1.getConstructor().newInstance();
            t2=c2.getConstructor().newInstance();
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
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

    public List<?> getQueryTargets()
    {
        List<Object> list = new ArrayList<>();
        list.add(t1);
        list.add(t2);
        return list;
    }
}
