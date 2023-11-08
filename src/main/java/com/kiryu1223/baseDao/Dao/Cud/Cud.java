package com.kiryu1223.baseDao.Dao.Cud;

import com.kiryu1223.baseDao.Dao.DBUtil;
import com.kiryu1223.baseDao.Dao.Statement.Statement;

public abstract class Cud<T> extends Statement<T>
{
    public Cud(Class<T> c1)
    {
        super(c1);
    }
}
