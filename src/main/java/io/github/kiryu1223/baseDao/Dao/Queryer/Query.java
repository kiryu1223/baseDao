package io.github.kiryu1223.baseDao.Dao.Queryer;

import io.github.kiryu1223.baseDao.Dao.*;
import io.github.kiryu1223.baseDao.Dao.Base.*;
import io.github.kiryu1223.baseDao.Dao.Statement.Statement;

import io.github.kiryu1223.baseDao.Error.NoWayException;
import io.github.kiryu1223.expressionTree.Expression;
import io.github.kiryu1223.expressionTree.FunctionalInterface.ExpressionTree;
import io.github.kiryu1223.expressionTree.FunctionalInterface.IReturnBoolean;
import io.github.kiryu1223.expressionTree.FunctionalInterface.IReturnGeneric;
import io.github.kiryu1223.expressionTree.FunctionalInterface.IReturnVoid;
import io.github.kiryu1223.expressionTree.expressionV2.IExpression;
import io.github.kiryu1223.expressionTree.expressionV2.NewExpression;

import java.util.ArrayList;
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
        return entity.toSql();
    }

    public Entity toEntity()
    {
        tryGetEntity();
        return entity;
    }

    public void toEntityAndThen(IReturnVoid<Entity> then)
    {
        tryGetEntity();
        then.invoke(entity);
    }

    public List<T> toList()
    {
        tryGetEntity();
        return DBUtil.startQuery(entity, IExpression.New(c1));
    }

    public <Key> Map<Key, T> toMap(IReturnGeneric.G1<T, Key> getKey)
    {
        tryGetEntity();
        return DBUtil.startQuery(entity, IExpression.New(c1), getKey);
    }

    public <Key, Value> Map<Key, Value> toMap(IReturnGeneric.G1<T, Key> getKey, IReturnGeneric.G1<T, Value> getValue)
    {
        tryGetEntity();
        return DBUtil.startQuery(entity, IExpression.New(c1), getKey, getValue);
    }

    public void toListAndThen(IReturnVoid<List<T>> then)
    {
        tryGetEntity();
        List<T> list = DBUtil.startQuery(entity, IExpression.New(c1));
        then.invoke(list);
    }

    public <Key> void toMapAndThen(IReturnGeneric.G1<T, Key> getKey, IReturnVoid<Map<Key, T>> then)
    {
        tryGetEntity();
        Map<Key, T> map = DBUtil.startQuery(entity, IExpression.New(c1), getKey);
        then.invoke(map);
    }

    public <Key, Value> void toMapAndThen(IReturnGeneric.G1<T, Key> getKey, IReturnGeneric.G1<T, Value> getValue, IReturnVoid<Map<Key, Value>> then)
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

    public Query<T> where(@Expression IReturnBoolean.B1<T> func)
    {
        throw new NoWayException();
    }

    public <R> Query<T> orderBy(@Expression IReturnGeneric.G1<T, R> func)
    {
        throw new NoWayException();
    }

    public <R> Query<T> descOrderBy(@Expression IReturnGeneric.G1<T, R> func)
    {
        throw new NoWayException();
    }

    public <R> QueryResult<R> select(@Expression(NewExpression.class) IReturnGeneric.G1<T, R> func)
    {
        throw new NoWayException();
    }

    public Query<T> where(ExpressionTree.E1<Void, T> e1)
    {
        bases.add(new Where(e1.invoke(null, t1)));
        return this;
    }

    public <R> QueryResult<R> select(ExpressionTree.NR1<Void, T, R> r)
    {
        return new QueryResult<R>(bases, getQueryClasses(), getQueryTargets(), null, r.invoke(null, t1));
    }

    public Query<T> orderBy(ExpressionTree.E1<Void, T> e1)
    {
        bases.add(new OrderBy(e1.invoke(null, t1), false));
        return this;
    }

    public Query<T> descOrderBy(ExpressionTree.E1<Void, T> e1)
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
