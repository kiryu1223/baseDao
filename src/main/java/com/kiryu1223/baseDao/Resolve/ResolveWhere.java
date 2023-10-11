package com.kiryu1223.baseDao.Resolve;

import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;

import java.util.Map;

public class ResolveWhere
{
    public ResolveWhere(TreeMaker treeMaker, Names names, Map<String, JCTree.JCMethodInvocation> faMap)
    {
        this.treeMaker = treeMaker;
        this.names = names;
        this.faMap = faMap;
    }

    private final TreeMaker treeMaker;
    private final Names names;
    private final Map<String, JCTree.JCMethodInvocation> faMap;

    public JCTree.JCMethodInvocation resolveWhere(JCTree.JCMethodInvocation methodInvocation)
    {
        var whereExpression = treeMaker.Select(treeMaker.Ident(names.fromString("org.example.MyProcessor.Expression")), names.fromString("WhereExpression"));
        var whereExpression_of = treeMaker.Select(whereExpression, names.fromString("of"));

        var arrow = methodInvocation.args.toString().split("->");
        var lambdaNikeName = arrow[0].replace("(", "").replace(")", "").replace(" ", "");
        var andList = resolveWhereStart(methodInvocation, arrow[1], java.util.List.of(lambdaNikeName.split(",")));

        ListBuffer<JCTree.JCExpression> temp = new ListBuffer<>();
        temp.append(treeMaker.Literal(lambdaNikeName));
        temp.addAll(andList);

        return treeMaker.Apply(List.nil(), whereExpression_of, temp.toList());
    }

    public ListBuffer<JCTree.JCMethodInvocation> resolveWhereStart(JCTree.JCMethodInvocation methodInvocation, String str, java.util.List<String> nikeNames)
    {
        //System.out.println(str);
        var andExpression = treeMaker.Select(treeMaker.Ident(names.fromString("org.example.MyProcessor.Expression")), names.fromString("AndExpression"));
        var andExpression_of = treeMaker.Select(andExpression, names.fromString("of"));
        ListBuffer<JCTree.JCMethodInvocation> andList = new ListBuffer<>();
        var splitOr = str.split("\\|\\|");
        for (var sor : splitOr)
        {
            var binaryList = resolveAnd(sor.trim(), nikeNames);
            if (!binaryList.isEmpty())
            {
                ListBuffer<JCTree.JCExpression> temp = new ListBuffer<>();
                temp.addAll(binaryList);
                JCTree.JCMethodInvocation and = treeMaker.Apply(
                        List.nil(),
                        andExpression_of,
                        temp.toList()
                );
                andList.append(and);
            }
        }
        return andList;
    }

    public ListBuffer<JCTree.JCMethodInvocation> resolveAnd(String str, java.util.List<String> nikeNames)
    {
        var sym = treeMaker.Select(treeMaker.Ident(names.fromString("org.example.MyProcessor.Expression")), names.fromString("Sym"));
        ListBuffer<JCTree.JCMethodInvocation> binaryList = new ListBuffer<>();
        var splits = str.split("&&");
        for (var split : splits)
        {
            JCTree.JCMethodInvocation binary = null;
            if (split.contains("=="))
            {
                var s = split.split("==");
                ListBuffer<JCTree.JCExpression> listBuffer = new ListBuffer<>();
                listBuffer.append(treeMaker.Literal(s[0].trim().replace("()", ""))).append(treeMaker.Select(sym, names.fromString("EQ")));
                binary = resVal(s[1].trim(), listBuffer, nikeNames);
            }
            else if (split.contains("!="))
            {
                var s = split.split("!=");
                ListBuffer<JCTree.JCExpression> listBuffer = new ListBuffer<>();
                listBuffer.append(treeMaker.Literal(s[0].trim().replace("()", ""))).append(treeMaker.Select(sym, names.fromString("NE")));
                binary = resVal(s[1].trim(), listBuffer, nikeNames);
            }
            else if (split.contains(">="))
            {
                var s = split.split(">=");
                ListBuffer<JCTree.JCExpression> listBuffer = new ListBuffer<>();
                listBuffer.append(treeMaker.Literal(s[0].trim().replace("()", ""))).append(treeMaker.Select(sym, names.fromString("GE")));
                binary = resVal(s[1].trim(), listBuffer, nikeNames);
            }
            else if (split.contains("<="))
            {
                var s = split.split("<=");
                ListBuffer<JCTree.JCExpression> listBuffer = new ListBuffer<>();
                listBuffer.append(treeMaker.Literal(s[0].trim().replace("()", ""))).append(treeMaker.Select(sym, names.fromString("LE")));
                binary = resVal(s[1].trim(), listBuffer, nikeNames);
            }
            else if (split.contains(">"))
            {
                var s = split.split(">");
                ListBuffer<JCTree.JCExpression> listBuffer = new ListBuffer<>();
                listBuffer.append(treeMaker.Literal(s[0].trim().replace("()", ""))).append(treeMaker.Select(sym, names.fromString("GT")));
                binary = resVal(s[1].trim(), listBuffer, nikeNames);
            }
            else if (split.contains("<"))
            {
                var s = split.split("<");
                ListBuffer<JCTree.JCExpression> listBuffer = new ListBuffer<>();
                listBuffer.append(treeMaker.Literal(s[0].trim().replace("()", ""))).append(treeMaker.Select(sym, names.fromString("LT")));
                binary = resVal(s[1].trim(), listBuffer, nikeNames);
            }
            else if (split.contains(".contains"))
            {
                var s = split.split("\\.contains");
                ListBuffer<JCTree.JCExpression> listBuffer = new ListBuffer<>();
                listBuffer.append(treeMaker.Literal(s[0].trim().replace("()", ""))).append(treeMaker.Select(sym, names.fromString("Like")));
                var trimmed = s[1].trim();
                binary = resVal(trimmed.substring(1, trimmed.length() - 1), listBuffer, nikeNames);
            }
            else if (split.contains(".startsWith"))
            {
                var s = split.split("\\.startsWith");
                ListBuffer<JCTree.JCExpression> listBuffer = new ListBuffer<>();
                listBuffer.append(treeMaker.Literal(s[0].trim().replace("()", ""))).append(treeMaker.Select(sym, names.fromString("StartLike")));
                var trimmed = s[1].trim();
                binary = resVal(trimmed.substring(1, trimmed.length() - 1), listBuffer, nikeNames);
            }
            else if (split.contains(".endsWith"))
            {
                var s = split.split("\\.endsWith");
                ListBuffer<JCTree.JCExpression> listBuffer = new ListBuffer<>();
                listBuffer.append(treeMaker.Literal(s[0].trim().replace("()", ""))).append(treeMaker.Select(sym, names.fromString("EndLike")));
                var trimmed = s[1].trim();
                binary = resVal(trimmed.substring(1, trimmed.length() - 1), listBuffer, nikeNames);
            }
            binaryList.append(binary);
        }
        return binaryList;
    }

    public JCTree.JCMethodInvocation resVal(String value, ListBuffer<JCTree.JCExpression> listBuffer, java.util.List<String> nikeNames)
    {
        var binaryExpression = treeMaker.Select(treeMaker.Ident(names.fromString("org.example.MyProcessor.Expression")), names.fromString("BinaryExpression"));
        var binaryExpression_of = treeMaker.Select(binaryExpression, names.fromString("of"));
        var binaryValuesExpression = treeMaker.Select(treeMaker.Ident(names.fromString("org.example.MyProcessor.Expression")), names.fromString("BinaryValuesExpression"));
        var binaryValuesExpression_of = treeMaker.Select(binaryValuesExpression, names.fromString("of"));
        var trimmed = value.trim();
        if (trimmed.startsWith("(") && trimmed.endsWith(")") && trimmed.contains("|"))
        {
            var inValues = trimmed.substring(1, trimmed.length() - 1).split("\\|");
            for (var inValue : inValues)
            {
                resValStart(inValue, listBuffer, nikeNames);
            }
            return treeMaker.Apply(
                    List.nil(),
                    binaryValuesExpression_of,
                    listBuffer.toList()
            );
        }
        else
        {
            resValStart(trimmed, listBuffer, nikeNames);
            return treeMaker.Apply(
                    List.nil(),
                    binaryExpression_of,
                    listBuffer.toList()
            );
        }
    }

    public void resValStart(String str, ListBuffer<JCTree.JCExpression> listBuffer, java.util.List<String> nikeNames)
    {
        var refType = treeMaker.Select(treeMaker.Ident(names.fromString("org.example.MyProcessor.Expression")), names.fromString("RefType"));
        var refType_Ref = treeMaker.Select(refType, names.fromString("Ref"));

        var trimmed = str.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\""))
        {
            listBuffer.append(treeMaker.Literal(trimmed.replace("\"", "")));
        }
        else if (Util.isInteger(trimmed))
        {
            listBuffer.append(treeMaker.Literal(Integer.parseInt(trimmed)));
        }
        else if (Util.isFloat(trimmed))
        {
            listBuffer.append(treeMaker.Literal(Float.parseFloat(trimmed)));
        }
        else if (Util.isDouble(trimmed))
        {
            listBuffer.append(treeMaker.Literal(Double.parseDouble(trimmed)));
        }
        else if (Util.isShort(trimmed))
        {
            listBuffer.append(treeMaker.Literal(Short.parseShort(trimmed)));
        }
        else if (Util.isLong(trimmed))
        {
            listBuffer.append(treeMaker.Literal(Long.parseLong(trimmed)));
        }
        else if (Util.isByte(trimmed))
        {
            listBuffer.append(treeMaker.Literal(Byte.parseByte(trimmed)));
        }
        else if (Util.isBoolean(trimmed))
        {
            listBuffer.append(treeMaker.Literal(Boolean.parseBoolean(trimmed)));
        }
        else if (Util.isNull(trimmed))
        {
            listBuffer.append(treeMaker.Literal(TypeTag.BOT, "null"));
        }
        else if (trimmed.contains("."))
        {
            if (nikeNames.contains(trimmed.split("\\.")[0]))
            {
                listBuffer.append(treeMaker.Literal(trimmed.replace("()", ""))).append(refType_Ref);
            }
            else if (faMap.containsKey(trimmed))
            {
                listBuffer.append(faMap.get(trimmed));
            }
        }
        else
        {
            listBuffer.append(treeMaker.Ident(names.fromString(trimmed)));
        }
    }
}
