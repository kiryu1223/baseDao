package com.kiryu1223.baseDao.Dao.Updater;

import com.kiryu1223.baseDao.Dao.Base.Base;

public class Set<T> extends Base
{
    private final T t;

    public Set(T t)
    {
        this.t = t;
    }

    public T getTarget()
    {
        return t;
    }
}
