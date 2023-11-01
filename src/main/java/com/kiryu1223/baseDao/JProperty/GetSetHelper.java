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
        var getter = "";
        if (expression instanceof JCTree.JCPrimitiveTypeTree)
        {
            var primitiveTypeTree = (JCTree.JCPrimitiveTypeTree) expression;
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
            var type = (JCTree.JCIdent) expression;
            if (type.getName().equals(names.fromString("Boolean"))
                    && name.startsWith("is"))
            {
                var removedIs = name.substring(2);
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
                var removedIs = name.substring(2);
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
            var newClass = (JCTree.JCNewClass) expression;
            if (newClass.getIdentifier() instanceof JCTree.JCIdent)
            {
                return ((JCTree.JCIdent) newClass.getIdentifier()).getName();
            }
            else if (newClass.getIdentifier() instanceof JCTree.JCTypeApply)
            {
                var typeApply = (JCTree.JCTypeApply) newClass.getIdentifier();
                return ((JCTree.JCIdent) typeApply.getType()).getName();
            }
        }
        else if (expression instanceof JCTree.JCTypeCast)
        {
            var typeCast = (JCTree.JCTypeCast) expression;
            if (typeCast.getType() instanceof JCTree.JCIdent)
            {
                var ident = (JCTree.JCIdent) typeCast.getType();
                return ident.getName();
            }
            else if (typeCast.getType() instanceof JCTree.JCTypeApply)
            {
                var typeApply = (JCTree.JCTypeApply) typeCast.getType();
                return ((JCTree.JCIdent) typeApply.getType()).getName();
            }
        }
        else if (expression instanceof JCTree.JCIdent)
        {
            var name = ((JCTree.JCIdent) expression).getName();
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
            var assign = (JCTree.JCAssign) expression;
            return getType(assign.getVariable(), nameMap);
        }
        else if (expression instanceof JCTree.JCBinary)
        {
            var binary = (JCTree.JCBinary) expression;
            return getType(binary.getLeftOperand(), nameMap);
        }
        else if (expression instanceof JCTree.JCFieldAccess)
        {
            var fieldAccess = (JCTree.JCFieldAccess) expression;
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
        for (var method : c.getMethods())
        {
            if (method.getName().equals(getterName))
            {
                getter = method;
            }
        }
        if (getter == null)
        {
            throw new RuntimeException("没有方法 " + getterName);
        }
        var name = "";
        var returnType = getter.getReturnType();
        if (returnType.equals(Boolean.class))
        {
            var mName = firstLowerCase(getter.getName().substring(3));
            var pass = false;
            for (var typeField : Cache.getTypeFields(c))
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
                for (var typeField : Cache.getTypeFields(c))
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
                throw new RuntimeException(getter.toString());
            }
        }
        else if (returnType.equals(Boolean.TYPE))
        {
            var mName = getter.getName();
            var pass = false;
            for (var typeField : Cache.getTypeFields(c))
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
                for (var typeField : Cache.getTypeFields(c))
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
                throw new RuntimeException(getter.toString());
            }
        }
        else
        {
            name = firstLowerCase(getter.getName().substring(3));
        }
        return name;
    }

    private boolean Ok;

    public boolean isOk()
    {
        return Ok;
    }
}
