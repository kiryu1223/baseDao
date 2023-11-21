package io.github.kiryu1223.baseDao.Dao.Updater;

import io.github.kiryu1223.baseDao.Dao.Base.SetData;
import io.github.kiryu1223.baseDao.Dao.Base.Where;
import io.github.kiryu1223.baseDao.Dao.Cud.Cud;
import io.github.kiryu1223.baseDao.Dao.ExpressionFunc;
import io.github.kiryu1223.baseDao.Resolve.Expression;
import io.github.kiryu1223.baseDao.Dao.DBUtil;
import io.github.kiryu1223.baseDao.Dao.Entity;
import io.github.kiryu1223.baseDao.Dao.Func.Func1;
import io.github.kiryu1223.baseDao.Dao.Func.Func2;
import io.github.kiryu1223.baseDao.Dao.Resolve;
import io.github.kiryu1223.baseDao.Error.NoWayException;
import io.github.kiryu1223.baseDao.ExpressionV2.IExpression;

public class Update<T> extends Cud<T>
{
    private Entity entity = null;
    public Update(Class<T> c1)
    {
        super(c1);
    }

    public Update<T> set(@Expression Func2<T> func)
    {
        throw new NoWayException();
    }

    public Update<T> set(ExpressionFunc.E1<Void,T> e1)
    {
        bases.add(new SetData(e1.invoke(null,t1)));
        return this;
    }

    public Update<T> where(@Expression Func1<T> func)
    {
        throw new NoWayException();
    }

    public Update<T> where(IExpression expression)
    {
        bases.add(new Where(expression));
        return this;
    }

    public int doUpdate()
    {
        tryGetEntity();
        return DBUtil.startUpdate(entity);
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

    private void tryGetEntity()
    {
        if (entity == null)
        {
            entity = Resolve.update(this);
        }
    }
}
