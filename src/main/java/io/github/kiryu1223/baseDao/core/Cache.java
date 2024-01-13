package io.github.kiryu1223.baseDao.core;

import javax.persistence.*;
import javax.persistence.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Cache
{
    private static final Map<Class<?>, Map<String, String>> JavaFieldNameToDbFieldNameMappingMap = new ConcurrentHashMap<>();

    private static void addClassFieldMapping(Class<?> c)
    {
        Map<String, String> map = new HashMap<>();
        for (Field field : c.getDeclaredFields())
        {
            field.setAccessible(true);
            map.put(field.getName(), field.isAnnotationPresent(Column.class) ? field.getAnnotation(Column.class).name() : field.getName());
        }
        JavaFieldNameToDbFieldNameMappingMap.put(c, map);
    }

    public static Map<String, String> getJavaFieldNameToDbFieldNameMappingMap(Class<?> c)
    {
        if (!JavaFieldNameToDbFieldNameMappingMap.containsKey(c))
        {
            addClassFieldMapping(c);
        }
        return JavaFieldNameToDbFieldNameMappingMap.get(c);
    }

    private static final Map<Class<?>, String> ClassNameToTableNameMappingMap = new ConcurrentHashMap<>();

    private static void addClassNameToTableNameMapping(Class<?> c)
    {
        String table = c.isAnnotationPresent(Table.class) ?
                "`" + c.getAnnotation(Table.class).schema() + "`.`" + c.getAnnotation(Table.class).name() + "`"
                : "`" + c.getSimpleName() + "`";
        ClassNameToTableNameMappingMap.put(c, table);
    }

    public static String getTableName(Class<?> c)
    {
        if (!ClassNameToTableNameMappingMap.containsKey(c))
        {
            addClassNameToTableNameMapping(c);
        }
        return ClassNameToTableNameMappingMap.get(c);
    }

    private static final Map<Class<?>, Map<String, Field>> DbNameToFieldMappingMap = new ConcurrentHashMap<>();

    private static void addDbNameToFieldMapping(Class<?> c)
    {
        if (c.isAnnotationPresent(Entity.class))
        {
            Map<String, Field> map = new HashMap<>();
            for (Field field : c.getDeclaredFields())
            {
                field.setAccessible(true);
                map.put(field.isAnnotationPresent(Column.class) ? field.getAnnotation(Column.class).name() : field.getName(), field);
            }
            DbNameToFieldMappingMap.put(c, map);
        }
        else
        {
            throw new RuntimeException("is not Entity!!");
        }
    }

    public static Map<String, Field> getDbNameToFieldMapping(Class<?> c)
    {
        if (!DbNameToFieldMappingMap.containsKey(c))
        {
            addDbNameToFieldMapping(c);
        }
        return DbNameToFieldMappingMap.get(c);
    }

    private static final Map<Class<?>, Map<String, Field>> FieldNameToFieldMappingMap = new ConcurrentHashMap<>();

    private static void addFieldNameToFieldMapping(Class<?> c)
    {
        Map<String, Field> map = new HashMap<>();
        for (Field field : c.getDeclaredFields())
        {
            field.setAccessible(true);
            map.put(field.getName(), field);
        }
        FieldNameToFieldMappingMap.put(c, map);
    }

    public static Map<String, Field> getFieldNameToFieldMapping(Class<?> c)
    {
        if (!FieldNameToFieldMappingMap.containsKey(c))
        {
            addFieldNameToFieldMapping(c);
        }
        return FieldNameToFieldMappingMap.get(c);
    }

    private static final Map<Class<?>, Map<String, Method>> MethodNameToMethodMappingMap = new ConcurrentHashMap<>();

    private static void addMethodNameToMethodMapping(Class<?> c)
    {
        Map<String, Method> methodMap = new HashMap<>();
        for (Method method : c.getDeclaredMethods())
        {
            method.setAccessible(true);
            if (method.getParameterCount() == 1)
            {
                methodMap.put(method.getName(), method);
            }
        }
        MethodNameToMethodMappingMap.put(c, methodMap);
    }

    public static Map<String, Method> getMethodNameToMethodMapping(Class<?> c)
    {
        if (!MethodNameToMethodMappingMap.containsKey(c))
        {
            addMethodNameToMethodMapping(c);
        }
        return MethodNameToMethodMappingMap.get(c);
    }

    private static final Map<Class<?>, List<Field>> TypeFieldMap = new ConcurrentHashMap<>();

    public static List<Field> getTypeFields(Class<?> c)
    {
        if (!TypeFieldMap.containsKey(c))
        {
            List<Field> fields = new ArrayList<Field>();
            for (Field a : c.getDeclaredFields())
            {
                a.setAccessible(true);
                fields.add(a);
            }
            TypeFieldMap.put(c, fields);
        }
        return TypeFieldMap.get(c);
    }

    public static String methodToTableFieldName(Method method)
    {
        if (method.isAnnotationPresent(Column.class))
        {
            return method.getAnnotation(Column.class).name();
        }
        Class<?> returnType = method.getReturnType();
        Class<?> declaringClass = method.getDeclaringClass();
        if (!returnType.isPrimitive())
        {
            try
            {
                String fieldName = method.getName().substring(3);
                fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
                Field field = declaringClass.getDeclaredField(fieldName);
                if (field.isAnnotationPresent(Column.class))
                {
                    return field.getAnnotation(Column.class).name();
                }
                else
                {
                    return field.getName();
                }
            }
            catch (NoSuchFieldException e)
            {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException(method.toString() + " Entity类必须使用包装类型");
    }

    public static String fieldToTableFieldName(Field field)
    {
        if (field.isAnnotationPresent(Column.class))
        {
            return field.getAnnotation(Column.class).name();
        }
        else
        {
            return field.getName();
        }
    }

    private static final Map<Method, Field> getterMap = new ConcurrentHashMap<>();

    public static Field getterToField(Method method)
    {
        try
        {
            if (!getterMap.containsKey(method))
            {
                Class<?> declaringClass = method.getDeclaringClass();
                String fieldName = method.getName().substring(3);
                fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
                Field field = declaringClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                getterMap.put(method, field);
            }
            return getterMap.get(method);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
    }
    private static final Map<Field, Method> setterMap = new ConcurrentHashMap<>();

    public static Method fieldToSetter(Field field)
    {
        try
        {
            if (!setterMap.containsKey(field))
            {
                Class<?> declaringClass = field.getDeclaringClass();
                String name = field.getName();
                String setter = name.substring(0, 1).toUpperCase() + name.substring(1);
                setter = "set" + setter;
                Method declaredMethod = declaringClass.getDeclaredMethod(setter, field.getType());
                setterMap.put(field,declaredMethod);
            }
            return setterMap.get(field);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static final List<String> index = new ArrayList<>(26);

    static
    {
        for (char c = 'a'; c <= 'z'; c++)
        {
            index.add(String.valueOf(c));
        }
    }
}
