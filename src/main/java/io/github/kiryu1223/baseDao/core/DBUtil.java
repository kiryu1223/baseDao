package io.github.kiryu1223.baseDao.core;

import io.github.kiryu1223.baseDao.core.Mapping.*;
import io.github.kiryu1223.expressionTree.delegate.Func1;
import io.github.kiryu1223.expressionTree.delegate.Func2;
import io.github.kiryu1223.expressionTree.delegate.Func3;
import io.github.kiryu1223.expressionTree.delegate.Func4;
import io.github.kiryu1223.expressionTree.expressions.*;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.sql.*;
import java.util.*;

public class DBUtil
{
    private static DataSource dataSource0;

    private DBUtil()
    {
    }

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

    private static Pairs getPairs(Object[] objects, Entity entity) throws InstantiationException, IllegalAccessException, InvocationTargetException, SQLException, NoSuchMethodException
    {
        List<Pair> pairs = new ArrayList<>();
        for (Class<?> clazz : entity.classes)
        {
            for (int i = 0; i < objects.length; i++)
            {
                Object obj = objects[i];
                if (obj.getClass().equals(clazz))
                {
                    pairs.add(
                            new ClassPair(obj, clazz,i,objects, Cache.getTypeFields(clazz))
                    );
                    break;
                }
            }
        }
        for (Method method : entity.methods)
        {
            Field field = Cache.getterToField(method);
            Class<?> declaringClass = method.getDeclaringClass();
            for (Object obj : objects)
            {
                if (declaringClass.equals(obj.getClass()))
                {
                    pairs.add(new FieldPair(field, obj));
                    break;
                }
            }
        }

        return new Pairs(pairs);
    }

    private static int getLength(ResultSet rs) throws SQLException
    {
        rs.last();
        int length = rs.getRow();
        rs.beforeFirst();
        return length;
    }

    public static <T1, R> List<R> startQueryToList(Entity entity, ExprTree<Func1<T1, R>> exprTree)
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
            ResultSetMetaData md = rs.getMetaData();
            Func1<T1, R> delegate = exprTree.getDelegate();
            LambdaExpression lambda = exprTree.getTree();
            List<ParameterExpression> parameters = lambda.getParameters();
            Object[] objects = new Object[parameters.size()];
            for (int i = 0; i < parameters.size(); i++)
            {
                ParameterExpression parameter = parameters.get(i);
                objects[i] = parameter.getType().getConstructor().newInstance();
            }
            Pairs pairs = getPairs(objects, entity);
            List<R> result = new ArrayList<>();
            while (rs.next())
            {
                pairs.set(rs);
                R r = delegate.invoke((T1) objects[0]);
                pairs.reload();
                result.add(r);
            }
            return result;
        }
        catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException |
               SQLException e)
        {
            System.out.println(entity);
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

    public static <T1, T2, R> List<R> startQueryToList2(Entity entity, ExprTree<Func2<T1, T2, R>> exprTree)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = dataSource0.getConnection();
            ps = conn.prepareStatement(entity.sql.toString());
            setValues(ps, entity.values);
            long start = System.currentTimeMillis();

            rs = ps.executeQuery();
            System.out.println("从数据库返回耗时:" + (System.currentTimeMillis() - start));
            ResultSetMetaData md = rs.getMetaData();
            Func2<T1, T2, R> delegate = exprTree.getDelegate();
            LambdaExpression lambda = exprTree.getTree();
            List<ParameterExpression> parameters = lambda.getParameters();
            Object[] objects = new Object[parameters.size()];
            for (int i = 0; i < parameters.size(); i++)
            {
                ParameterExpression parameter = parameters.get(i);
                objects[i] = parameter.getType().getConstructor().newInstance();
            }
            Pairs pairs = getPairs(objects, entity);
            List<R> result = new ArrayList<>();
            long start2 = System.currentTimeMillis();

            while (rs.next())
            {
                pairs.set(rs);
                int i = 0;
                R r = delegate.invoke((T1) objects[i++], (T2) objects[i++]);
                pairs.reload();
                result.add(r);
            }
            System.out.println("拼装对象耗时:" + (System.currentTimeMillis() - start2));
            return result;
        }
        catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException |
               SQLException e)
        {
            System.out.println(entity);
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

    public static <T1, T2, T3, R> List<R> startQueryToList3(Entity entity, ExprTree<Func3<T1, T2, T3, R>> exprTree)
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
            ResultSetMetaData md = rs.getMetaData();
            Func3<T1, T2, T3, R> delegate = exprTree.getDelegate();
            LambdaExpression lambda = exprTree.getTree();
            List<ParameterExpression> parameters = lambda.getParameters();
            Object[] objects = new Object[parameters.size()];
            for (int i = 0; i < parameters.size(); i++)
            {
                ParameterExpression parameter = parameters.get(i);
                objects[i] = parameter.getType().getConstructor().newInstance();
            }
            Pairs pairs = getPairs(objects, entity);
            List<R> result = new ArrayList<>();
            while (rs.next())
            {
                pairs.set(rs);
                int i = 0;
                R r = delegate.invoke((T1) objects[i++], (T2) objects[i++], (T3) objects[i++]);
                pairs.reload();
                result.add(r);
            }
            return result;
        }
        catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException |
               SQLException e)
        {
            System.out.println(entity);
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

    public static <T1, T2, T3, T4, R> List<R> startQueryToList4(Entity entity, ExprTree<Func4<T1, T2, T3, T4, R>> exprTree)
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
            ResultSetMetaData md = rs.getMetaData();
            Func4<T1, T2, T3, T4, R> delegate = exprTree.getDelegate();
            LambdaExpression lambda = exprTree.getTree();
            List<ParameterExpression> parameters = lambda.getParameters();
            Object[] objects = new Object[parameters.size()];
            for (int i = 0; i < parameters.size(); i++)
            {
                ParameterExpression parameter = parameters.get(i);
                objects[i] = parameter.getType().getConstructor().newInstance();
            }
            Pairs pairs = getPairs(objects, entity);
            List<R> result = new ArrayList<>();
            while (rs.next())
            {
                pairs.set(rs);
                int i = 0;
                R r = delegate.invoke((T1) objects[i++], (T2) objects[i++], (T3) objects[i++], (T4) objects[i++]);
                pairs.reload();
                result.add(r);
            }
            return result;
        }
        catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException |
               SQLException e)
        {
            System.out.println(entity);
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

    public static <T, Key, R> Map<Key, R> startQueryToMap(Entity entity, ExprTree<Func1<T, R>> exprTree, Func1<R, Key> getKey)
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
            Func1<T, R> delegate = exprTree.getDelegate();
            LambdaExpression lambda = exprTree.getTree();
            List<ParameterExpression> parameters = lambda.getParameters();
            Object[] objects = new Object[parameters.size()];
            for (int i = 0; i < parameters.size(); i++)
            {
                ParameterExpression parameter = parameters.get(i);
                objects[i] = parameter.getType().getConstructor().newInstance();
            }
            Pairs pairs = getPairs(objects, entity);
            List<R> result = new ArrayList<>();
            while (rs.next())
            {
                pairs.set(rs);
                R r = delegate.invoke((T) objects[0]);
                pairs.reload();
                result.add(r);
            }
            Map<Key, R> resultMap = new HashMap<>();
            for (R r : result)
            {
                resultMap.put(getKey.invoke(r), r);
            }
            return resultMap;
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

    public static <T1, T2, Key, R> Map<Key, R> startQueryToMap2(Entity entity, ExprTree<Func2<T1, T2, R>> exprTree, Func1<R, Key> getKey)
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
            ResultSetMetaData md = rs.getMetaData();
            Func2<T1, T2, R> delegate = exprTree.getDelegate();
            LambdaExpression lambda = exprTree.getTree();
            List<ParameterExpression> parameters = lambda.getParameters();
            Object[] objects = new Object[parameters.size()];
            for (int i = 0; i < parameters.size(); i++)
            {
                ParameterExpression parameter = parameters.get(i);
                objects[i] = parameter.getType().getConstructor().newInstance();
            }
            Pairs pairs = getPairs(objects, entity);
            List<R> result = new ArrayList<>();
            while (rs.next())
            {
                pairs.set(rs);
                int i = 0;
                R r = delegate.invoke((T1) objects[i++], (T2) objects[i++]);
                pairs.reload();
                result.add(r);
            }
            Map<Key, R> resultMap = new HashMap<>();
            for (R r : result)
            {
                resultMap.put(getKey.invoke(r), r);
            }
            return resultMap;
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

    public static <T1, T2, T3, Key, R> Map<Key, R> startQueryToMap3(Entity entity, ExprTree<Func3<T1, T2, T3, R>> exprTree, Func1<R, Key> getKey)
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
            ResultSetMetaData md = rs.getMetaData();
            Func3<T1, T2, T3, R> delegate = exprTree.getDelegate();
            LambdaExpression lambda = exprTree.getTree();
            List<ParameterExpression> parameters = lambda.getParameters();
            Object[] objects = new Object[parameters.size()];
            for (int i = 0; i < parameters.size(); i++)
            {
                ParameterExpression parameter = parameters.get(i);
                objects[i] = parameter.getType().getConstructor().newInstance();
            }
            Pairs pairs = getPairs(objects, entity);
            List<R> result = new ArrayList<>();
            while (rs.next())
            {
                pairs.set(rs);
                int i = 0;
                R r = delegate.invoke((T1) objects[i++], (T2) objects[i++], (T3) objects[i++]);
                pairs.reload();
                result.add(r);
            }
            Map<Key, R> resultMap = new HashMap<>();
            for (R r : result)
            {
                resultMap.put(getKey.invoke(r), r);
            }
            return resultMap;
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

    public static <T1, T2, T3, T4, Key, R> Map<Key, R> startQueryToMap4(Entity entity, ExprTree<Func4<T1, T2, T3, T4, R>> exprTree, Func1<R, Key> getKey)
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
            ResultSetMetaData md = rs.getMetaData();
            Func4<T1, T2, T3, T4, R> delegate = exprTree.getDelegate();
            LambdaExpression lambda = exprTree.getTree();
            List<ParameterExpression> parameters = lambda.getParameters();
            Object[] objects = new Object[parameters.size()];
            for (int i = 0; i < parameters.size(); i++)
            {
                ParameterExpression parameter = parameters.get(i);
                objects[i] = parameter.getType().getConstructor().newInstance();
            }
            Pairs pairs = getPairs(objects, entity);
            List<R> result = new ArrayList<>();
            while (rs.next())
            {
                pairs.set(rs);
                int i = 0;
                R r = delegate.invoke((T1) objects[i++], (T2) objects[i++], (T3) objects[i++], (T4) objects[i++]);
                pairs.reload();
                result.add(r);
            }
            Map<Key, R> resultMap = new HashMap<>();
            for (R r : result)
            {
                resultMap.put(getKey.invoke(r), r);
            }
            return resultMap;
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
            conn = dataSource0.getConnection();
            ps = conn.prepareStatement(entity.toSql());
            setValues(ps, entity.values);
            return ps.executeUpdate();
        }
        catch (SQLException e)
        {
            System.out.println(entity);
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

//    private static Arg resolveToArg(LambdaExpression lambdaExpression)
//    {
//        Expression body = lambdaExpression.getBody();
//        List<ParameterExpression> parameters = lambdaExpression.getParameters();
//        if (body instanceof MethodCallExpression)
//        {
//            MethodCallExpression methodCall = (MethodCallExpression) body;
//            if (methodCall.getExpr() instanceof ParameterExpression
//                    && parameters.contains((ParameterExpression) methodCall.getExpr()))
//            {
//                Class<?> returnType = methodCall.getMethod().getReturnType();
//                return new SqlFieldArg(returnType);
//            }
//        }
//        else if (body instanceof NewExpression)
//        {
//            NewExpression newExpression = (NewExpression) body;
//            BlockExpression classBody = newExpression.getClassBody();
//            Class<?> type = newExpression.getType();
//            NewClassArg newClassArg = new NewClassArg(type);
//            if (classBody == null)
//            {
//                return newClassArg;
//            }
//            classBody.accept(new GenericsVisitor<NewClassArg>()
//            {
//                @Override
//                public void visit(BlockExpression blockExpression, NewClassArg nc)
//                {
//                    for (Expression expression : blockExpression.getExpressions())
//                    {
//                        visit(expression, nc);
//                    }
//                }
//
//                @Override
//                public void visit(NewExpression newExpression, NewClassArg nc)
//                {
//                    if (newExpression.getClassBody() == null) return;
//                    BlockExpression classBody1 = newExpression.getClassBody();
//                    visit(classBody1, nc);
//                }
//
//                @Override
//                public void visit(MethodCallExpression methodCall, NewClassArg nc)
//                {
//                    Method method = methodCall.getMethod();
//                    if (methodCall.getExpr() == null)
//                    {
//                        if (method.getDeclaringClass().equals(type)
//                                && !methodCall.getArgs().isEmpty())
//                        {
//                            List<Arg> args = new ArrayList<>(methodCall.getArgs().size());
//                            for (Expression arg : methodCall.getArgs())
//                            {
//                                if (arg instanceof ParameterExpression
//                                        && parameters.contains((ParameterExpression) arg))
//                                {
//                                    ParameterExpression parameter = (ParameterExpression) arg;
//                                    args.add(new SqlTableArg(parameter.getType()));
//                                }
//                                else if (arg instanceof MethodCallExpression
//                                        && ((MethodCallExpression) arg).getArgs().isEmpty()
//                                        && ((MethodCallExpression) arg).getExpr() instanceof ParameterExpression
//                                        && parameters.contains((ParameterExpression) ((MethodCallExpression) arg).getExpr()))
//                                {
//                                    MethodCallExpression methodCallExpression = (MethodCallExpression) arg;
//                                    Class<?> returnType = methodCallExpression.getMethod().getReturnType();
//                                    args.add(new SqlFieldArg(returnType));
//                                }
//                                else if (arg instanceof NewExpression)
//                                {
//                                    NewExpression expression = (NewExpression) arg;
//                                    NewClassArg newClassArg1 = new NewClassArg(expression.getType());
//                                    visit(arg, newClassArg1);
//                                    args.add(newClassArg1);
//                                }
//                            }
//                            nc.getPairs().add(new Pair(method, args));
//                        }
//                    }
//                }
//
//            }, newClassArg);
//            return newClassArg;
//        }
//        throw new RuntimeException("wtf " + lambdaExpression);
//    }

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
                PreparedStatement ps = conn.prepareStatement(entity.toSql());
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