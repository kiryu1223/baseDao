package io.github.kiryu1223.baseDao.core.Queryer;

import io.github.kiryu1223.baseDao.core.Base.Base;
import io.github.kiryu1223.baseDao.core.Entity;
import io.github.kiryu1223.baseDao.core.Resolver;
import io.github.kiryu1223.expressionTree.expressions.ExprTree;

import java.util.List;

public abstract class SelectBase
{
    private final ExprTree<?> exprTree;
    protected final List<Base> bases;
    protected final List<Class<?>> joinClasses;
    protected boolean isDistinct;
    protected Entity entity = null;

    public SelectBase(ExprTree<?> exprTree, List<Base> bases, List<Class<?>> joinClasses)
    {
        this.exprTree = exprTree;
        this.joinClasses = joinClasses;
        this.bases = bases;
    }

    public boolean isDistinct()
    {
        return isDistinct;
    }

    public String toSql()
    {
        tryGetEntity();
        return entity.sql.toString();
    }

    public Entity toEntity()
    {
        tryGetEntity();
        return entity;
    }

    protected void tryGetEntity()
    {
        if (entity == null)
        {
            entity = Resolver.query(isDistinct, bases, exprTree, joinClasses);
        }
    }
}
