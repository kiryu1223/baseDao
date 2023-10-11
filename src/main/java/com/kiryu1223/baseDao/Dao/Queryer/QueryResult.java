package com.kiryu1223.baseDao.Dao.Queryer;

import com.kiryu1223.baseDao.Dao.DBUtil;
import com.kiryu1223.baseDao.Dao.Func.Func0;
import com.kiryu1223.baseDao.Dao.Func.Func2;
import com.kiryu1223.baseDao.Dao.Resolve;
import com.kiryu1223.baseDao.ExpressionV2.NewExpression;
import com.kiryu1223.baseDao.Dao.Base.Base;
import com.kiryu1223.baseDao.Dao.Entity;

import java.util.List;
import java.util.Map;

public class QueryResult<R>
{
    private final DBUtil dbUtil;
    private final List<Base> bases;
    private final List<Class<?>> queryClasses;
    private final List<Class<?>> joinClasses;
    private final NewExpression<R> newExpression;
    private final boolean isDistinct;
    private Entity entity = null;

    public QueryResult(DBUtil dbUtil, boolean isDistinct, List<Base> bases, List<Class<?>> queryClasses, List<Class<?>> joinClasses, NewExpression<R> newExpression)
    {
        this.dbUtil = dbUtil;
        this.isDistinct = isDistinct;
        this.queryClasses = queryClasses;
        this.joinClasses = joinClasses;
        this.newExpression = newExpression;
        this.bases = bases;
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

    public void toEntityAndThen(Func2<Entity> then)
    {
        tryGetEntity();
        then.invoke(entity);
    }

    public List<R> toList()
    {
        tryGetEntity();
        return dbUtil.startQuery(entity, newExpression);
    }

    public <Key> Map<Key, R> toMap(Func0<R, Key> getKey)
    {
        tryGetEntity();
        return dbUtil.startQuery(entity, newExpression, getKey);
    }

    public <Key, Value> Map<Key, Value> toMap(Func0<R, Key> getKey, Func0<R, Value> getValue)
    {
        tryGetEntity();
        return dbUtil.startQuery(entity, newExpression, getKey, getValue);
    }

    public void toListAndThen(Func2<List<R>> then)
    {
        tryGetEntity();
        var list = dbUtil.startQuery(entity, newExpression);
        then.invoke(list);
    }

    public <Key> void toMapAndThen(Func0<R, Key> getKey, Func2<Map<Key, R>> then)
    {
        tryGetEntity();
        var map = dbUtil.startQuery(entity, newExpression, getKey);
        then.invoke(map);
    }

    public <Key, Value> void toMapAndThen(Func0<R, Key> getKey, Func0<R, Value> getValue, Func2<Map<Key, Value>> then)
    {
        tryGetEntity();
        var map = dbUtil.startQuery(entity, newExpression, getKey, getValue);
        then.invoke(map);
    }

    public boolean isDistinct()
    {
        return isDistinct;
    }

    public NewExpression<R> getNewExpression()
    {
        return newExpression;
    }

    private void tryGetEntity()
    {
        if (entity == null)
        {
            entity = Resolve.query(isDistinct, bases, newExpression,queryClasses, joinClasses);
        }
    }
}
