package com.kiryu1223.baseDao.Dao.Statement;

import com.kiryu1223.baseDao.Dao.Cache;
import com.kiryu1223.baseDao.Dao.DBUtil;
import com.kiryu1223.baseDao.Dao.Base.Base;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public abstract class Statement<T>
{
    protected final T t1;
    protected final Class<T> c1;
    protected final DBUtil dbUtil;
    protected final List<Base> bases = new ArrayList<>();


    public Statement(DBUtil dbUtil, Class<T> c1)
    {
        this.dbUtil = dbUtil;
        this.c1=c1;
        try
        {
            this.t1 = c1.getConstructor().newInstance();
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

    public List<Class<?>> getQueryClasses()
    {
        return List.of(t1.getClass());
    }
    public List<?> getQueryTargets()
    {
        return List.of(t1);
    }
}
