package com.kiryu1223.baseDao.Dao.Inserter;

import com.kiryu1223.baseDao.Dao.Base.Base;

public class InsertOne<T> extends Base
{
    private final T t;

    public InsertOne(T t)
    {
        this.t = t;
    }

    public T getTarget()
    {
        return t;
    }
}
