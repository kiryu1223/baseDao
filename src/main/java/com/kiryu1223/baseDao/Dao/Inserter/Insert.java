package com.kiryu1223.baseDao.Dao.Inserter;

import com.kiryu1223.baseDao.Dao.Base.SetData;
import com.kiryu1223.baseDao.Dao.Cud.Cud;
import com.kiryu1223.baseDao.Dao.DBUtil;
import com.kiryu1223.baseDao.Dao.Entity;
import com.kiryu1223.baseDao.Dao.Func.Func2;
import com.kiryu1223.baseDao.Dao.Resolve;
import com.kiryu1223.baseDao.Error.NoWayException;
import com.kiryu1223.baseDao.ExpressionV2.MappingsExpression;

public class Insert<T> extends Cud<T>
{
    public Insert(DBUtil dbUtil, Class<T> c1)
    {
        super(dbUtil, c1);
    }

    public Insert<T> pushData(Func2<T> func)
    {
        throw new NoWayException();
    }

    public Insert<T> pushData(MappingsExpression mappingExpressions)
    {
        bases.add(new SetData(mappingExpressions));
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

    public int doSave()
    {
        var e = Resolve.cud(this);
        return dbUtil.startUpdate(e);
    }
}
