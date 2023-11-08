package com.kiryu1223.baseDao.Dao.Deleter;

import com.kiryu1223.baseDao.Dao.Cud.Cud;
import com.kiryu1223.baseDao.Dao.DBUtil;
import com.kiryu1223.baseDao.Dao.Entity;
import com.kiryu1223.baseDao.Dao.Func.Func1;
import com.kiryu1223.baseDao.Dao.Resolve;
import com.kiryu1223.baseDao.Dao.Statement.Statement;
import com.kiryu1223.baseDao.Error.NoWayException;
import com.kiryu1223.baseDao.ExpressionV2.OperatorExpression;
import com.kiryu1223.baseDao.Dao.Base.Where;

public class Delete<T> extends Cud<T>
{
    public Delete(Class<T> c1)
    {
        super(c1);
    }

    public Delete<T> where(Func1<T> func)
    {
        throw new NoWayException();
    }

    public Delete<T> where(OperatorExpression operatorExpression)
    {
        bases.add(new Where(operatorExpression));
        return this;
    }

    public Entity toEntity()
    {
        return Resolve.cud(this);
    }

    public String toSql()
    {
        return Resolve.cud(this).toString();
    }

    public int doDelete()
    {
        Entity e = Resolve.cud(this);
        return DBUtil.startUpdate(e);
    }
}
