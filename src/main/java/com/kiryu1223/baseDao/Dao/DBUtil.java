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
    private static DataSource dataSource0;

    private DBUtil() {}

    public static void setDataSource(DataSource dataSource)
    {
        dataSource0 = dataSource;
    }

    private static void setValues(PreparedStatement ps, List<Object> values) throws SQLException
    {
        if (values != null)
        {
            for (int i = 0; i < values.size(); i++)
            {
                ps.setObject(i + 1, values.get(i));
            }
        }
    }

    private static List<Integer> setBatchValues(Connection conn, List<Entity> entityList) throws SQLException
    {
        Entity ee = null;
        PreparedStatement ps = null;
        List<Integer> ints = new ArrayList<>(entityList.size());
        for (Entity entity : entityList)
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
                    int[] count = ps.executeBatch();
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

    private static <R> List<R> getResultList(ResultSet rs, NewExpression<R> newExpression) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
    {
        java.sql.ResultSetMetaData md = rs.getMetaData();
        Class<R> resultType = newExpression.getTarget();
        List<R> result = new ArrayList<>();
        if (newExpression.getExpressions().isEmpty())
        {
            Map<String, java.lang.reflect.Field> map = Cache.getDbNameToFieldMapping(resultType);
            while (rs.next())
            {
                R r = resultType.getConstructor().newInstance();
                for (int i = 1; i <= md.getColumnCount(); i++)
                {
                    String rName = md.getColumnLabel(i);
                    java.lang.reflect.Field field = map.get(rName);
                    Object o = rs.getObject(rName, field.getType());
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
            java.lang.reflect.Constructor<R> constructor = resultType.getConstructor();
            while (rs.next())
            {
                R r = constructor.newInstance();
                int[] offset = {1};
                for (BaseMapping iMapping : baseMappings)
                {
                    iMapping.setParent(r);
                    DMain(iMapping, offset, rs);
                }
                result.add(r);
            }
        }
        return result;
    }

    private static void DMain(BaseMapping iMapping, int[] offset, ResultSet rs) throws SQLException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException
    {
        if (iMapping instanceof SetterMapping)
        {
            SetterMapping setterMapping = (SetterMapping) iMapping;
            Method setter = setterMapping.getMethod();
            Object o = rs.getObject(offset[0]++, setter.getParameterTypes()[0]);
            if (o != null)
            {
                setter.invoke(setterMapping.getParent(), o);
            }
        }
        else if (iMapping instanceof RefTableMapping)
        {
            RefTableMapping refTableMapping = (RefTableMapping) iMapping;
            Class<?> target = refTableMapping.getTarget();
            Map<String, java.lang.reflect.Field> map = Cache.getDbNameToFieldMapping(target);
            Object temp = target.getConstructor().newInstance();
            java.sql.ResultSetMetaData metaData = rs.getMetaData();
            for (int i = 0; i < map.size(); i++)
            {
                String name = metaData.getColumnLabel(offset[0]);
                java.lang.reflect.Field field = map.get(name);
                Object o = rs.getObject(offset[0]++, field.getType());
                if (o != null)
                {
                    field.set(temp, o);
                }
            }
            Method setter = refTableMapping.getMethod();
            setter.invoke(refTableMapping.getParent(), temp);
        }
        else if (iMapping instanceof NewClassMapping)
        {
            NewClassMapping newClassMapping = (NewClassMapping) iMapping;
            Object nt = newClassMapping.getTarget().getConstructor().newInstance();
            for (BaseMapping mapping : newClassMapping.getMappings())
            {
                mapping.setParent(nt);
                DMain(mapping, offset, rs);
            }
            Method setter = newClassMapping.getMethod();
            setter.invoke(newClassMapping.getParent(), nt);
        }
    }

    private static <Key, R> Map<Key, R> getResultMap(ResultSet rs, NewExpression<R> newExpression, Func0<R, Key> getKey) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
    {
        java.sql.ResultSetMetaData md = rs.getMetaData();
        Class<R> resultType = newExpression.getTarget();
        Map<Key, R> result = new HashMap<>();
        List<R> list = getResultList(rs, newExpression);
        for (R r : list)
        {
            result.put(getKey.invoke(r), r);
        }
        return result;
    }

    private static <Key, Value, R> Map<Key, Value> getResultMap(ResultSet rs, NewExpression<R> newExpression, Func0<R, Key> getKey, Func0<R, Value> getValue) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
    {
        java.sql.ResultSetMetaData md = rs.getMetaData();
        Class<R> resultType = newExpression.getTarget();
        Map<Key, Value> result = new HashMap<>();
        List<R> list = getResultList(rs, newExpression);
        for (R r : list)
        {
            result.put(getKey.invoke(r), getValue.invoke(r));
        }
        return result;
    }

    public static <R> List<R> startQuery(Entity entity, NewExpression<R> newExpression)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            System.out.println(entity);
            conn = dataSource0.getConnection();
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

    public static <Key, R> Map<Key, R> startQuery(Entity entity, NewExpression<R> newExpression, Func0<R, Key> getKey)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource0.getConnection();
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

    public static <Key, Value, R> Map<Key, Value> startQuery(Entity entity, NewExpression<R> newExpression, Func0<R, Key> getKey, Func0<R, Value> getValue)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource0.getConnection();
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

    public static int startUpdate(Entity entity)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            System.out.println(entity);
            conn = dataSource0.getConnection();
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

    public static List<Integer> batchUpdate(List<Entity> entityList)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        Boolean autoCommit = null;
        try
        {
            conn = dataSource0.getConnection();
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            List<Integer> result = setBatchValues(conn, entityList);
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

    private static void doResolve(NewExpression<?> newExpression, List<BaseMapping> baseMappings) throws NoSuchMethodException
    {
        Map<String, Method> map = Cache.getMethodNameToMethodMapping(newExpression.getTarget());
        for (IExpression expression : newExpression.getExpressions())
        {
            doResolveStart(expression, map, baseMappings);
        }
    }

    private static void doResolveStart(IExpression expression, Map<String, Method> methodMap, List<BaseMapping> baseMappings) throws NoSuchMethodException
    {
        if (expression instanceof MethodCallExpression)
        {
            MethodCallExpression methodCall = (MethodCallExpression) expression;
            Method method = methodMap.get(methodCall.getSelectedMethod());
            if (methodCall.getParams().get(0) instanceof NewExpression<?>)
            {
                NewExpression<?> newExpression = (NewExpression<?>) methodCall.getParams().get(0);
                if (!newExpression.getExpressions().isEmpty())
                {
                    NewClassMapping newClassMapping = new NewClassMapping(newExpression.getTarget(), method);
                    List<BaseMapping> list = new ArrayList<>();
                    doResolve(newExpression, list);
                    newClassMapping.getMappings().addAll(list);
                    baseMappings.add(newClassMapping);
                }
                else
                {
                    RefTableMapping refTableMapping = new RefTableMapping(newExpression.getTarget(), method);
                    baseMappings.add(refTableMapping);
                }
            }
            else
            {
                baseMappings.add(new SetterMapping(method));
            }
        }
    }

    public static boolean transactionCud(List<Entity> entityList, Integer transactionIsolation)
    {
        Connection conn = null;
        Boolean autoCommit = null;
        try
        {
            conn = dataSource0.getConnection();
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            if (transactionIsolation != null)
            {
                conn.setTransactionIsolation(transactionIsolation);
            }
            for (Entity entity : entityList)
            {
                PreparedStatement ps = conn.prepareStatement(entity.toString());
                setValues(ps, entity.values);
                int count = ps.executeUpdate();
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