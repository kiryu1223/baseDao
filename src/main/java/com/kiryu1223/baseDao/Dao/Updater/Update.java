package com.kiryu1223.baseDao.Dao.Updater;

import com.kiryu1223.baseDao.Dao.Cud.Cud;
import com.kiryu1223.baseDao.Dao.DBUtil;
import com.kiryu1223.baseDao.Dao.Entity;
import com.kiryu1223.baseDao.Dao.Func.Func1;
import com.kiryu1223.baseDao.Dao.Func.Func2;
import com.kiryu1223.baseDao.Dao.Resolve;
import com.kiryu1223.baseDao.Dao.Statement.Statement;
import com.kiryu1223.baseDao.Error.NoWayException;
import com.kiryu1223.baseDao.ExpressionV2.OperatorExpression;
import com.kiryu1223.baseDao.Dao.Base.Where;

import java.lang.reflect.InvocationTargetException;

public class Update<T> extends Cud<T>
{
    public Update(Class<T> c1)
    {
        super(c1);
    }

    public Update<T> set(Func2<T> func)
    {
        try
        {
            T t = c1.getConstructor().newInstance();
            func.invoke(t);
            bases.add(new Set<T>(t));
            return this;
        }
        catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    public Update<T> where(Func1<T> func)
    {
        throw new NoWayException();
    }

    public Update<T> where(OperatorExpression operatorExpression)
    {
        bases.add(new Where(operatorExpression));
        return this;
    }

    public int doUpdate()
    {
        Entity e = Resolve.cud(this);
        return DBUtil.startUpdate(e);
    }

    public Entity toEntity()
    {
        return Resolve.cud(this);
    }

    public String toSql()
    {
        return Resolve.cud(this).toString();
    }
}
