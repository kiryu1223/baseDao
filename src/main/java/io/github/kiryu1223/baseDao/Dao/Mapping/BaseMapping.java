package io.github.kiryu1223.baseDao.Dao.Mapping;

import java.lang.reflect.Method;

public abstract class BaseMapping
{
    private Object parent;
    private final Method method;
    public BaseMapping(Method method)
    {
        this.method = method;
    }
    public Object getParent()
    {
        return parent;
    }

    public void setParent(Object parent)
    {
        this.parent = parent;
    }

    public Method getMethod()
    {
        return method;
    }
}
