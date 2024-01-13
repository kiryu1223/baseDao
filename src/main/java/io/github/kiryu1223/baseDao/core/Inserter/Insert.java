package io.github.kiryu1223.baseDao.core.Inserter;

import io.github.kiryu1223.baseDao.core.Base.SetData;
import io.github.kiryu1223.baseDao.core.Cud.Cud;
import io.github.kiryu1223.baseDao.core.DBUtil;
import io.github.kiryu1223.baseDao.core.Entity;
import io.github.kiryu1223.baseDao.core.Resolver;
import io.github.kiryu1223.expressionTree.delegate.Action1;
import io.github.kiryu1223.expressionTree.expressions.ExprTree;

public class Insert<T> extends Cud<T>
{
    private Entity entity = null;
    public Insert(Class<T> c1)
    {
        super(c1);
    }

    public Insert<T> set(ExprTree<Action1<T>> e1)
    {
        bases.add(new SetData(e1.getTree()));
        return this;
    }

    public Entity toEntity()
    {
        return Resolver.cud(this);
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
            entity = Resolver.insert(this);
        }
    }
}
