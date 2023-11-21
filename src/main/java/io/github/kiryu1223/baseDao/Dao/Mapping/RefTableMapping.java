package io.github.kiryu1223.baseDao.Dao.Mapping;

import java.lang.reflect.Method;

public class RefTableMapping extends BaseMapping
{
    private final Class<?> target;

    public RefTableMapping(Class<?> target,Method method)
    {
        super(method);
        this.target = target;
    }

    public Class<?> getTarget()
    {
        return target;
    }
}
