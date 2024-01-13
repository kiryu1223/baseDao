package io.github.kiryu1223.baseDao.core.Cud;

import io.github.kiryu1223.baseDao.core.Statement.Statement;

public abstract class Cud<T> extends Statement<T>
{
    public Cud(Class<T> c1)
    {
        super(c1);
    }
}
