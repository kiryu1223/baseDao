package com.kiryu1223.baseDao.Dao;

import com.kiryu1223.baseDao.ExpressionV2.*;
import com.kiryu1223.baseDao.Dao.Func.Func0;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private void setBatchValues(PreparedStatement ps, List<List<Object>> valueList) throws SQLException
    {
        if (valueList != null)
        {
            for (List<Object> list : valueList)
            {
                for (int i = 0; i < list.size(); i++)
                {
                    ps.setObject(i + 1, list.get(i));
                }
                ps.addBatch();
            }
        }
    }

    private static boolean isJavaClass(Class<?> clz)
    {
        return clz != null && clz.getClassLoader() == null;
    }

    private <R> List<R> getResultList(ResultSet rs, NewExpression<R> newExpression) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
    {
        var md = rs.getMetaData();
        var resultType = newExpression.getTarget();
        List<R> result = new ArrayList<>();
        if (isJavaClass(resultType))
        {
            while (rs.next())
            {
                var o = rs.getObject(1, resultType);
                if (o != null)
                {
                    result.add(o);
                }
            }
        }
        else
        {
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
                            //if (o.getClass().equals(LocalDateTime.class)) o = Timestamp.valueOf((LocalDateTime) o);
                            field.set(r, o);
                        }
                    }
                    result.add(r);
                }
            }
            else
            {
                List<Method> methods = new ArrayList<>();
                Map<Method, Object> pairs = new HashMap<>();
                var methodMap = Cache.getMethodNameToMethodMapping(resultType);
                for (var expression : newExpression.getExpressions())
                {
                    doResolve(expression, methodMap, pairs, methods);
                }
                var constructor = resultType.getConstructor();

                while (rs.next())
                {
                    var r = constructor.newInstance();
                    for (int i = 1; i <= md.getColumnCount(); i++)
                    {
                        var setter = methods.get(i - 1);
                        var o = rs.getObject(i, setter.getParameterTypes()[0]);
                        if (o != null)
                        {
                            setter.invoke(r, o);
                        }
                    }
                    for (var entry : pairs.entrySet())
                    {
                        var k = entry.getKey();
                        var v = entry.getValue();
                        k.invoke(r, v);
                    }
                    result.add(r);
                }
            }
        }
        return result;
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
            System.out.println(entity);
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
            System.out.println(entity);
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
            ps = conn.prepareStatement(entity.sql.toString());
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

    public int[] batchUpdate(BatchEntity batchEntity)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = dataSource.getConnection();
            ps = conn.prepareStatement(batchEntity.sql.toString());
            setBatchValues(ps, batchEntity.values);
            return ps.executeBatch();
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


    private void doResolve(IExpression expression, Map<String, Method> methodsMap, Map<Method, Object> pairs, List<Method> methods)
    {
        if (expression instanceof MappingExpression)
        {
            doResolveMappingExpression((MappingExpression) expression, methodsMap, pairs, methods);
        }
    }

    private void doResolveMappingExpression(MappingExpression expression, Map<String, Method> methodsMap, Map<Method, Object> pairs, List<Method> methods)
    {
        if (expression.getValue() instanceof DbRefExpression || expression.getValue() instanceof DbFuncExpression)
        {
            methods.add(methodsMap.get(expression.getSource()));
        }
        else if (expression.getValue() instanceof ValueExpression)
        {
            var value = (ValueExpression) expression.getValue();
            pairs.put(methodsMap.get(expression.getSource()), value.getValue());
        }
    }
}