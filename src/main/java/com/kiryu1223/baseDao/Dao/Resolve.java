package com.kiryu1223.baseDao.Dao;

import com.kiryu1223.baseDao.Dao.Base.*;
import com.kiryu1223.baseDao.DataBase.DataBase;
import com.kiryu1223.baseDao.ExpressionV2.*;
import com.kiryu1223.baseDao.Dao.Deleter.Delete;
import com.kiryu1223.baseDao.Dao.Inserter.Insert;
import com.kiryu1223.baseDao.Dao.Inserter.InsertOne;
import com.kiryu1223.baseDao.Dao.Updater.Set;
import com.kiryu1223.baseDao.Dao.Updater.Update;

import javax.persistence.Id;
import java.util.*;

public class Resolve
{
    private static DataBase db;

    public static void setDb(DataBase db)
    {
        Resolve.db = db;
    }

    public static Entity query(boolean isDistinct, List<Base> bases, NewExpression<?> newExpression, List<Class<?>> queryClasses, List<Class<?>> joins)
    {
        var entity = new Entity();
        select(entity, isDistinct, newExpression, queryClasses, joins);
        for (var base : bases)
        {
            if (base instanceof Where)
            {
                where(entity, ((Where) base).getOperatorExpression(), queryClasses);
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
                orderBy(entity, (OrderBy) base, queryClasses);
            }
            else if (base instanceof Join)
            {
                join(entity, (Join) base, queryClasses);
            }
            else if (base instanceof On)
            {
                on(entity, ((On) base).getOperatorExpression(), queryClasses);
            }
        }
        return entity;
    }

    public static Entity insert(Insert<?> insert)
    {
        var entity = new Entity();
        for (var base : insert.getBases())
        {
            if (base instanceof InsertOne)
            {
                insertOne(entity, (InsertOne<?>) base);
            }
        }
        return entity;
    }

    public static Entity delete(Delete<?> delete)
    {
        var entity = new Entity();
        entity.sql.append("delete").append(" ").append(indexMapping(0))
                .append(".*").append(" ").append("from").append(" ")
                .append(Cache.getTableName(delete.getC1())).append(" ")
                .append("as").append(" ").append(indexMapping(0)).append(" ");
        for (var base : delete.getBases())
        {
            if (base instanceof Where)
            {
                where(entity, ((Where) base).getOperatorExpression(), delete.getQueryClasses());
            }
        }
        return entity;
    }

    public static Entity update(Update<?> update)
    {
        var entity = new Entity();
        for (var base : update.getBases())
        {
            if (base instanceof Set)
            {
                set(entity, (Set<?>) base);
            }
            else if (base instanceof Where)
            {
                where(entity, ((Where) base).getOperatorExpression(), update.getQueryClasses());
            }
        }
        return entity;
    }

    public static Entity save(InsertOne<?> save)
    {
        var entity = new Entity();
        insertOne(entity, save);
        return entity;
    }

    public static <T> BatchEntity batchSave(List<T> ts)
    {
        var batchEntity = new BatchEntity();
        var type = ts.get(0).getClass();
        batchEntity.sql.append("insert into ").append(Cache.getTableName(type)).append("(");
        var map = Cache.getJavaFieldNameToDbFieldNameMappingMap(type);
        var fields = Cache.getTypeFields(type);
        int count = 0;
        for (var field : fields)
        {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Id.class))
            {
                continue;
            }
            try
            {
                var o = field.get(type);
                if (o != null)
                {
                    batchEntity.sql.append(map.get(field.getName())).append(",");
                    count++;
                }
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }
        if (count > 0) batchEntity.sql.deleteCharAt(batchEntity.sql.length() - 1);
        batchEntity.sql.append(")").append(" ").append("values").append("(");
        for (int i = 0; i < count; i++)
        {
            batchEntity.sql.append("?").append(",");
        }
        if (count > 0) batchEntity.sql.deleteCharAt(batchEntity.sql.length() - 1);
        batchEntity.sql.append(")").append(" ");
        for (T t : ts)
        {
            List<Object> values = new ArrayList<>();
            for (var field : t.getClass().getDeclaredFields())
            {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Id.class))
                {
                    continue;
                }
                try
                {
                    var o = field.get(type);
                    if (o != null)
                    {
                        values.add(o);
                        count++;
                    }
                }
                catch (IllegalAccessException e)
                {
                    throw new RuntimeException(e);
                }
            }
            batchEntity.values.add(values);
        }
        return batchEntity;
    }

    private static void join(Entity entity, Join join, List<Class<?>> queryClass)
    {
        switch (join.getJoinType())
        {
            case Inner:
                entity.sql.append("inner join ");
                break;
            case Left:
                entity.sql.append("left join ");
                break;
            case Right:
                entity.sql.append("right join ");
                break;
            case Full:
                entity.sql.append("full join ");
                break;
        }
        entity.sql.append(Cache.getTableName(join.getJoinClass()))
                .append(" ").append("as").append(" ")
                .append(indexMapping(queryClass.indexOf(join.getJoinClass()))).append(" ");
    }

    private static void select(Entity entity, boolean isDistinct, NewExpression<?> newExpression, List<Class<?>> queryClass, List<Class<?>> joinClass)
    {
        entity.sql.append("select").append(" ");
        if (isDistinct) entity.sql.append("distinct").append(" ");
        if (!newExpression.getExpressions().isEmpty())
        {
            for (var expression : newExpression.getExpressions())
            {
                doResolve(entity, expression, queryClass);
                entity.sql.append(",");
            }
            entity.sql.deleteCharAt(entity.sql.length() - 1);
        }
        else
        {
            entity.sql.append(indexMapping(queryClass.indexOf(newExpression.getTarget()))).append(".").append("*");
        }
        entity.sql.append(" ");
        entity.sql.append("from").append(" ");
        for (var c : queryClass)
        {
            if (joinClass == null || !joinClass.contains(c))
            {
                entity.sql.append(Cache.getTableName(c)).append(" ").append("as").append(" ")
                        .append(indexMapping(queryClass.indexOf(c))).append(",");
            }
        }
        entity.sql.deleteCharAt(entity.sql.length() - 1).append(" ");
    }

    private static void doResolve(Entity entity, IExpression expression, List<Class<?>> queryClass)
    {
        if (expression instanceof MappingExpression)
        {
            doResolveMappingExpression((MappingExpression) expression, entity, queryClass);
        }
        else if (expression instanceof DbRefExpression)
        {
            doResolveDbRefExpression((DbRefExpression) expression, entity, queryClass);
        }
        else if (expression instanceof DbFuncExpression)
        {
            doResolveDbFuncExpression((DbFuncExpression) expression, entity, queryClass);
        }
        else if (expression instanceof BinaryExpression)
        {
            doResolveBinaryExpression((BinaryExpression) expression, entity, queryClass);
        }
        else if (expression instanceof UnaryExpression)
        {
            doResolveUnaryExpression((UnaryExpression) expression, entity, queryClass);
        }
        else if (expression instanceof ValueExpression)
        {
            doResolveValueExpression((ValueExpression) expression, entity, queryClass);
        }
        else if (expression instanceof ParensExpression)
        {
            doResolveParensExpression((ParensExpression) expression, entity, queryClass);
        }
    }

    private static void doResolveMappingExpression(MappingExpression mapping, Entity entity, List<Class<?>> queryClass)
    {
        doResolve(entity, mapping.getValue(), queryClass);
    }

    private static void doResolveDbRefExpression(DbRefExpression dbRef, Entity entity, List<Class<?>> queryClass)
    {
        var map = Cache.getJavaFieldNameToDbFieldNameMappingMap(queryClass.get(dbRef.getIndex()));
        entity.sql.append(indexMapping(dbRef.getIndex())).append(".").append(map.get(dbRef.getRef()));
    }

    private static void doResolveUnaryExpression(UnaryExpression unary, Entity entity, List<Class<?>> queryClass)
    {
        if (unary.getOperator() == Operator.NOT)
        {
            if (unary.getExpression() instanceof ParensExpression)
            {
                entity.sql.append("!");
                doResolve(entity, unary.getExpression(), queryClass);
            }
            else
            {
                entity.sql.append("!(");
                doResolve(entity, unary.getExpression(), queryClass);
                entity.sql.deleteCharAt(entity.sql.length() - 1).append(")").append(" ");
            }
        }
    }

    private static void doResolveValueExpression(ValueExpression value, Entity entity, List<Class<?>> queryClass)
    {
        if (value.getValue() != null)
        {
            entity.sql.append("?");
            entity.values.add(value.getValue());
        }
        else
        {
            entity.sql.append("null");
        }
    }

    private static void doResolveBinaryExpression(BinaryExpression binary, Entity entity, List<Class<?>> queryClass)
    {
        doResolve(entity, binary.getLeft(), queryClass);
        if (entity.sql.charAt(entity.sql.length() - 1) != ' ') entity.sql.append(" ");
        if (binary.getRight() instanceof ValueExpression && ((ValueExpression) binary.getRight()).getValue() == null)
        {
            switch (binary.getOperator())
            {
                case EQ:
                    entity.sql.append("is");
                    break;
                case NE:
                    entity.sql.append("is not");
                    break;
            }
        }
        else
        {
            entity.sql.append(operatorToString(binary.getOperator()));
        }
        entity.sql.append(" ");
        if (binary.getOperator() == Operator.Like || binary.getOperator() == Operator.StartLike || binary.getOperator() == Operator.EndLike)
        {
            var value = (ValueExpression) binary.getRight();
            entity.sql.append("?");
            switch (binary.getOperator())
            {
                case Like:
                    entity.values.add("%" + value.getValue() + "%");
                    break;
                case StartLike:
                    entity.values.add(value.getValue() + "%");
                    break;
                case EndLike:
                    entity.values.add("%" + value.getValue());
                    break;
            }
            entity.values.add(value.getValue());
        }
        else if (binary.getOperator() == Operator.IN)
        {
            var value = (ValueExpression) binary.getRight();
            if (value.getValue() instanceof Iterable)
            {
                var it = ((Iterable<?>) value.getValue()).iterator();
                entity.sql.append("(");
                while (it.hasNext())
                {
                    entity.sql.append("?").append(",");
                    entity.values.add(it.next());
                }
                entity.sql.deleteCharAt(entity.sql.length() - 1).append(")");
            }
        }
        else
        {
            doResolve(entity, binary.getRight(), queryClass);
        }
        if (entity.sql.charAt(entity.sql.length() - 1) != ' ') entity.sql.append(" ");
    }

    private static void doResolveParensExpression(ParensExpression parens, Entity entity, List<Class<?>> queryClass)
    {
        entity.sql.append("(");
        doResolve(entity, parens.getExpression(), queryClass);
        entity.sql.deleteCharAt(entity.sql.length() - 1).append(")").append(" ");
    }

    private static void doResolveDbFuncExpression(DbFuncExpression dbFuncExpression, Entity entity, List<Class<?>> queryClass)
    {
        var expression = dbFuncExpression.getExpression();
        if (expression instanceof DbRefExpression)
        {
            var dbRef = (DbRefExpression) expression;
            var map = Cache.getJavaFieldNameToDbFieldNameMappingMap(queryClass.get(dbRef.getIndex()));
            switch (dbFuncExpression.getDbFuncType())
            {
                case Count:
                    if (dbRef.getRef().equals(""))
                    {
                        entity.sql.append("count(*)");
                    }
                    else
                    {
                        entity.sql.append("count(").append(indexMapping(dbRef.getIndex())).append(".")
                                .append(map.get(dbRef.getRef())).append(")");
                    }
                    break;
                case Sum:
                    if (dbRef.getRef().equals(""))
                    {
                        entity.sql.append("sum(*)");
                    }
                    else
                    {
                        entity.sql.append("sum(").append(indexMapping(dbRef.getIndex())).append(".")
                                .append(map.get(dbRef.getRef())).append(")");
                    }
                    break;
            }
        }
        else if (expression instanceof ValueExpression)
        {
            var value = (ValueExpression) expression;
            switch (dbFuncExpression.getDbFuncType())
            {
                case Count:
                    entity.sql.append("count(").append(value.getValue()).append(")");
                    break;
                case Sum:
                    entity.sql.append("sum(").append(value.getValue()).append(")");
                    break;
            }
        }
        else if (expression instanceof DbFuncExpression)
        {
            var funcExpression = (DbFuncExpression) expression;
            switch (dbFuncExpression.getDbFuncType())
            {
                case Count:
                    entity.sql.append("count(");
                    doResolveDbFuncExpression(funcExpression, entity, queryClass);
                    entity.sql.append(")");
                    break;
                case Sum:
                    entity.sql.append("sum(");
                    doResolveDbFuncExpression(funcExpression, entity, queryClass);
                    entity.sql.append(")");
                    break;
            }
        }
    }

    private static void where(Entity entity, IExpression expression, List<Class<?>> queryClass)
    {
        entity.sql.append("where").append(" ");
        doResolve(entity, expression, queryClass);
    }

    private static void on(Entity entity, IExpression expression, List<Class<?>> queryClass)
    {
        entity.sql.append("on").append(" ");
        doResolve(entity, expression, queryClass);
    }

    private static void insertOne(Entity entity, InsertOne<?> insertOne)
    {
        var target = insertOne.getTarget();
        entity.sql.append("insert into ").append(Cache.getTableName(target.getClass())).append("(");
        var map = Cache.getJavaFieldNameToDbFieldNameMappingMap(target.getClass());
        int count = 0;
        for (var field : target.getClass().getDeclaredFields())
        {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Id.class))
            {
                continue;
            }
            try
            {
                var o = field.get(target);
                if (o != null)
                {
                    entity.sql.append(map.get(field.getName())).append(",");
                    entity.values.add(o);
                    count++;
                }
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }
        if (count > 0) entity.sql.deleteCharAt(entity.sql.length() - 1);
        entity.sql.append(")").append(" ").append("values").append("(");
        for (int i = 0; i < count; i++)
        {
            entity.sql.append("?").append(",");
        }
        if (count > 0) entity.sql.deleteCharAt(entity.sql.length() - 1);
        entity.sql.append(")").append(" ");
    }

    private static void set(Entity entity, Set<?> set)
    {
        var target = set.getTarget();
        entity.sql.append("update ").append(Cache.getTableName(target.getClass())).append(" ")
                .append("as").append(" ").append(indexMapping(0)).append(" ").append("set").append(" ");
        var map = Cache.getJavaFieldNameToDbFieldNameMappingMap(target.getClass());
        boolean flag = false;
        for (var field : target.getClass().getDeclaredFields())
        {
            field.setAccessible(true);
            try
            {
                var o = field.get(target);
                if (o != null)
                {
                    entity.sql.append(indexMapping(0)).append(".")
                            .append(map.get(field.getName()))
                            .append("=").append("?").append(",");
                    entity.values.add(o);
                    flag = true;
                }
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }
        if (flag) entity.sql.deleteCharAt(entity.sql.length() - 1);
        entity.sql.append(" ");
    }

    private static void take(Entity entity, Take take)
    {
        entity.sql.append("limit").append(" ");
        entity.sql.append(take.getCount()).append(" ");
    }

    private static void skip(Entity entity, Skip skip)
    {
        entity.sql.append("offset").append(" ");
        entity.sql.append(skip.getCount()).append(" ");
    }

    private static void orderBy(Entity entity, OrderBy orderBy, List<Class<?>> queryClass)
    {
        var ref = orderBy.getDbRefExpression();
        entity.sql.append("order by ").append(indexMapping(ref.getIndex()));
        if (!ref.getRef().equals(""))
        {
            var map = Cache.getJavaFieldNameToDbFieldNameMappingMap(queryClass.get(ref.getIndex()));
            entity.sql.append(".").append(map.get(ref.getRef()));
        }
        entity.sql.append(" ");
    }

    private static String removeGetOrSet(String methodName)
    {
        var name = methodName;
        if (name.startsWith("get") || name.startsWith("set"))
        {
            var temp = methodName.substring(3);
            name = temp.substring(0, 1).toLowerCase() + temp.substring(1);
        }
        return name;
    }

    private static String indexMapping(int index)
    {
        return Mapping.get(index);
    }

    private static String operatorToString(Operator operator)
    {
        switch (operator)
        {
            case NOT:
                return "not";
            case EQ:
                return "=";
            case NE:
                return "!=";
            case GE:
                return ">=";
            case LE:
                return "<=";
            case GT:
                return ">";
            case LT:
                return "<";
            case Like:
            case StartLike:
            case EndLike:
                return "like";
            case And:
                return "and";
            case Or:
                return "or";
            case PLUS:
                return "+";
            case MINUS:
                return "-";
            case MUL:
                return "*";
            case DIV:
                return "/";
            case MOD:
                return "%";
            case IN:
                return "in";
            default:
                throw new RuntimeException("未知运算符");
        }
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
