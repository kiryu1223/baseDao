package com.kiryu1223.baseDao.Dao;

import java.util.ArrayList;
import java.util.List;

public class BatchEntity
{
    public final StringBuilder sql = new StringBuilder();
    public final List<List<Object>> values = new ArrayList<>();

    @Override
    public String toString()
    {
        return "Sql: " + sql.toString() + "\n" +
                "values: " + values.toString();
    }
}
