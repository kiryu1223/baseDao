package com.kiryu1223.baseDao.Dao.Statement;

import com.kiryu1223.baseDao.Dao.DBUtil;
import com.kiryu1223.baseDao.Dao.Base.Base;

import java.util.ArrayList;
import java.util.List;

public abstract class Statement<T>
{
    protected final Class<T> c1;
    protected final DBUtil dbUtil;
    protected final List<Base> bases = new ArrayList<>();

    public Statement(DBUtil dbUtil,Class<T> c1)
    {
        this.c1 = c1;
        this.dbUtil = dbUtil;
    }
    public List<Base> getBases()
    {
        return bases;
    }

    public Class<T> getC1()
    {
        return c1;
    }
    public List<Class<?>> getQueryClasses()
    {
        return List.of(c1);
    }
}
