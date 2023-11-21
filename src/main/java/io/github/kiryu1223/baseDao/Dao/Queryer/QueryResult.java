package io.github.kiryu1223.baseDao.Dao.Queryer;

import io.github.kiryu1223.baseDao.Dao.Base.Base;
import io.github.kiryu1223.baseDao.Dao.DBUtil;
import io.github.kiryu1223.baseDao.Dao.Func.Func0;
import io.github.kiryu1223.baseDao.Dao.Func.Func2;
import io.github.kiryu1223.baseDao.Dao.Resolve;
import io.github.kiryu1223.baseDao.ExpressionV2.NewExpression;
import io.github.kiryu1223.baseDao.Dao.Entity;

import java.util.List;
import java.util.Map;

public class QueryResult<R>
{
    private final List<Base> bases;
    private final List<Class<?>> queryClasses;
    private final List<?> queryTargets;
    private final List<Class<?>> joinClasses;
    private final NewExpression<R> newExpression;
    private boolean isDistinct;
    private Entity entity = null;

    public QueryResult(List<Base> bases, List<Class<?>> queryClasses, List<?> queryTargets, List<Class<?>> joinClasses, NewExpression<R> newExpression)
    {
        this.queryClasses = queryClasses;
        this.queryTargets = queryTargets;
        this.joinClasses = joinClasses;
        this.bases = bases;
        this.newExpression = newExpression;
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
        return DBUtil.startQuery(entity, newExpression);
    }

    public <Key> Map<Key, R> toMap(Func0<R, Key> getKey)
    {
        tryGetEntity();
        return DBUtil.startQuery(entity, newExpression, getKey);
    }

    public <Key, Value> Map<Key, Value> toMap(Func0<R, Key> getKey, Func0<R, Value> getValue)
    {
        tryGetEntity();
        return DBUtil.startQuery(entity, newExpression, getKey, getValue);
    }

    public void toListAndThen(Func2<List<R>> then)
    {
        tryGetEntity();
        List<R> list = DBUtil.startQuery(entity, newExpression);
        then.invoke(list);
    }

    public <Key> void toMapAndThen(Func0<R, Key> getKey, Func2<Map<Key, R>> then)
    {
        tryGetEntity();
        Map<Key, R> map = DBUtil.startQuery(entity, newExpression, getKey);
        then.invoke(map);
    }

    public <Key, Value> void toMapAndThen(Func0<R, Key> getKey, Func0<R, Value> getValue, Func2<Map<Key, Value>> then)
    {
        tryGetEntity();
        Map<Key, Value> map = DBUtil.startQuery(entity, newExpression, getKey, getValue);
        then.invoke(map);
    }

    public QueryResult<R> distinct()
    {
        return distinct(true);
    }

    public QueryResult<R> distinct(boolean sw)
    {
        isDistinct = sw;
        return this;
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
            entity = Resolve.query(isDistinct, bases, newExpression, queryClasses,queryTargets, joinClasses);
        }
    }
}
