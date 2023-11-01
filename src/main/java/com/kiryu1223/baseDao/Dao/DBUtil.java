package com.kiryu1223.baseDao.Dao;

import com.kiryu1223.baseDao.Dao.Mapping.*;
import com.kiryu1223.baseDao.ExpressionV2.*;
import com.kiryu1223.baseDao.Dao.Func.Func0;


import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DBUtil
{
    private final DataSource dataSource;

    public DBUtil(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    private void setValues(PreparedStatement ps, List<Object> values) throws SQLException
    {
        if (values != null)
        {
            for (int i = 0; i < values.size(); i++)
            {
                ps.setObject(i + 1, values.get(i));
            }
        }
    }

    private List<Integer> setBatchValues(Connection conn, List<Entity> entityList) throws SQLException
    {
        Entity ee = null;
        PreparedStatement ps = null;
        List<Integer> ints = new ArrayList<>(entityList.size());
        for (var entity : entityList)
        {
            if (ee != null && ee.sql.toString().contentEquals(entity.sql))
            {
                for (int i = 0; i < entity.values.size(); i++)
                {
                    ps.setObject(i + 1, entity.values.get(i));
                }
                ps.addBatch();
            }
            else
            {
                if (ps != null)
                {
                    var count = ps.executeBatch();
                    for (int i : count)
                    {
                        ints.add(i);
                    }
                }
                ps = conn.prepareStatement(entity.sql.toString());
                for (int i = 0; i < entity.values.size(); i++)
                {
                    ps.setObject(i + 1, entity.values.get(i));
                }
                ps.addBatch();
                ee = entity;
            }
        }
        return ints;
    }

    private static final Map<Class<?>, Boolean> javaClassMap = new ConcurrentHashMap<>();

    private static boolean isJavaClass(Class<?> c)
    {
        if (!javaClassMap.containsKey(c))
        {
            javaClassMap.put(c, c.getClassLoader() == null);
        }
        return javaClassMap.get(c);
    }

    private <R> List<R> getResultList(ResultSet rs, NewExpression<R> newExpression) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
    {
        var md = rs.getMetaData();
        var resultType = newExpression.getTarget();
        List<R> result = new ArrayList<>();
        if (newExpression.getExpressions().isEmpty())
        {
            var map = Cache.getDbNameToFieldMapping(resultType);
            while (rs.next())
            {
                var r = resultType.getConstructor().newInstance();
                for (int i = 1; i <= md.getColumnCount(); i++)
                {
                    var rName = md.getColumnLabel(i);
                    var field = map.get(rName);
                    var o = rs.getObject(rName, field.getType());
                    if (o != null)
                    {
                        field.set(r, o);
                    }
                }
                result.add(r);
            }
        }
        else
        {
            List<BaseMapping> baseMappings = new ArrayList<>();
            doResolve(newExpression, baseMappings);
            var constructor = resultType.getConstructor();
            while (rs.next())
            {
                var r = constructor.newInstance();
                int[] offset = {1};
                for (var iMapping : baseMappings)
                {
                    iMapping.setParent(r);
                    DMain(iMapping, offset, rs);
                }
                result.add(r);
            }
        }
        return result;
    }

    private void DMain(BaseMapping iMapping, int[] offset, ResultSet rs) throws SQLException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException
    {
        if (iMapping instanceof SetterMapping)
        {
            var setterMapping = (SetterMapping) iMapping;
            var setter = setterMapping.getMethod();
            var o = rs.getObject(offset[0]++, setter.getParameterTypes()[0]);
            if (o != null)
            {
                setter.invoke(setterMapping.getParent(), o);
            }
        }
        else if (iMapping instanceof RefTableMapping)
        {
            var refTableMapping = (RefTableMapping) iMapping;
            var target = refTableMapping.getTarget();
            var map = Cache.getDbNameToFieldMapping(target);
            var temp = target.getConstructor().newInstance();
            var metaData = rs.getMetaData();
            for (int i = 0; i < map.size(); i++)
            {
                var name = metaData.getColumnLabel(offset[0]);
                var field = map.get(name);
                var o = rs.getObject(offset[0]++, field.getType());
                if (o != null)
                {
                    field.set(temp, o);
                }
            }
            var setter = refTableMapping.getMethod();
            setter.invoke(refTableMapping.getParent(), temp);
        }
        else if (iMapping instanceof NewClassMapping)
        {
            var newClassMapping = (NewClassMapping) iMapping;
            var nt = newClassMapping.getTarget().getConstructor().newInstance();
            for (var mapping : newClassMapping.getMappings())
            {
                mapping.setParent(nt);
                DMain(mapping, offset, rs);
            }
            var setter = newClassMapping.getMethod();
            setter.invoke(newClassMapping.getParent(), nt);
        }
    }

    private <Key, R> Map<Key, R> getResultMap(ResultSet rs, NewExpression<R> newExpression, Func0<R, Key> getKey) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
    {
        var md = rs.getMetaData();
        var resultType = newExpression.getTarget();
        Map<Key, R> result = new HashMap<>();
        var list = getResultList(rs, newExpression);
        for (R r : list)
        {
            result.put(getKey.invoke(r), r);
        }
        return result;
    }

    private <Key, Value, R> Map<Key, Value> getResultMap(ResultSet rs, NewExpression<R> newExpression, Func0<R, Key> getKey, Func0<R, Value> getValue) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
    {
        var md = rs.getMetaData();
        var resultType = newExpression.getTarget();
        Map<Key, Value> result = new HashMap<>();
        var list = getResultList(rs, newExpression);
        for (R r : list)
        {
            result.put(getKey.invoke(r), getValue.invoke(r));
        }
        return result;
    }

    public <R> List<R> startQuery(Entity entity, NewExpression<R> newExpression)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            System.out.println(entity);
            conn = dataSource.getConnection();
            ps = conn.prepareStatement(entity.sql.toString());
            setValues(ps, entity.values);
            rs = ps.executeQuery();
            return getResultList(rs, newExpression);
        }
        catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException |
               SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                if (rs != null && !rs.isClosed()) rs.close();
                if (ps != null && !ps.isClosed()) ps.close();
                if (conn != null && !conn.isClosed()) conn.close();
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public <Key, R> Map<Key, R> startQuery(Entity entity, NewExpression<R> newExpression, Func0<R, Key> getKey)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            ps = conn.prepareStatement(entity.sql.toString());
            setValues(ps, entity.values);
            rs = ps.executeQuery();
            return getResultMap(rs, newExpression, getKey);
        }
        catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException |
               SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                if (rs != null && !rs.isClosed()) rs.close();
                if (ps != null && !ps.isClosed()) ps.close();
                if (conn != null && !conn.isClosed()) conn.close();
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public <Key, Value, R> Map<Key, Value> startQuery(Entity entity, NewExpression<R> newExpression, Func0<R, Key> getKey, Func0<R, Value> getValue)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource.getConnection();
            ps = conn.prepareStatement(entity.sql.toString());
            setValues(ps, entity.values);
            rs = ps.executeQuery();
            return getResultMap(rs, newExpression, getKey, getValue);
        }
        catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException |
               SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                if (rs != null && !rs.isClosed()) rs.close();
                if (ps != null && !ps.isClosed()) ps.close();
                if (conn != null && !conn.isClosed()) conn.close();
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public int startUpdate(Entity entity)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = dataSource.getConnection();
            ps = conn.prepareStatement(entity.toString());
            setValues(ps, entity.values);
            return ps.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                if (ps != null && !ps.isClosed()) ps.close();
                if (conn != null && !conn.isClosed()) conn.close();
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public List<Integer> batchUpdate(List<Entity> entityList)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        Boolean autoCommit = null;
        try
        {
            conn = dataSource.getConnection();
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            var result = setBatchValues(conn, entityList);
            conn.commit();
            return result;
        }
        catch (SQLException e)
        {
            try
            {
                if (conn != null) conn.rollback();
            }
            catch (SQLException ex)
            {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                if (conn != null)
                {
                    if (autoCommit != null)
                    {
                        conn.setAutoCommit(autoCommit);
                    }
                    conn.close();
                }
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private void doResolve(NewExpression<?> newExpression, List<BaseMapping> baseMappings) throws NoSuchMethodException
    {
        var map = Cache.getMethodNameToMethodMapping(newExpression.getTarget());
        for (var expression : newExpression.getExpressions())
        {
            doResolveStart(expression, map, baseMappings);
        }
    }

    private void doResolveStart(IExpression expression, Map<String, Method> methodMap, List<BaseMapping> baseMappings) throws NoSuchMethodException
    {
        if (expression instanceof MethodCallExpression)
        {
            var methodCall = (MethodCallExpression) expression;
            var method = methodMap.get(methodCall.getSelectedMethod());
            if (methodCall.getParams().get(0) instanceof NewExpression<?>)
            {
                var newExpression = (NewExpression<?>) methodCall.getParams().get(0);
                if (!newExpression.getExpressions().isEmpty())
                {
                    var newClassMapping = new NewClassMapping(newExpression.getTarget(), method);
                    List<BaseMapping> list = new ArrayList<>();
                    doResolve(newExpression, list);
                    newClassMapping.getMappings().addAll(list);
                    baseMappings.add(newClassMapping);
                }
                else
                {
                    var refTableMapping = new RefTableMapping(newExpression.getTarget(), method);
                    baseMappings.add(refTableMapping);
                }
            }
            else
            {
                baseMappings.add(new SetterMapping(method));
            }
        }
    }

    public boolean transactionCud(List<Entity> entityList, Integer transactionIsolation)
    {
        Connection conn = null;
        Boolean autoCommit = null;
        try
        {
            conn = dataSource.getConnection();
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            if (transactionIsolation != null)
            {
                conn.setTransactionIsolation(transactionIsolation);
            }
            for (var entity : entityList)
            {
                var ps = conn.prepareStatement(entity.toString());
                setValues(ps, entity.values);
                var count = ps.executeUpdate();
                if (count < 1)
                {
                    conn.rollback();
                    return false;
                }
            }
            conn.commit();
            return true;
        }
        catch (SQLException e)
        {
            if (conn != null)
            {
                try
                {
                    conn.rollback();
                }
                catch (SQLException ex)
                {
                    throw new RuntimeException(ex);
                }
            }
            throw new RuntimeException(e);
        }
        finally
        {
            if (conn != null)
            {
                try
                {
                    if (autoCommit != null)
                    {
                        conn.setAutoCommit(autoCommit);
                    }
                    conn.close();
                }
                catch (SQLException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}