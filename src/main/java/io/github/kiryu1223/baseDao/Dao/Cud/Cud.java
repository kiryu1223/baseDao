package io.github.kiryu1223.baseDao.Dao.Cud;

import io.github.kiryu1223.baseDao.Dao.Statement.Statement;

public abstract class Cud<T> extends Statement<T>
{
    public Cud(Class<T> c1)
    {
        super(c1);
    }
}
