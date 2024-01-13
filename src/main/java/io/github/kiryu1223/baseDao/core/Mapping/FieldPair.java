package io.github.kiryu1223.baseDao.core.Mapping;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FieldPair extends Pair
{
    protected final Field field;

    public FieldPair(Field field, Object o)
    {
        super(o);
        this.field = field;
    }

    public Class<?> getType()
    {
        return field.getType();
    }

    @Override
    public void set(ResultSet resultSet,int[] index) throws IllegalAccessException, SQLException
    {
        Object value = resultSet.getObject(index[0]++, getType());
        field.set(object,value);
    }
}
