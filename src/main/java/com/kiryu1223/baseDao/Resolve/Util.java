package com.kiryu1223.baseDao.Resolve;

import com.sun.tools.javac.tree.JCTree;

import java.util.Map;
import java.util.Stack;

public class Util
{
    public static void setPosClassNames(JCTree.JCMethodInvocation tree, String args, Map<Integer, String> classPos)
    {
        if (classPos.containsKey(tree.getStartPosition()))
        {
            var classNames = classPos.get(tree.getStartPosition());
            String newClassNames = args.replace(".class", "").replace(" ", "").trim() + "," + classNames;
            classPos.put(tree.getStartPosition(), newClassNames);
        }
        else
        {
            classPos.put(tree.getStartPosition(), args.replace(".class", "").replace(" ", "").trim());
        }
    }

    public static String parseBracketString(String input)
    {
        Stack<Character> stack = new Stack<>();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++)
        {
            char c = input.charAt(i);
            if (c == '(')
            {
                stack.push(c);
                if (stack.size() == 1) continue;
            }
            else if (c == ')')
            {
                stack.pop();
                if (stack.isEmpty())
                {
                    return result.toString();
                }
            }
            if (!stack.isEmpty())
            {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static String getClassName(String input)
    {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++)
        {
            char c = input.charAt(i);
            if (c == '(' || c == ')')
            {
                break;
            }
            if (c != ' ')
            {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static boolean isNumeric(String str)
    {
        return str.matches("^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$");
    }

    public static boolean isInteger(String str)
    {
        try
        {
            Integer.parseInt(str.trim());
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

    public static boolean isFloat(String str)
    {
        try
        {
            Float.parseFloat(str.trim());
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

    public static boolean isDouble(String str)
    {
        try
        {
            Double.parseDouble(str.trim());
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

    public static boolean isLong(String str)
    {
        try
        {
            Long.parseLong(str.trim());
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

    public static boolean isByte(String str)
    {
        try
        {
            Byte.parseByte(str.trim());
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

    public static boolean isBoolean(String str)
    {
        return Boolean.parseBoolean(str.trim());
    }

    public static boolean isShort(String str)
    {
        try
        {
            Short.parseShort(str.trim());
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

    public static boolean isNull(String str)
    {
        return str.equals("null");
    }
}
