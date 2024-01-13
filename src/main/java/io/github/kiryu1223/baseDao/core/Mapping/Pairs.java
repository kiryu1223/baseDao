package io.github.kiryu1223.baseDao.core.Mapping;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Pairs
{
    private final List<Pair> pairs;

    public Pairs(List<Pair> pairs) throws NoSuchMethodException
    {
        this.pairs = pairs;
    }

    public void set(ResultSet resultSet) throws SQLException, InvocationTargetException, IllegalAccessException
    {
        int[] index = {1};
        for (Pair pair : pairs)
        {
            pair.set(resultSet, index);
        }
    }

    public void reload() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException
    {
        for (Pair pair : pairs)
        {
            pair.reLoad();
        }
    }
}
