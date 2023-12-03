package io.github.kiryu1223.baseDao.Dao.Deleter;

import io.github.kiryu1223.baseDao.Dao.Base.Where;
import io.github.kiryu1223.baseDao.Dao.Cud.Cud;
import io.github.kiryu1223.baseDao.Dao.Resolve;
import io.github.kiryu1223.baseDao.Error.NoWayException;
import io.github.kiryu1223.baseDao.Dao.DBUtil;
import io.github.kiryu1223.baseDao.Dao.Entity;
import io.github.kiryu1223.expressionTree.Expression;
import io.github.kiryu1223.expressionTree.FunctionalInterface.IReturnBoolean;
import io.github.kiryu1223.expressionTree.expressionV2.IExpression;

public class Delete<T> extends Cud<T>
{
    private Entity entity = null;

    public Delete(Class<T> c1)
    {
        super(c1);
    }

    public Delete<T> where(@Expression IReturnBoolean.B1<T> func)
    {
        throw new NoWayException();
    }

    public Delete<T> where(IExpression expression)
    {
        bases.add(new Where(expression));
        return this;
    }

    public Entity toEntity()
    {
        tryGetEntity();
        return entity;
    }

    public String toSql()
    {
        tryGetEntity();
        return entity.toSql();
    }

    public int doDelete()
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
