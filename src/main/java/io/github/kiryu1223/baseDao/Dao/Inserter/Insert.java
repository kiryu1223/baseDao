package io.github.kiryu1223.baseDao.Dao.Inserter;

import io.github.kiryu1223.baseDao.Dao.Base.SetData;
import io.github.kiryu1223.baseDao.Dao.Cud.Cud;
import io.github.kiryu1223.baseDao.Resolve.Expression;
import io.github.kiryu1223.baseDao.Dao.DBUtil;
import io.github.kiryu1223.baseDao.Dao.Entity;
import io.github.kiryu1223.baseDao.Dao.ExpressionFunc;
import io.github.kiryu1223.baseDao.Dao.Func.Func2;
import io.github.kiryu1223.baseDao.Dao.Resolve;
import io.github.kiryu1223.baseDao.Error.NoWayException;

public class Insert<T> extends Cud<T>
{
    private Entity entity = null;
    public Insert(Class<T> c1)
    {
        super(c1);
    }

    public Insert<T> set(@Expression Func2<T> func)
    {
        throw new NoWayException();
    }

    public Insert<T> set(ExpressionFunc.E1<Void,T> e1)
    {
        bases.add(new SetData(e1.invoke(null,t1)));
        return this;
    }

    public Entity toEntity()
    {
        return Resolve.cud(this);
    }

    public String toSql()
    {
        tryGetEntity();
        return entity.toSql();
    }

    public int doSave()
    {
        tryGetEntity();
        return DBUtil.startUpdate(entity);
    }

    private void tryGetEntity()
    {
        if (entity == null)
        {
            entity = Resolve.insert(this);
        }
    }
}
