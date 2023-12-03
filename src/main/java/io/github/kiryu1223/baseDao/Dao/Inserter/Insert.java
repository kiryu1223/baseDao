package io.github.kiryu1223.baseDao.Dao.Inserter;

import io.github.kiryu1223.baseDao.Dao.Base.SetData;
import io.github.kiryu1223.baseDao.Dao.Cud.Cud;
import io.github.kiryu1223.baseDao.Dao.DBUtil;
import io.github.kiryu1223.baseDao.Dao.Entity;
import io.github.kiryu1223.baseDao.Dao.Resolve;
import io.github.kiryu1223.baseDao.Error.NoWayException;
import io.github.kiryu1223.expressionTree.Expression;
import io.github.kiryu1223.expressionTree.FunctionalInterface.ExpressionTree;
import io.github.kiryu1223.expressionTree.FunctionalInterface.IReturnVoid;

public class Insert<T> extends Cud<T>
{
    private Entity entity = null;
    public Insert(Class<T> c1)
    {
        super(c1);
    }

    public Insert<T> set(@Expression IReturnVoid<T> func)
    {
        throw new NoWayException();
    }

    public Insert<T> set(ExpressionTree.E1<Void,T> e1)
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
