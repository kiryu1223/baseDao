package io.github.kiryu1223.baseDao.core.Queryer;

import io.github.kiryu1223.baseDao.core.Base.Base;
import io.github.kiryu1223.baseDao.core.DBUtil;
import io.github.kiryu1223.expressionTree.delegate.Func1;
import io.github.kiryu1223.expressionTree.expressions.ExprTree;

import java.util.List;
import java.util.Map;

public class Select<T, R> extends SelectBase
{
    private final ExprTree<Func1<T, R>> exprTree;

    public Select(List<Base> bases, List<Class<?>> joinClasses, ExprTree<Func1<T, R>> exprTree)
    {
        super(exprTree, bases, joinClasses);
        this.exprTree = exprTree;
    }

    public List<R> toList()
    {
        tryGetEntity();
        return DBUtil.startQueryToList(entity, exprTree);
    }

    public <Key> Map<Key, R> toMap(Func1<R, Key> getKey)
    {
        tryGetEntity();
        return DBUtil.startQueryToMap(entity, exprTree, getKey);
    }

    public Select<T, R> distinct()
    {
        return distinct(true);
    }

    public Select<T, R> distinct(boolean sw)
    {
        isDistinct = sw;
        return this;
    }

    public ExprTree<Func1<T, R>> getExprTree()
    {
        return exprTree;
    }
}
