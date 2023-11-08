package com.kiryu1223.baseDao.JProperty;

import com.kiryu1223.baseDao.Dao.Cache;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import javax.lang.model.type.TypeKind;
import java.lang.reflect.Method;
import java.util.Map;

public class GetSetHelper
{
    private static Names names;

    public static void setNames(Names names1)
    {
        names = names1;
    }

    public static String getter(String name, JCTree.JCExpression expression)
    {
        String getter = "";
        if (expression instanceof JCTree.JCPrimitiveTypeTree)
        {
            JCTree.JCPrimitiveTypeTree primitiveTypeTree = (JCTree.JCPrimitiveTypeTree) expression;
            if (primitiveTypeTree.getPrimitiveTypeKind() == TypeKind.BOOLEAN
                    && name.startsWith("is"))
            {
                getter = name;
            }
            else
            {
                getter = "is" + firstUpperCase(name);
            }
        }
        else if (expression instanceof JCTree.JCIdent)
        {
            JCTree.JCIdent type = (JCTree.JCIdent) expression;
            if (type.getName().equals(names.fromString("Boolean"))
                    && name.startsWith("is"))
            {
                String removedIs = name.substring(2);
                getter = "get" + firstUpperCase(removedIs);
            }
            else
            {
                getter = "get" + firstUpperCase(name);
            }
        }
        else
        {
            getter = "get" + firstUpperCase(name);
        }
        return getter;
    }

    public static String setter(String name, JCTree.JCExpression expression)
    {
        if ((expression instanceof JCTree.JCPrimitiveTypeTree
                && ((JCTree.JCPrimitiveTypeTree) expression).getPrimitiveTypeKind() == TypeKind.BOOLEAN)
                ||
                (expression instanceof JCTree.JCIdent
                        && ((JCTree.JCIdent) expression).getName().equals(names.fromString("Boolean"))))
        {
            if (name.startsWith("is"))
            {
                String removedIs = name.substring(2);
                return "set" + firstUpperCase(removedIs);
            }
        }
        return "set" + firstUpperCase(name);
    }

    public static String firstUpperCase(String str)
    {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String firstLowerCase(String str)
    {
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    public static Name getType(JCTree.JCExpression expression, Map<Name, Name> nameMap)
    {
        if (expression instanceof JCTree.JCNewClass)
        {
            JCTree.JCNewClass newClass = (JCTree.JCNewClass) expression;
            if (newClass.getIdentifier() instanceof JCTree.JCIdent)
            {
                return ((JCTree.JCIdent) newClass.getIdentifier()).getName();
            }
            else if (newClass.getIdentifier() instanceof JCTree.JCTypeApply)
            {
                JCTree.JCTypeApply typeApply = (JCTree.JCTypeApply) newClass.getIdentifier();
                return ((JCTree.JCIdent) typeApply.getType()).getName();
            }
        }
        else if (expression instanceof JCTree.JCTypeCast)
        {
            JCTree.JCTypeCast typeCast = (JCTree.JCTypeCast) expression;
            if (typeCast.getType() instanceof JCTree.JCIdent)
            {
                JCTree.JCIdent ident = (JCTree.JCIdent) typeCast.getType();
                return ident.getName();
            }
            else if (typeCast.getType() instanceof JCTree.JCTypeApply)
            {
                JCTree.JCTypeApply typeApply = (JCTree.JCTypeApply) typeCast.getType();
                return ((JCTree.JCIdent) typeApply.getType()).getName();
            }
        }
        else if (expression instanceof JCTree.JCIdent)
        {
            Name name = ((JCTree.JCIdent) expression).getName();
            if (nameMap.containsKey(name))
            {
                return nameMap.get(name);
            }
        }
        else if (expression instanceof JCTree.JCLiteral)
        {
            return null;
//            var value = ((JCTree.JCLiteral) expression).getValue();
//            var name = value.getClass().getSimpleName();
//            switch (name)
//            {
//                case "int":
//                    return treeMaker.TypeIdent(TypeTag.INT);
//                case "short":
//                    return treeMaker.TypeIdent(TypeTag.SHORT);
//                case "byte":
//                    return treeMaker.TypeIdent(TypeTag.BYTE);
//                case "long":
//                    return treeMaker.TypeIdent(TypeTag.LONG);
//                case "boolean":
//                    return treeMaker.TypeIdent(TypeTag.BOOLEAN);
//                case "float":
//                    return treeMaker.TypeIdent(TypeTag.FLOAT);
//                case "double":
//                    return treeMaker.TypeIdent(TypeTag.DOUBLE);
//                case "char":
//                    return treeMaker.TypeIdent(TypeTag.CHAR);
//                case "Integer":
//                    return treeMaker.Ident(names.fromString("Integer"));
//                case "Short":
//                    return treeMaker.Ident(names.fromString("Short"));
//                case "Byte":
//                    return treeMaker.Ident(names.fromString("Byte"));
//                case "Long":
//                    return treeMaker.Ident(names.fromString("Long"));
//                case "Boolean":
//                    return treeMaker.Ident(names.fromString("Boolean"));
//                case "Float":
//                    return treeMaker.Ident(names.fromString("Float"));
//                case "Double":
//                    return treeMaker.Ident(names.fromString("Double"));
//                case "Character":
//                    return treeMaker.Ident(names.fromString("Character"));
//            }
        }
        else if (expression instanceof JCTree.JCAssign)
        {
            JCTree.JCAssign assign = (JCTree.JCAssign) expression;
            return getType(assign.getVariable(), nameMap);
        }
        else if (expression instanceof JCTree.JCBinary)
        {
            JCTree.JCBinary binary = (JCTree.JCBinary) expression;
            return getType(binary.getLeftOperand(), nameMap);
        }
        else if (expression instanceof JCTree.JCFieldAccess)
        {
            JCTree.JCFieldAccess fieldAccess = (JCTree.JCFieldAccess) expression;
            return getType(fieldAccess.getExpression(), nameMap);
        }
        else if (expression instanceof JCTree.JCMethodInvocation)
        {
            return null;
        }
        throw new RuntimeException("找不到类型 ---> " + expression + "\n" + expression.getClass());
    }

    public static String getterToFieldName(String getterName, Class<?> c)
    {
        Method getter = null;
        for (Method method : c.getMethods())
        {
            if (method.getName().equals(getterName))
            {
                getter = method;
                break;
            }
        }
        if (getter == null)
        {
            throw new RuntimeException("没有方法 " + getterName);
        }
        String name = "";
        Class<?> returnType = getter.getReturnType();
        if (returnType.equals(Boolean.class))
        {
            String mName = firstLowerCase(getter.getName().substring(3));
            boolean pass = false;
            for (java.lang.reflect.Field typeField : Cache.getTypeFields(c))
            {
                if (typeField.getName().equals(mName))
                {
                    pass = true;
                    break;
                }
            }
            if (!pass)
            {
                mName = "is" + firstUpperCase(mName);
                for (java.lang.reflect.Field typeField : Cache.getTypeFields(c))
                {
                    if (typeField.getName().equals(mName))
                    {
                        pass = true;
                        break;
                    }
                }
            }
            if (!pass)
            {
                throw new RuntimeException("找不到对应属性 " + getterName);
            }
        }
        else if (returnType.equals(Boolean.TYPE))
        {
            String mName = getter.getName();
            boolean pass = false;
            for (java.lang.reflect.Field typeField : Cache.getTypeFields(c))
            {
                if (typeField.getName().equals(mName))
                {
                    pass = true;
                    break;
                }
            }
            if (!pass)
            {
                mName = firstLowerCase(mName.substring(2));
                for (java.lang.reflect.Field typeField : Cache.getTypeFields(c))
                {
                    if (typeField.getName().equals(mName))
                    {
                        pass = true;
                        break;
                    }
                }
            }
            if (!pass)
            {
                throw new RuntimeException("找不到对应属性 " + getterName);
            }
        }
        else
        {
            name = firstLowerCase(getter.getName().substring(3));
            try
            {
                c.getField(name);
            }
            catch (NoSuchFieldException e)
            {
                throw new RuntimeException(e + " 找不到对应属性 " + getterName);
            }
        }
        return name;
    }
}
