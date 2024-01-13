package io.github.kiryu1223.baseDao.core.Queryer;

import io.github.kiryu1223.baseDao.core.Base.Base;
import io.github.kiryu1223.baseDao.core.DBUtil;
import io.github.kiryu1223.expressionTree.delegate.Func1;
import io.github.kiryu1223.expressionTree.delegate.Func4;
import io.github.kiryu1223.expressionTree.expressions.ExprTree;

import java.util.List;
import java.util.Map;

public class Select4<T1, T2, T3, T4, R> extends SelectBase
{
    private final ExprTree<Func4<T1, T2, T3, T4, R>> exprTree;

    public Select4(ExprTree<Func4<T1, T2, T3, T4, R>> exprTree, List<Base> bases, List<Class<?>> joinClasses)
    {
        super(exprTree, bases, joinClasses);
        this.exprTree = exprTree;
    }

    public List<R> toList()
    {
        tryGetEntity();
        return DBUtil.startQueryToList4(entity, exprTree);
    }

    public <Key> Map<Key, R> toMap(Func1<R, Key> getKey)
    {
        tryGetEntity();
        return DBUtil.startQueryToMap4(entity, exprTree, getKey);
    }

    public Select4<T1, T2, T3, T4, R> distinct()
    {
        return distinct(true);
    }

    public Select4<T1, T2, T3, T4, R> distinct(boolean sw)
    {
        isDistinct = sw;
        return this;
    }

    public ExprTree<Func4<T1, T2, T3, T4, R>> getExprTree()
    {
        return exprTree;
    }
}
