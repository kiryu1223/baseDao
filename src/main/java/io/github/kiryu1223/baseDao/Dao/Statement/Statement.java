package io.github.kiryu1223.baseDao.Dao.Statement;

import io.github.kiryu1223.baseDao.Dao.Base.Base;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public abstract class Statement<T>
{
    protected final T t1;
    protected final Class<T> c1;
    protected final List<Base> bases = new ArrayList<>();

    public Statement(Class<T> c1)
    {
        this.c1 = c1;
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
        List<Class<?>> list = new ArrayList<>();
        list.add(c1);
        return list;
    }

    public List<?> getQueryTargets()
    {
        List<Object> list = new ArrayList<>();
        list.add(t1);
        return list;
    }
}
