package io.github.kiryu1223.baseDao.core;

import io.github.kiryu1223.baseDao.core.Base.*;
import io.github.kiryu1223.baseDao.core.Inserter.Insert;
import io.github.kiryu1223.baseDao.core.Statement.Statement;
import io.github.kiryu1223.baseDao.core.Updater.Update;
import io.github.kiryu1223.baseDao.core.Deleter.Delete;
import io.github.kiryu1223.expressionTree.expressions.*;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class Resolver
{
    public static Entity query(boolean isDistinct, List<Base> bases, ExprTree<?> exprTree, List<Class<?>> joins)
    {
        Entity entity = new Entity();
        LambdaExpression lambda = exprTree.getTree();
        select(entity, isDistinct, lambda, joins);
        for (Base base : bases)
        {
            if (base instanceof Where)
            {
                where(entity, (Where) base);
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
                orderBy(entity, (OrderBy) base);
            }
            else if (base instanceof Join)
            {
                join(entity, (Join) base);
            }
            else if (base instanceof On)
            {
                on(entity, (On) base);
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
        entity.append("insert into ").append(Cache.getTableName(statement.getC1())).append(" ");
        for (Base base : statement.getBases())
        {
            if (base instanceof SetData)
            {
                set(entity, (SetData) base);
            }
        }
        return entity;
    }

    public static Entity delete(Statement<?> statement)
    {
        Entity entity = new Entity();
        entity.append("delete").append(" ").append("a")
                .append(".*").append(" ").append("from").append(" ")
                .append(Cache.getTableName(statement.getQueryClasses().get(0))).append(" ")
                .append("as").append(" ").append("a").append(" ");
        for (Base base : statement.getBases())
        {
            if (base instanceof Where)
            {
                where(entity, (Where) base);
            }
        }
        return entity;
    }

    public static Entity update(Statement<?> statement)
    {
        Entity entity = new Entity();
        entity.append("update ").append(Cache.getTableName(statement.getC1())).append(" ")
                .append("as").append(" ").append("a").append(" ");
        for (Base base : statement.getBases())
        {
            if (base instanceof SetData)
            {
                set(entity, (SetData) base);
            }
            else if (base instanceof Where)
            {
                where(entity, (Where) base);
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

    private static void set(Entity entity, SetData setData)
    {
        entity.append("set ");
        LambdaExpression lambda = setData.getLambdaExpression();
        List<ParameterExpression> parameters = lambda.getParameters();
        lambda.getBody().accept(new Visitor()
        {
            @Override
            public void visit(BlockExpression block)
            {
                for (Expression expression : block.getExpressions())
                {
                    visit(expression);
                    entity.blank();
                }
            }

            @Override
            public void visit(MethodCallExpression methodCall)
            {
                Expression expr = methodCall.getExpr();
                Method method = methodCall.getMethod();
                List<Expression> args = methodCall.getArgs();
                if (methodCall.inParameters(parameters) && args.size() == 1)
                {
                    entity.append(Cache.methodToTableFieldName(method)).append(" = ");
                    visit(args.get(0));
                }
            }

            @Override
            public void visit(ConstantExpression constant)
            {
                Object value = constant.getValue();
                if (value == null)
                {
                    entity.append("null");
                }
                else
                {
                    entity.questionMarkAndValue(value);
                }
            }

            @Override
            public void visit(ReferenceExpression reference)
            {
                Object value = reference.getRef();
                if (value == null)
                {
                    entity.append("null");
                }
                else
                {
                    entity.questionMarkAndValue(value);
                }
            }
        });
    }

    private static void join(Entity entity, Join join)
    {
        switch (join.getJoinType())
        {
            case Inner:
                entity.append("inner");
                break;
            case Left:
                entity.append("left");
                break;
            case Right:
                entity.append("right");
                break;
            case Full:
                entity.append("full");
                break;
        }
        entity.append(" join ").append(Cache.getTableName(join.getJoinClass()))
                .append(" as ")
                .append(Cache.index.get(join.getIndex()))
                .append(" ");
    }

    private static void select(Entity entity, boolean isDistinct, LambdaExpression lambda, List<Class<?>> joinClass)
    {
        entity.append("select").append(" ");
        if (isDistinct) entity.append("distinct").append(" ");
        addSelectField(entity, lambda);
        entity.append(" from ");
        List<ParameterExpression> parameters = lambda.getParameters();
        for (int i = 0; i < parameters.size(); i++)
        {
            Class<?> type = parameters.get(i).getType();
            if (joinClass.contains(type)) continue;
            entity.append(Cache.getTableName(type)).append(" as ").append(Cache.index.get(i)).append(",");
        }
        entity.deleteLast().append(" ");
    }

    private static void addConditional(Entity entity, LambdaExpression lambdaExpression)
    {
        List<ParameterExpression> parameters = lambdaExpression.getParameters();
        lambdaExpression.getBody().accept(new Visitor()
        {
            @Override
            public void visit(BinaryExpression binary)
            {
                visit(binary.getLeft());
                entity.blank().append(toSqlOp(binary.getOperatorType())).blank();
                visit(binary.getRight());
                entity.changeOp().blank();
            }

            @Override
            public void visit(ConstantExpression constant)
            {
                if (constant.getType() == Void.class)
                {
                    entity.append("null");
                }
                else
                {
                    entity.questionMarkAndValue(constant.getValue());
                }
            }

            @Override
            public void visit(ReferenceExpression reference)
            {
                Object value = reference.getRef();
                if (value == null)
                {
                    entity.append("null");
                }
                else
                {
                    entity.questionMarkAndValue(value);
                }
            }

            @Override
            public void visit(MethodCallExpression methodCall)
            {
                Expression expr = methodCall.getExpr();
                if (expr != null)
                {
                    List<Expression> args = methodCall.getArgs();
                    if (expr.getKind() == Kind.Parameter)
                    {
                        ParameterExpression parameter = (ParameterExpression) expr;
                        if (parameters.contains(parameter))
                        {
                            int indexOf = parameters.indexOf(parameter);
                            entity.append(Cache.index.get(indexOf)).append(".");
                            Method method = methodCall.getMethod();
                            if (args.isEmpty())
                            {
                                entity.append(Cache.methodToTableFieldName(method));
                            }
                        }
                    }
                    else if (expr instanceof MethodCallExpression
                            && ((MethodCallExpression) expr).inParameters(parameters)
                            && !args.isEmpty())
                    {
                        MethodCallExpression methodCallExpression = (MethodCallExpression) expr;
                        ParameterExpression parameter = (ParameterExpression) methodCallExpression.getExpr();
                        Method method = methodCall.getMethod();
                        switch (method.getName())
                        {
                            case "equals":
                                int indexOf = parameters.indexOf(parameter);
                                entity.append(Cache.index.get(indexOf)).append(".");
                                entity.append(Cache.methodToTableFieldName(methodCallExpression.getMethod()));
                                entity.blank().append("=").blank();
                                for (Expression arg : args)
                                {
                                    visit(arg);
                                }
                                entity.blank();
                                break;
                        }
                    }
                }
            }

            @Override
            public void visit(FieldSelectExpression fieldSelect)
            {
                Expression expr = fieldSelect.getExpr();
                Field field = fieldSelect.getField();
                if (expr != null)
                {
                    if (expr.getKind() == Kind.Parameter)
                    {
                        ParameterExpression parameter = (ParameterExpression) expr;
                        if (parameters.contains(parameter))
                        {
                            int indexOf = parameters.indexOf(parameter);
                            entity.append(Cache.index.get(indexOf)).append(".")
                                    .append(Cache.fieldToTableFieldName(field));
                        }
                    }
                    else if (expr.getKind() == Kind.Reference)
                    {
                        try
                        {
                            ReferenceExpression reference = (ReferenceExpression) expr;
                            Object value = field.get(reference.getKind());
                            entity.questionMarkAndValue(value);
                        }
                        catch (IllegalAccessException e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }

            @Override
            public void visit(ParensExpression parensExpression)
            {
                entity.append("(");
                visit(parensExpression.getExpr());
                entity.append(")");
            }

            @Override
            public void visit(UnaryExpression unary)
            {
                Kind kind = unary.getOperand().getKind();
                entity.append(toSqlOp(unary.getOperatorType())).append(kind == Kind.Parens ? "" : "(");
                visit(unary.getOperand());
                entity.append(kind == Kind.Parens ? "" : "(");
            }
        });
    }

    private static void addSelectField(Entity entity, LambdaExpression lambdaExpression)
    {
        Expression body = lambdaExpression.getBody();
        if (body == null) return;
        List<ParameterExpression> parameters = lambdaExpression.getParameters();
        List<Class<?>> qc = new ArrayList<>(parameters.size());
        for (ParameterExpression parameter : parameters)
        {
            qc.add(parameter.getType());
        }
        Set<Class<?>> classes = new HashSet<>(parameters.size());
        Set<Method> methods = new HashSet<>();
        body.accept(new DeepFindVisitor()
        {
            @Override
            public void visit(ParameterExpression parameter)
            {
                if (parameters.contains(parameter))
                {
                    classes.add(parameter.getType());
                }
            }

            @Override
            public void visit(MethodCallExpression methodCall)
            {
                if (methodCall.inParameters(parameters) && methodCall.getArgs().isEmpty())
                {
                    methods.add(methodCall.getMethod());
                }
                else
                {
                    super.visit(methodCall);
                }
            }
        });
        for (Class<?> clazz : classes)
        {
            methods.removeIf(a -> a.getDeclaringClass().equals(clazz));
        }
        for (Class<?> clazz : classes)
        {
            entity.classes.add(clazz);
            String index = Cache.index.get(qc.indexOf(clazz));
            for (Field typeField : Cache.getTypeFields(clazz))
            {
                entity.append(index).append(".")
                        .append(Cache.fieldToTableFieldName(typeField)).append(",");
            }
        }
        for (Method method : methods)
        {
            String index = Cache.index.get(qc.indexOf(method.getDeclaringClass()));
            entity.methods.add(method);
            entity.append(index).append(".")
                    .append(Cache.methodToTableFieldName(method)).append(",");
        }
        entity.deleteLast();
    }

    private static void where(Entity entity, Where where)
    {
        entity.append("where ");
        addConditional(entity, where.getLambdaExpression());
    }

    private static void on(Entity entity, On on)
    {
        entity.append("on ");
        addConditional(entity, on.getLambdaExpression());
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

    private static void orderBy(Entity entity, OrderBy orderBy)
    {
        entity.append("order by ");
        addConditional(entity, orderBy.getLambdaExpression());
        entity.blank();
    }

    private static String toSqlOp(OperatorType operatorType)
    {
        switch (operatorType)
        {
            case AND:
                return "and";
            case OR:
                return "or";
            case EQ:
                return "=";
            case NE:
                return "<>";
            default:
                return operatorType.getOperator();
        }
    }
}
