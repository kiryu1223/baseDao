package com.kiryu1223.baseDao.Dao.Statement;

import com.kiryu1223.baseDao.Dao.DBUtil;
import com.kiryu1223.baseDao.Dao.Base.Base;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public abstract class Statement4<T1, T2, T3, T4>
{
    protected final T1 t1;
    protected final T2 t2;
    protected final T3 t3;
    protected final T4 t4;
    protected final Class<T1> c1;
    protected final Class<T2> c2;
    protected final Class<T3> c3;
    protected final Class<T4> c4;
    protected final List<Class<?>> joins = new ArrayList<>();
    protected final DBUtil dbUtil;
    protected final List<Base> bases = new ArrayList<>();

    public Statement4(DBUtil dbUtil, List<Base> bases, List<Class<?>> joins, Class<T1> c1, Class<T2> c2, Class<T3> c3, Class<T4> c4)
    {
        this.dbUtil = dbUtil;
        if (bases != null) this.bases.addAll(bases);
        if (joins != null) this.joins.addAll(joins);
        this.c1=c1;
        this.c2=c2;
        this.c3=c3;
        this.c4=c4;
        try
        {
            t1 = c1.getConstructor().newInstance();
            t2 = c2.getConstructor().newInstance();
            t3 = c3.getConstructor().newInstance();
            t4 = c4.getConstructor().newInstance();
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
        return List.of(c1,c2,c3,c4);
    }

    public List<?> getQueryTargets()
    {
        return List.of(t1, t2, t3, t4);
    }
}
