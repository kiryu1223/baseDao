package io.github.kiryu1223.baseDao.core.Mapping;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class Pair
{
    protected Object object;
    public Pair(Object object)
    {
        this.object = object;
    }

    public abstract void set(ResultSet resultSet,int[] index) throws IllegalAccessException, SQLException;

    public void reLoad() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
    {

    }
}
