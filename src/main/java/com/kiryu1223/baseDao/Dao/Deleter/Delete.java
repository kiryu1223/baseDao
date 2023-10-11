package com.kiryu1223.baseDao.Dao.Deleter;

import com.kiryu1223.baseDao.Dao.DBUtil;
import com.kiryu1223.baseDao.Dao.Entity;
import com.kiryu1223.baseDao.Dao.Func.Func1;
import com.kiryu1223.baseDao.Dao.Resolve;
import com.kiryu1223.baseDao.Dao.Statement.Statement;
import com.kiryu1223.baseDao.ExpressionV2.OperatorExpression;
import com.kiryu1223.baseDao.Dao.Base.Where;

public class Delete<T> extends Statement<T>
{
    public Delete(DBUtil dbUtil, Class<T> c1)
    {
        super(dbUtil, c1);
    }

    public Delete<T> where(Func1<T> func)
    {
        throw new RuntimeException("no way");
    }

    public Delete<T> where(OperatorExpression operatorExpression)
    {
        bases.add(new Where(operatorExpression));
        return this;
    }

    public Entity toEntity()
    {
        return Resolve.delete(this);
    }

    public String toSql()
    {
        return Resolve.delete(this).sql.toString();
    }

    public int doDelete()
    {
        var e = Resolve.delete(this);
        return dbUtil.startUpdate(e);
    }
}
