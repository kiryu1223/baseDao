package io.github.kiryu1223.baseDao.core.Updater;

import io.github.kiryu1223.baseDao.core.Base.SetData;
import io.github.kiryu1223.baseDao.core.Base.Where;
import io.github.kiryu1223.baseDao.core.Cud.Cud;
import io.github.kiryu1223.baseDao.core.DBUtil;
import io.github.kiryu1223.baseDao.core.Entity;
import io.github.kiryu1223.baseDao.core.Resolver;
import io.github.kiryu1223.expressionTree.delegate.Action1;
import io.github.kiryu1223.expressionTree.delegate.Func1;
import io.github.kiryu1223.expressionTree.expressions.ExprTree;

public class Update<T> extends Cud<T>
{
    private Entity entity = null;
    public Update(Class<T> c1)
    {
        super(c1);
    }

    public Update<T> set(ExprTree<Action1<T>> e1)
    {
        bases.add(new SetData(e1.getTree()));
        return this;
    }

    public Update<T> where(ExprTree<Func1<T,Boolean>> e1)
    {
        bases.add(new Where(e1.getTree()));
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
            entity = Resolver.update(this);
        }
    }
}
