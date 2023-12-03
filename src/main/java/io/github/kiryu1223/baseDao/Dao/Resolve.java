package io.github.kiryu1223.baseDao.Dao;

import io.github.kiryu1223.baseDao.Dao.Base.*;
import io.github.kiryu1223.baseDao.Dao.Inserter.Insert;
import io.github.kiryu1223.baseDao.Dao.Statement.Statement;
import io.github.kiryu1223.baseDao.Dao.Updater.Update;
import io.github.kiryu1223.baseDao.DataBase.DataBase;
import io.github.kiryu1223.baseDao.Dao.Deleter.Delete;
import io.github.kiryu1223.expressionTree.expressionV2.*;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Resolve
{
    private static DataBase db;

    public static void setDb(DataBase db)
    {
        Resolve.db = db;
    }

    public static Entity query(boolean isDistinct, List<Base> bases, NewExpression<?> newExpression, List<Class<?>> queryClasses, List<?> queryTarget, List<Class<?>> joins)
    {
        Entity entity = new Entity();
        select(entity, isDistinct, newExpression, queryClasses, queryTarget, joins);
        for (Base base : bases)
        {
            if (base instanceof Where)
            {
                where(entity, ((Where) base).getExpression(), queryTarget);
            }
            else if (base instanceof Take)
            {
                take(entity, (Take) base);
            }
            else if (base instanceof Skip)
            {
                skip(entity, (Skip) base);
            }
            else if (base instanceof OrderBy)
            {
                orderBy(entity, (OrderBy) base, queryTarget);
            }
            else if (base instanceof Join)
            {
                join(entity, (Join) base, queryClasses);
            }
            else if (base instanceof On)
            {
                on(entity, ((On) base).getExpression(), queryTarget);
            }
        }
        return entity;
    }

    public static Entity cud(Statement<?> statement)
    {
        if (statement instanceof Insert)
        {
            return insert(statement);
        }
        else if (statement instanceof Delete)
        {
            return delete(statement);
        }
        else if (statement instanceof Update)
        {
            return update(statement);
        }
        return new Entity();
    }

    public static Entity insert(Statement<?> statement)
    {
        Entity entity = new Entity();
        List<?> targets = statement.getQueryTargets();
        entity.append("insert into ").append(Cache.getTableName(targets.get(0).getClass())).append(" ");
        for (Base base : statement.getBases())
        {
            if (base instanceof SetData)
            {
                setInsert(entity, (SetData) base, targets);
            }
        }
        return entity;
    }

    public static Entity delete(Statement<?> statement)
    {
        Entity entity = new Entity();
        entity.append("delete").append(" ").append(indexMapping(0))
                .append(".*").append(" ").append("from").append(" ")
                .append(Cache.getTableName(statement.getQueryClasses().get(0))).append(" ")
                .append("as").append(" ").append(indexMapping(0)).append(" ");
        for (Base base : statement.getBases())
        {
            if (base instanceof Where)
            {
                where(entity, ((Where) base).getExpression(), statement.getQueryTargets());
            }
        }
        return entity;
    }

    public static Entity update(Statement<?> statement)
    {
        Entity entity = new Entity();
        List<?> targets = statement.getQueryTargets();
        entity.append("update ").append(Cache.getTableName(targets.get(0).getClass())).append(" ")
                .append("as").append(" ").append(indexMapping(0)).append(" ");
        for (Base base : statement.getBases())
        {
            if (base instanceof SetData)
            {
                setUpdate(entity, (SetData) base, statement.getQueryTargets());
            }
            else if (base instanceof Where)
            {
                where(entity, ((Where) base).getExpression(), statement.getQueryTargets());
            }
        }
        return entity;
    }

    public static <T> List<Entity> batchSave(List<T> ts)
    {
        List<Entity> entityList = new ArrayList<>(ts.size());
        Class<?> type = ts.get(0).getClass();
        String tableName = Cache.getTableName(type);
        Map<String, String> map = Cache.getJavaFieldNameToDbFieldNameMappingMap(type);
        List<Field> fields = Cache.getTypeFields(type);
        for (T t : ts)
        {
            Entity entity = new Entity();
            entity.append("insert into ").append(tableName).append(" ")
                    .append("set").append(" ");
            for (Field field : fields)
            {
                if (field.isAnnotationPresent(Id.class))
                {
                    switch (field.getAnnotation(GeneratedValue.class).strategy())
                    {
                        case IDENTITY:
                            continue;
                        case TABLE:
                        case SEQUENCE:
                        case AUTO:
                    }
                }
                try
                {
                    entity.append(map.get(field.getName())).append(" = ");
                    Object o = field.get(type);
                    if (o != null)
                    {
                        entity.pushValue(o);
                    }
                    else
                    {
                        entity.append("null");
                    }
                    entity.append(",");
                }
                catch (IllegalAccessException e)
                {
                    throw new RuntimeException(e);
                }
            }
            entity.deleteLast();
            entityList.add(entity);
        }
        return entityList;
    }

    private static void setInsert(Entity entity, SetData setData, List<?> targets)
    {
        entity.append("set ");
        Class<?> target = targets.get(0).getClass();
        Map<String, String> map = Cache.getJavaFieldNameToDbFieldNameMappingMap(target);
        IExpression expression = setData.getExpression();
        if (expression instanceof BlockExpression)
        {
            BlockExpression blockExpression = (BlockExpression) expression;
            for (IExpression iExpression : blockExpression.getExpressions())
            {
                if (iExpression instanceof MethodCallExpression)
                {
                    MethodCallExpression methodCallExpression = (MethodCallExpression) iExpression;
                    String fieldName = GetSetHelper.getterToFieldName(methodCallExpression.getSelectedMethod(), target);
                    entity.append(map.get(fieldName)).append(" = ");
                    IExpression val = methodCallExpression.getParams().get(0);
                    doResolve(entity, val, targets);
                }
                entity.append(",");
            }
            entity.deleteLast().append(" ");
        }
        else if (expression instanceof MethodCallExpression)
        {
            MethodCallExpression methodCallExpression = (MethodCallExpression) expression;
            String fieldName = GetSetHelper.getterToFieldName(methodCallExpression.getSelectedMethod(), target);
            entity.append(map.get(fieldName)).append(" = ");
            IExpression val = methodCallExpression.getParams().get(0);
            doResolve(entity, val, targets);
        }
        else
        {
            throw new RuntimeException(expression.toString());
        }
    }

    private static void setUpdate(Entity entity, SetData setData, List<?> targets)
    {
        entity.append("set ");
        Class<?> target = targets.get(0).getClass();
        Map<String, String> map = Cache.getJavaFieldNameToDbFieldNameMappingMap(target);
        IExpression expression = setData.getExpression();
        if (expression instanceof BlockExpression)
        {
            BlockExpression blockExpression = (BlockExpression) expression;
            for (IExpression iExpression : blockExpression.getExpressions())
            {
                doResolve(entity, iExpression, targets);
                entity.append(",");
            }
            entity.deleteLast().append(" ");
        }
        else if (expression instanceof MethodCallExpression)
        {
            doResolve(entity, expression, targets);
        }
        else if (expression instanceof AssignExpression)
        {
            doResolve(entity, expression, targets);
        }
        else
        {
            throw new RuntimeException(expression.toString());
        }
    }

    private static void join(Entity entity, Join join, List<Class<?>> queryClass)
    {
        switch (join.getJoinType())
        {
            case Inner:
                entity.append("inner join ");
                break;
            case Left:
                entity.append("left join ");
                break;
            case Right:
                entity.append("right join ");
                break;
            case Full:
                entity.append("full join ");
                break;
        }
        entity.append(Cache.getTableName(join.getJoinClass()))
                .append(" ").append("as").append(" ")
                .append(indexMapping(queryClass.indexOf(join.getJoinClass()))).append(" ");
    }

    private static void select(Entity entity, boolean isDistinct, NewExpression<?> newExpression, List<Class<?>> queryClass, List<?> queryTarget, List<Class<?>> joinClass)
    {
        entity.append("select").append(" ");
        if (isDistinct) entity.append("distinct").append(" ");
        if (!newExpression.getExpressions().isEmpty())
        {
            doResolve(entity, newExpression, queryTarget);
        }
        else
        {
            entity.append(indexMapping(queryClass.indexOf(newExpression.getTarget()))).append(".").append("*");
        }
        entity.append(" ");
        entity.append("from").append(" ");
        for (Class<?> c : queryClass)
        {
            if (joinClass == null || !joinClass.contains(c))
            {
                entity.append(Cache.getTableName(c)).append(" ").append("as").append(" ")
                        .append(indexMapping(queryClass.indexOf(c))).append(",");
            }
        }
        entity.deleteLast().append(" ");
    }

    private static void doResolve(Entity entity, IExpression expression, List<?> queryTarget)
    {
        if (expression instanceof BinaryExpression)
        {
            doResolveBinaryExpression((BinaryExpression) expression, entity, queryTarget);
        }
        else if (expression instanceof UnaryExpression)
        {
            doResolveUnaryExpression((UnaryExpression) expression, entity, queryTarget);
        }
        else if (expression instanceof ValueExpression)
        {
            doResolveValueExpression((ValueExpression<?>) expression, entity);
        }
        else if (expression instanceof ParensExpression)
        {
            doResolveParensExpression((ParensExpression) expression, entity, queryTarget);
        }
        else if (expression instanceof ReferenceExpression)
        {
            doResolveReferenceExpression((ReferenceExpression) expression, entity, queryTarget);
        }
        else if (expression instanceof FieldSelectExpression)
        {
            doResolveFieldSelectExpression((FieldSelectExpression) expression, entity, queryTarget);
        }
        else if (expression instanceof MethodCallExpression)
        {
            doResolveMethodCallExpression((MethodCallExpression) expression, entity, queryTarget);
        }
        else if (expression instanceof NewExpression<?>)
        {
            doResolveNewExpression((NewExpression<?>) expression, entity, queryTarget);
        }
        else if (expression instanceof AssignExpression)
        {
            doResolveAssignExpression((AssignExpression) expression,entity, queryTarget);
        }
    }

    private static void doResolveAssignExpression(AssignExpression assign, Entity entity, List<?> queryTarget)
    {
        doResolve(entity,assign.getLeft(),queryTarget);
        entity.append(" = ");
        doResolve(entity,assign.getRight(),queryTarget);
        entity.blank();
    }

    private static void doResolveNewExpression(NewExpression<?> newExpression, Entity entity, List<?> queryTarget)
    {
        for (IExpression expression : newExpression.getExpressions())
        {
            if (expression instanceof MethodCallExpression)
            {
                MethodCallExpression methodCallExpression = (MethodCallExpression) expression;
                for (IExpression param : methodCallExpression.getParams())
                {
                    doResolve(entity,param,queryTarget);
                }
                entity.append(",");
            }
        }
        entity.deleteLast();
    }

    private static void doResolveReferenceExpression(ReferenceExpression reference, Entity entity, List<?> queryTarget)
    {
        Object ref = reference.getReference();
        if (queryTarget.contains(ref))
        {
            int index = queryTarget.indexOf(ref);
            entity.append(indexMapping(index));
        }
        else
        {
            entity.append("?");
            entity.values.add(ref);
        }
    }

    private static void doResolveFieldSelectExpression(FieldSelectExpression fieldSelect, Entity entity, List<?> queryTarget)
    {
        Object reference = fieldSelect.getSource().getReference();
        if (queryTarget.contains(reference))
        {
            doResolve(entity, fieldSelect.getSelector(), queryTarget);
            entity.append(".");
            Map<String, String> map = Cache.getJavaFieldNameToDbFieldNameMappingMap(reference.getClass());
            entity.append(map.get(fieldSelect.getSelectedField()));
        }
        else
        {
            entity.append("?").append(" ");
            Object val = fieldSelect.getValue();
            entity.values.add(val);
        }
    }

    private static void doResolveMethodCallExpression(MethodCallExpression methodCall, Entity entity, List<?> queryTarget)
    {
        Object reference = methodCall.getSource().getReference();
        if (queryTarget.contains(reference))
        {
            switch (methodCall.getSelectedMethod())
            {
                case "equals":
                    doResolve(entity, methodCall.getSelector(), queryTarget);
                    entity.append(" = ");
                    doResolve(entity, methodCall.getParams().get(0), queryTarget);
                    break;
                case "contains":
                    doResolve(entity, methodCall.getSelector(), queryTarget);
                    entity.append(" like ");
                    doResolve(entity, methodCall.getParams().get(0), queryTarget);
                    int index = entity.values.size() - 1;
                    entity.values.set(index, "%" + entity.values.get(index) + "%");
                    break;
                default:
                    doResolve(entity, methodCall.getSelector(), queryTarget);
                    Map<String, String> map = Cache.getJavaFieldNameToDbFieldNameMappingMap(reference.getClass());
                    entity.append(".").append(map.get(GetSetHelper.getterToFieldName(
                            methodCall.getSelectedMethod(),
                            reference.getClass())
                    ));
                    if (!methodCall.getParams().isEmpty())
                    {
                        entity.append(" = ");
                        doResolve(entity, methodCall.getParams().get(0), queryTarget);
                    }
                    break;
            }
        }
        else
        {
            entity.append("?").append(" ");
            Object val = methodCall.getValue();
            entity.values.add(val);
        }
    }

    private static void doResolveUnaryExpression(UnaryExpression unary, Entity entity, List<?> queryTarget)
    {
        if (unary.getOperator() == Operator.NOT)
        {
            if (unary.getExpression() instanceof ParensExpression)
            {
                entity.append("!");
                doResolve(entity, unary.getExpression(), queryTarget);
            }
            else
            {
                entity.append("!(");
                doResolve(entity, unary.getExpression(), queryTarget);
                entity.append(")").append(" ");
            }
        }
    }

    private static void doResolveValueExpression(ValueExpression<?> value, Entity entity)
    {
        if (value.getValue() != null)
        {
            entity.append("?");
            entity.values.add(value.getValue());
        }
        else
        {
            entity.append("null");
        }
    }

    private static void doResolveBinaryExpression(BinaryExpression binary, Entity entity, List<?> queryTarget)
    {
        doResolve(entity, binary.getLeft(), queryTarget);
        entity.blank();
        if (binary.getRight() instanceof ValueExpression && ((ValueExpression<?>) binary.getRight()).getValue() == null)
        {
            switch (binary.getOperator())
            {
                case EQ:
                    entity.append("is");
                    break;
                case NE:
                    entity.append("is not");
                    break;
            }
        }
        else
        {
            entity.append(binary.getOperator().toString());
        }
        entity.blank();
        doResolve(entity, binary.getRight(), queryTarget);
        entity.blank();
    }

    private static void doResolveParensExpression(ParensExpression parens, Entity entity, List<?> queryTarget)
    {
        entity.append("(");
        doResolve(entity, parens.getExpression(), queryTarget);
        entity.deleteLast().append(")").append(" ");
    }

    private static void where(Entity entity, IExpression expression, List<?> queryTarget)
    {
        entity.append("where").append(" ");
        doResolve(entity, expression, queryTarget);
    }

    private static void on(Entity entity, IExpression expression, List<?> queryTarget)
    {
        entity.append("on").append(" ");
        doResolve(entity, expression, queryTarget);
    }

    public static Entity save(Save<?> save)
    {
        Entity entity = new Entity();
        Object target = save.getTarget();
        entity.append("insert into ").append(Cache.getTableName(target.getClass()))
                .append(" ").append("set").append(" ");
        Map<String, String> map = Cache.getJavaFieldNameToDbFieldNameMappingMap(target.getClass());
        List<java.lang.reflect.Field> fields = Cache.getTypeFields(target.getClass());
        int count = 0;
        for (java.lang.reflect.Field field : fields)
        {
            if (field.isAnnotationPresent(Id.class))
            {
                switch (field.getAnnotation(GeneratedValue.class).strategy())
                {
                    case IDENTITY:
                        continue;
                    case TABLE:
                    case SEQUENCE:
                    case AUTO:
                }
            }
            try
            {
                Object o = field.get(target);
                if (o != null)
                {
                    entity.append(map.get(field.getName())).append(" = ").append("?").append(",");
                    entity.values.add(o);
                    count++;
                }
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }
        if (count > 0) entity.deleteLast();
        return entity;
    }

//    private static void set(Entity entity, Set<?> set)
//    {
//        Object target = set.getTarget();
//        entity.append("update ").append(Cache.getTableName(target.getClass())).append(" ")
//                .append("as").append(" ").append(indexMapping(0)).append(" ").append("set").append(" ");
//        Map<String, String> map = Cache.getJavaFieldNameToDbFieldNameMappingMap(target.getClass());
//        boolean flag = false;
//        for (java.lang.reflect.Field field : target.getClass().getDeclaredFields())
//        {
//            field.setAccessible(true);
//            try
//            {
//                Object o = field.get(target);
//                if (o != null)
//                {
//                    entity.append(indexMapping(0)).append(".")
//                            .append(map.get(field.getName()))
//                            .append("=").append("?").append(",");
//                    entity.values.add(o);
//                    flag = true;
//                }
//            }
//            catch (IllegalAccessException e)
//            {
//                throw new RuntimeException(e);
//            }
//        }
//        if (flag) entity.deleteLast();
//        entity.append(" ");
//    }

    private static void take(Entity entity, Take take)
    {
        entity.append("limit").append(" ");
        entity.append(take.getCount()).append(" ");
    }

    private static void skip(Entity entity, Skip skip)
    {
        entity.append("offset").append(" ");
        entity.append(skip.getCount()).append(" ");
    }

    private static void orderBy(Entity entity, OrderBy orderBy, List<?> queryTarget)
    {
        IExpression ref = orderBy.getExpression();
        entity.append("order by ");
        doResolve(entity, ref, queryTarget);
        entity.blank();
    }

    private static String indexMapping(int index)
    {
        return Mapping.get(index);
    }

    private static final Map<Integer, String> Mapping = new HashMap<>();

    static
    {
        Mapping.put(0, "a");
        Mapping.put(1, "b");
        Mapping.put(2, "c");
        Mapping.put(3, "d");
        Mapping.put(4, "e");
        Mapping.put(5, "f");
        Mapping.put(6, "g");
        Mapping.put(7, "h");
        Mapping.put(8, "i");
        Mapping.put(9, "j");
        Mapping.put(10, "k");
        Mapping.put(11, "l");
        Mapping.put(12, "m");
        Mapping.put(13, "n");
        Mapping.put(14, "o");
        Mapping.put(15, "p");
        Mapping.put(16, "q");
        Mapping.put(17, "r");
        Mapping.put(18, "s");
        Mapping.put(19, "t");
        Mapping.put(20, "u");
        Mapping.put(21, "v");
        Mapping.put(22, "w");
        Mapping.put(23, "x");
        Mapping.put(24, "y");
        Mapping.put(25, "z");
    }
}
