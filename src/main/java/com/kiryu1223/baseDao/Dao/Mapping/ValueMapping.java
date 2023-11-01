package com.kiryu1223.baseDao.Dao.Mapping;

import java.lang.reflect.Method;

public class ValueMapping extends BaseMapping
{
    private final Object value;
    public ValueMapping(Method method, Object value)
    {
        super(method);
        this.value = value;
    }

    public Object getValue()
    {
        return value;
    }
}
