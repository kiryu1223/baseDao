package io.github.kiryu1223.baseDao.Dao.Mapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class NewClassMapping extends BaseMapping
{
    private final Class<?> target;
    private final List<BaseMapping> mappings=new ArrayList<>();

    public NewClassMapping(Class<?> target, Method method)
    {
        super(method);
        this.target = target;
    }

    public Class<?> getTarget()
    {
        return target;
    }

    public List<BaseMapping> getMappings()
    {
        return mappings;
    }
}
