package com.kiryu1223.baseDao.Dao.Statement;

import com.kiryu1223.baseDao.Dao.DBUtil;
import com.kiryu1223.baseDao.Dao.Base.Base;

import java.util.ArrayList;
import java.util.List;

public abstract class Statement4<T1, T2, T3, T4>
{
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
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.c4 = c4;
    }

    public List<Base> getBases()
    {
        return bases;
    }

    public List<Class<?>> getQueryClasses()
    {
        return List.of(c1, c2, c3, c4);
    }

    public List<Class<?>> getJoins()
    {
        return joins;
    }
}
