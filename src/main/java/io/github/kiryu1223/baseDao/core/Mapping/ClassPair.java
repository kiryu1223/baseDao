package io.github.kiryu1223.baseDao.core.Mapping;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ClassPair extends Pair
{
    private final Class<?> clazz;
    private final Object[] objects;
    private final int index;
    private final List<Field> fields;

    public ClassPair(Object object, Class<?> clazz, int index, Object[] objects, List<Field> fields)
    {
        super(object);
        this.clazz = clazz;
        this.objects = objects;
        this.index = index;
        this.fields = fields;
    }

    @Override
    public void set(ResultSet resultSet, int[] index) throws IllegalAccessException, SQLException
    {
        for (Field field : fields)
        {
            Object value = resultSet.getObject(index[0]++, field.getType());
            field.set(object, value);
        }
    }

    @Override
    public void reLoad() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
    {
        object = clazz.getConstructor().newInstance();
        objects[index] = object;
    }
}
