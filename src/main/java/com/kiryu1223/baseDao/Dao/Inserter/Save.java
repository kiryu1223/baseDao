package com.kiryu1223.baseDao.Dao.Inserter;

import com.kiryu1223.baseDao.Dao.Cud.Cud;
import com.kiryu1223.baseDao.Dao.DBUtil;

public class Save<T>
{
    private final T t;

    public Save(T t)
    {
        this.t = t;
    }

    public T getTarget()
    {
        return t;
    }
}
