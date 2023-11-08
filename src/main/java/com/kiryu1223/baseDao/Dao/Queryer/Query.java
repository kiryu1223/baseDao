package com.kiryu1223.baseDao.Dao.Queryer;

import com.kiryu1223.baseDao.Dao.*;
import com.kiryu1223.baseDao.Dao.Base.*;
import com.kiryu1223.baseDao.Dao.Func.Func0;
import com.kiryu1223.baseDao.Dao.Func.Func1;
import com.kiryu1223.baseDao.Dao.Func.Func2;
import com.kiryu1223.baseDao.ExpressionV2.*;

import com.kiryu1223.baseDao.Error.NoWayException;
import com.kiryu1223.baseDao.Dao.Statement.Statement;
import com.kiryu1223.baseDao.Resolve.Expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Query<T> extends Statement<T>
{
    private Entity entity = null;

    public Query(Class<T> c1)
    {
        super(c1);
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
        return DBUtil.startQuery(entity, IExpression.New(c1));
    }

    public <Key> Map<Key, T> toMap(Func0<T, Key> getKey)
    {
        tryGetEntity();
        return DBUtil.startQuery(entity, IExpression.New(c1), getKey);
    }

    public <Key, Value> Map<Key, Value> toMap(Func0<T, Key> getKey, Func0<T, Value> getValue)
    {
        tryGetEntity();
        return DBUtil.startQuery(entity, IExpression.New(c1), getKey, getValue);
    }

    public void toListAndThen(Func2<List<T>> then)
    {
        tryGetEntity();
        List<T> list = DBUtil.startQuery(entity, IExpression.New(c1));
        then.invoke(list);
    }

    public <Key> void toMapAndThen(Func0<T, Key> getKey, Func2<Map<Key, T>> then)
    {
        tryGetEntity();
        Map<Key, T> map = DBUtil.startQuery(entity, IExpression.New(c1), getKey);
        then.invoke(map);
    }

    public <Key, Value> void toMapAndThen(Func0<T, Key> getKey, Func0<T, Value> getValue, Func2<Map<Key, Value>> then)
    {
        tryGetEntity();
        Map<Key, Value> map = DBUtil.startQuery(entity, IExpression.New(c1), getKey, getValue);
        then.invoke(map);
    }

    private void tryGetEntity()
    {
        if (entity == null)
        {
            entity = Resolve.query(false, bases, IExpression.New(c1), getQueryClasses(), getQueryTargets(), null);
        }
    }

    public Query<T> where(@Expression Func1<T> func)
    {
        throw new NoWayException();
    }

    public <R> Query<T> orderBy(@Expression Func0<T, R> func)
    {
        throw new NoWayException();
    }

    public <R> Query<T> descOrderBy(@Expression Func0<T, R> func)
    {
        throw new NoWayException();
    }

    public <R> QueryResult<R> select(@Expression(NewExpression.class) Func0<T, R> func)
    {
        throw new NoWayException();
    }

    public Query<T> where(ExpressionFunc.E1<Void, T> e1)
    {
        bases.add(new Where(e1.invoke(null, t1)));
        return this;
    }

    public <R> QueryResult<R> select(ExpressionFunc.NR1<Void, T, R> r)
    {
        return new QueryResult<R>(bases, getQueryClasses(), getQueryTargets(), null, r.invoke(null, t1));
    }

    public Query<T> orderBy(ExpressionFunc.E1<Void, T> e1)
    {
        bases.add(new OrderBy(e1.invoke(null, t1), false));
        return this;
    }

    public Query<T> descOrderBy(ExpressionFunc.E1<Void, T> e1)
    {
        bases.add(new OrderBy(e1.invoke(null, t1), true));
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

    public <T2> Query2<T, T2> innerJoin(Class<T2> c)
    {
        bases.add(new Join(c, JoinType.Inner));
        List<Class<?>> list = new ArrayList<>();
        list.add(c);
        return new Query2<>(bases, list, c1, c);
    }

    public <T2> Query2<T, T2> leftJoin(Class<T2> c)
    {
        bases.add(new Join(c, JoinType.Left));
        List<Class<?>> list = new ArrayList<>();
        list.add(c);
        return new Query2<>(bases, list, c1, c);
    }

    public <T2> Query2<T, T2> rightJoin(Class<T2> c)
    {
        bases.add(new Join(c, JoinType.Right));
        List<Class<?>> list = new ArrayList<>();
        list.add(c);
        return new Query2<>(bases, list, c1, c);
    }

    public <T2> Query2<T, T2> fullJoin(Class<T2> c)
    {
        bases.add(new Join(c, JoinType.Full));
        List<Class<?>> list = new ArrayList<>();
        list.add(c);
        return new Query2<>(bases, list, c1, c);
    }
}
