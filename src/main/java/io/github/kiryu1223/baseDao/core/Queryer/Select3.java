package io.github.kiryu1223.baseDao.core.Queryer;

import io.github.kiryu1223.baseDao.core.Base.Base;
import io.github.kiryu1223.baseDao.core.DBUtil;
import io.github.kiryu1223.expressionTree.delegate.Func1;
import io.github.kiryu1223.expressionTree.delegate.Func3;
import io.github.kiryu1223.expressionTree.expressions.ExprTree;

import java.util.List;
import java.util.Map;

public class Select3<T1, T2, T3, R> extends SelectBase
{
    private final ExprTree<Func3<T1, T2, T3, R>> exprTree;

    public Select3(ExprTree<Func3<T1, T2, T3, R>> exprTree, List<Base> bases, List<Class<?>> joinClasses)
    {
        super(exprTree, bases, joinClasses);
        this.exprTree = exprTree;
    }

    public List<R> toList()
    {
        tryGetEntity();
        return DBUtil.startQueryToList3(entity, exprTree);
    }

    public <Key> Map<Key, R> toMap(Func1<R, Key> getKey)
    {
        tryGetEntity();
        return DBUtil.startQueryToMap3(entity, exprTree, getKey);
    }

    public Select3<T1, T2, T3, R> distinct()
    {
        return distinct(true);
    }

    public Select3<T1, T2, T3, R> distinct(boolean sw)
    {
        isDistinct = sw;
        return this;
    }

    public ExprTree<Func3<T1, T2, T3, R>> getExprTree()
    {
        return exprTree;
    }
}
