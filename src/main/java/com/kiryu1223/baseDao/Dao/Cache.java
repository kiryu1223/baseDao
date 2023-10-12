package com.kiryu1223.baseDao.Dao;

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
        for (var field : c.getDeclaredFields())
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
        var value = (c.isAnnotationPresent(Table.class) ? c.getAnnotation(Table.class).name() : c.getSimpleName()).toLowerCase();
        ClassNameToTableNameMappingMap.put(c, value);
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
            for (var field : c.getDeclaredFields())
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
        for (var field : c.getDeclaredFields())
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
        for (var method : c.getDeclaredMethods())
        {
            method.setAccessible(true);
            if (method.getParameterCount() ==1)
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
        if(!TypeFieldMap.containsKey(c))
        {
            TypeFieldMap.put(c,new ArrayList<>(Arrays.asList(c.getDeclaredFields())));
        }
        return TypeFieldMap.get(c);
    }
}
