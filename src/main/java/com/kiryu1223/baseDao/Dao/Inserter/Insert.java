package com.kiryu1223.baseDao.Dao.Inserter;

import com.kiryu1223.baseDao.Dao.DBUtil;
import com.kiryu1223.baseDao.Dao.Entity;
import com.kiryu1223.baseDao.Dao.Func.Func2;
import com.kiryu1223.baseDao.Dao.Resolve;
import com.kiryu1223.baseDao.Dao.Statement.Statement;

import java.lang.reflect.InvocationTargetException;

public class Insert<T> extends Statement<T>
{
    public Insert(DBUtil dbUtil, Class<T> c1)
    {
        super(dbUtil, c1);
    }

    public Insert<T> add(Func2<T> func)
    {
        try
        {
            var t = c1.getConstructor().newInstance();
            func.invoke(t);
            bases.add(new InsertOne<T>(t));
            return this;
        }
        catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    public Entity toEntity()
    {
        return Resolve.insert(this);
    }

    public String toSql()
    {
        return Resolve.insert(this).sql.toString();
    }

    public int doSave()
    {
        var e = Resolve.insert(this);
        return dbUtil.startUpdate(e);
    }
}
