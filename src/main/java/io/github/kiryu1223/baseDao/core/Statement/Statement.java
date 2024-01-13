package io.github.kiryu1223.baseDao.core.Statement;

import io.github.kiryu1223.baseDao.core.Base.Base;

import java.util.ArrayList;
import java.util.List;

public abstract class Statement<T>
{
    protected final Class<T> c1;
    protected final List<Base> bases = new ArrayList<>();

    public Statement(Class<T> c1)
    {
        this.c1 = c1;
    }

    public List<Base> getBases()
    {
        return bases;
    }

    public List<Class<?>> getQueryClasses()
    {
        List<Class<?>> list = new ArrayList<>();
        list.add(c1);
        return list;
    }

    public Class<?> getC1()
    {
        return c1;
    }
}
