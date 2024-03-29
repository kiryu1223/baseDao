package io.github.kiryu1223.baseDao.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Entity
{
    public final StringBuilder sql = new StringBuilder();
    public final List<Object> values = new ArrayList<>();
    public final List<Class<?>> classes = new ArrayList<>();
    public final List<Method> methods = new ArrayList<>();


    @Override
    public String toString()
    {
        return "Sql: " + sql.toString() + "\n" +
                "values: " + values.toString();
    }

    public String toSql()
    {
        return sql.toString();
    }

    public <T> Entity append(T t)
    {
        sql.append(t);
        return this;
    }

    public Entity questionMarkAndValue(Object o)
    {
        sql.append("?");
        values.add(o);
        return this;
    }

    public Entity append(String s)
    {
        sql.append(s);
        return this;
    }

    public Entity blank()
    {
        if (sql.charAt(sql.length() - 1) != ' ') append(" ");
        return this;
    }

    public Entity deleteLast()
    {
        if (sql.length() > 0)
        {
            sql.deleteCharAt(sql.length() - 1);
        }
        return this;
    }

    public Entity tryDeleteLast()
    {
        if (sql.length() > 0)
        {
            int last = sql.length() - 1;
            switch (sql.charAt(last))
            {
                case ',':
                case ' ':
                case '(':
                case ')':
                    sql.deleteCharAt(last);
                    tryDeleteLast();
                    break;
            }
        }
        return this;
    }

    public Entity pushValue(Object o)
    {
        values.add(o);
        return this;
    }

    public void clear()
    {
        sql.setLength(0);
        values.clear();
    }

    public Entity changeOp()
    {
        String sqlStr = toSql().trim();
        if (sqlStr.endsWith("= null"))
        {
            String substring = sqlStr.substring(0, sqlStr.length() - 6);
            substring+="is null";
            sql.setLength(0);
            sql.append(substring);
        }
        else if (sqlStr.endsWith("<> null"))
        {
            String substring = sqlStr.substring(0, sqlStr.length() - 7);
            substring+="is not null";
            sql.setLength(0);
            sql.append(substring);
        }
        return this;
    }
}
