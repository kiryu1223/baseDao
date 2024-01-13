package io.github.kiryu1223.baseDao.core.Deleter;

import io.github.kiryu1223.baseDao.core.Base.Where;
import io.github.kiryu1223.baseDao.core.Cud.Cud;
import io.github.kiryu1223.baseDao.core.Resolver;
import io.github.kiryu1223.baseDao.core.DBUtil;
import io.github.kiryu1223.baseDao.core.Entity;
import io.github.kiryu1223.expressionTree.delegate.Func1;
import io.github.kiryu1223.expressionTree.expressions.ExprTree;

public class Delete<T> extends Cud<T>
{
    private Entity entity = null;

    public Delete(Class<T> c1)
    {
        super(c1);
    }

    public Delete<T> where(ExprTree<Func1<T,Boolean>> e1)
    {
        bases.add(new Where(e1.getTree()));
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
            entity = Resolver.insert(this);
        }
    }
}
