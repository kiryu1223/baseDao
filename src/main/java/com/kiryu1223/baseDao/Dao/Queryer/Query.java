package com.kiryu1223.baseDao.Dao.Queryer;

import com.kiryu1223.baseDao.Dao.Base.*;
import com.kiryu1223.baseDao.Dao.DBUtil;
import com.kiryu1223.baseDao.Dao.Entity;
import com.kiryu1223.baseDao.Dao.Func.Func0;
import com.kiryu1223.baseDao.Dao.Func.Func1;
import com.kiryu1223.baseDao.Dao.Func.Func2;
import com.kiryu1223.baseDao.Dao.JoinType;
import com.kiryu1223.baseDao.Dao.Resolve;
import com.kiryu1223.baseDao.ExpressionV2.DbRefExpression;
import com.kiryu1223.baseDao.ExpressionV2.IExpression;
import com.kiryu1223.baseDao.ExpressionV2.NewExpression;
import com.kiryu1223.baseDao.ExpressionV2.OperatorExpression;

import com.kiryu1223.baseDao.Error.NoWayException;
import com.kiryu1223.baseDao.Dao.Statement.Statement;

import java.util.List;
import java.util.Map;

public class Query<T> extends Statement<T>
{
    private Entity entity = null;

    public Query(DBUtil dbUtil, Class<T> c1)
    {
        super(dbUtil, c1);
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

    public List<T> toList()
    {
        tryGetEntity();
        return dbUtil.startQuery(entity, IExpression.New(c1));
    }

    public <Key> Map<Key, T> toMap(Func0<T, Key> getKey)
    {
        tryGetEntity();
        return dbUtil.startQuery(entity, IExpression.New(c1), getKey);
    }

    public <Key, Value> Map<Key, Value> toMap(Func0<T, Key> getKey, Func0<T, Value> getValue)
    {
        tryGetEntity();
        return dbUtil.startQuery(entity, IExpression.New(c1), getKey, getValue);
    }

    public void toListAndThen(Func2<List<T>> then)
    {
        tryGetEntity();
        var list = dbUtil.startQuery(entity, IExpression.New(c1));
        then.invoke(list);
    }

    public <Key> void toMapAndThen(Func0<T, Key> getKey, Func2<Map<Key, T>> then)
    {
        tryGetEntity();
        var map = dbUtil.startQuery(entity, IExpression.New(c1), getKey);
        then.invoke(map);
    }

    public <Key, Value> void toMapAndThen(Func0<T, Key> getKey, Func0<T, Value> getValue, Func2<Map<Key, Value>> then)
    {
        tryGetEntity();
        var map = dbUtil.startQuery(entity, IExpression.New(c1), getKey, getValue);
        then.invoke(map);
    }

    private void tryGetEntity()
    {
        if (entity == null)
        {
            entity = Resolve.query(false, bases, IExpression.New(c1), getQueryClasses(), null);
        }
    }

    public Query<T> where(Func1<T> func)
    {
        throw new NoWayException();
    }

    public <R> QueryResult<R> select(Func0<T,R> func)
    {
        throw new NoWayException();
    }

    public <R> QueryResult<R> selectDistinct(Func0<T,R> func)
    {
        throw new NoWayException();
    }

    public Query<T> where(OperatorExpression operatorExpression)
    {
        bases.add(new Where(operatorExpression));
        return this;
    }

    public <R> QueryResult<R> select(NewExpression<R> newExpression)
    {
        return new QueryResult<R>(dbUtil, false, bases, getQueryClasses(), null, newExpression);
    }

    public <R> QueryResult<R> selectDistinct(NewExpression<R> newExpression)
    {
        return new QueryResult<R>(dbUtil, true, bases, getQueryClasses(), null, newExpression);
    }

    public <R> Query<T> orderBy(Func0<T, R> func)
    {
        throw new NoWayException();
    }
    public <R> Query<T> descOrderBy(Func0<T, R> func)
    {
        throw new NoWayException();
    }

    public Query<T> orderBy(DbRefExpression dbRefExpression)
    {
        bases.add(new OrderBy(dbRefExpression, false));
        return this;
    }
    public Query<T> descOrderBy(DbRefExpression dbRefExpression)
    {
        bases.add(new OrderBy(dbRefExpression, true));
        return this;
    }

    public Query<T> take(int count)
    {
        bases.add(new Take(count));
        return this;
    }

    public Query<T> skip(int count)
    {
        bases.add(new Skip(count));
        return this;
    }

    public Query<T> If(boolean flag, Func2<Query<T>> If)
    {
        if (flag) If.invoke(this);
        return this;
    }

    public Query<T> IfElse(boolean flag, Func2<Query<T>> If, Func2<Query<T>> Else)
    {
        if (flag)
        {
            If.invoke(this);
        }
        else
        {
            Else.invoke(this);
        }
        return this;
    }

    public <T2> Query2<T, T2> innerJoin(Class<T2> c)
    {
        bases.add(new Join(c, JoinType.Inner));

        return new Query2<>(dbUtil, bases, List.of(c), c1, c);
    }

    public <T2> Query2<T, T2> leftJoin(Class<T2> c)
    {
        bases.add(new Join(c, JoinType.Left));
        return new Query2<>(dbUtil, bases, List.of(c), c1, c);
    }

    public <T2> Query2<T, T2> rightJoin(Class<T2> c)
    {
        bases.add(new Join(c, JoinType.Right));
        return new Query2<>(dbUtil, bases, List.of(c), c1, c);
    }

    public <T2> Query2<T, T2> fullJoin(Class<T2> c)
    {
        bases.add(new Join(c, JoinType.Full));
        return new Query2<>(dbUtil, bases, List.of(c), c1, c);
    }
}
