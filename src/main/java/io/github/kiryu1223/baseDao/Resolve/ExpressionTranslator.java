package io.github.kiryu1223.baseDao.Resolve;

import com.sun.tools.javac.util.Name;
import io.github.kiryu1223.baseDao.Resolve.info.*;
import io.github.kiryu1223.baseDao.ExpressionV2.IExpression;
import io.github.kiryu1223.baseDao.ExpressionV2.NewExpression;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class ExpressionTranslator extends TreeTranslator
{
    private final java.util.List<VarInfo> currentVarInfos;
    private final java.util.List<ClassInfo> classInfos;
    private final java.util.List<ImportInfo> importInfos;
    private final Map<JCTree.Tag, JCTree.JCFieldAccess> opMap;
    private final Map<IExpression.Type, JCTree.JCFieldAccess> expressionMap;
    private final TreeMaker treeMaker;
    private final Names names;
    private final ThreadLocal<JCTree.JCFieldAccess> localClass = new ThreadLocal<>();

    public ExpressionTranslator(
            java.util.List<VarInfo> currentVarInfos, java.util.List<ClassInfo> classInfos, java.util.List<ImportInfo> importInfos, Map<JCTree.Tag, JCTree.JCFieldAccess> opMap,
            Map<IExpression.Type, JCTree.JCFieldAccess> expressionMap,
            TreeMaker treeMaker, Names names)
    {
        this.currentVarInfos = currentVarInfos;
        this.classInfos = classInfos;
        this.importInfos = importInfos;
        this.opMap = opMap;
        this.expressionMap = expressionMap;
        this.treeMaker = treeMaker;
        this.names = names;
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl tree)
    {
        super.visitMethodDef(tree);
        java.util.List<VarInfo> varInfos = new ArrayList<>();
        for (JCTree.JCVariableDecl parameter : tree.getParameters())
        {
            varInfos.add(new VarInfo(
                    parameter.getPreferredPosition(),
                    parameter.getName().toString(),
                    findFullName(parameter.getType().toString())
            ));
        }

        for (JCTree.JCStatement statement : tree.getBody().getStatements())
        {
            statement.accept(new TreeTranslator()
            {
                @Override
                public void visitApply(JCTree.JCMethodInvocation invocation)
                {
                    if (invocation.getMethodSelect() instanceof JCTree.JCFieldAccess)
                    {
                        Collections.reverse(varInfos);
                        JCTree.JCFieldAccess methodSelect = (JCTree.JCFieldAccess) invocation.getMethodSelect();
                        String typeName = findType(varInfos, methodSelect.getExpression());
                        String methodName = methodSelect.getIdentifier().toString();
                        List<JCTree.JCExpression> arguments = invocation.getArguments();
                        for (ClassInfo classInfo : classInfos)
                        {
                            if (classInfo.getFullName().equals(typeName))
                            {
                                for (MethodInfo methodInfo : classInfo.getMethodInfos())
                                {
                                    java.util.List<ParamInfo> paramInfos = methodInfo.getParamInfos();
                                    if (methodInfo.getMethodName().equals(methodName)
                                            && paramInfos.size() == arguments.size())
                                    {
                                        for (int i = 0; i < paramInfos.size(); i++)
                                        {
                                            ParamInfo paramInfo = paramInfos.get(i);
                                            JCTree.JCExpression expression = arguments.get(i);
                                            if (expression instanceof JCTree.JCLambda && paramInfo.isExpressionParam())
                                            {
                                                JCTree.JCLambda jcLambda = (JCTree.JCLambda) expression;
                                                if (jcLambda.getParameters().size() == paramInfo.getLambdaParamCount())
                                                {
                                                    extracted(invocation, i, jcLambda, paramInfo.getExpressionType());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Collections.reverse(varInfos);
                    }
                    super.visitApply(invocation);
                }
            });
            if (statement instanceof JCTree.JCVariableDecl)
            {
                JCTree.JCVariableDecl variableDecl = (JCTree.JCVariableDecl) statement;
                int pos = variableDecl.getPreferredPosition();
                if (variableDecl.getType() == null)
                {
                    JCTree.JCExpression initializer = variableDecl.getInitializer();
                    if (initializer != null)
                    {
                        Collections.reverse(varInfos);
                        String typeName = findType(varInfos, initializer);
                        Collections.reverse(varInfos);
                        if (typeName != null)
                        {
                            varInfos.add(new VarInfo(
                                    variableDecl.getPreferredPosition(),
                                    variableDecl.getName().toString(),
                                    typeName
                            ));
                        }
                    }
                }
                else
                {
                    String typeFullName = findFullName(variableDecl.getType().toString());
                    varInfos.add(new VarInfo(
                            variableDecl.getPreferredPosition(),
                            variableDecl.getName().toString(),
                            typeFullName
                    ));
                }
            }
        }
    }

    private String findType(java.util.List<VarInfo> varInfos, JCTree.JCExpression expression)
    {
        if (expression instanceof JCTree.JCNewClass)
        {
            JCTree.JCNewClass jcNewClass = (JCTree.JCNewClass) expression;
            return findFullName(jcNewClass.getIdentifier().toString());
        }
        else if (expression instanceof JCTree.JCIdent)
        {
            JCTree.JCIdent jcIdent = (JCTree.JCIdent) expression;
            String varName = jcIdent.toString();
            for (VarInfo varInfo : varInfos)
            {
                if (varInfo.varName.equals(varName) && varInfo.pos < expression.getPreferredPosition())
                {
                    return varInfo.typeName;
                }
            }
            for (VarInfo currentVarInfo : currentVarInfos)
            {
                if (currentVarInfo.varName.equals(varName))
                {
                    return currentVarInfo.typeName;
                }
            }
        }
        if (expression instanceof JCTree.JCMethodInvocation)
        {
            JCTree.JCMethodInvocation methodInvocation = (JCTree.JCMethodInvocation) expression;
            if (methodInvocation.getMethodSelect() instanceof JCTree.JCFieldAccess)
            {
                JCTree.JCFieldAccess fieldAccess = (JCTree.JCFieldAccess) methodInvocation.getMethodSelect();
                String methodName = fieldAccess.getIdentifier().toString();
                String typeName = findType(varInfos, fieldAccess.getExpression());
                for (ClassInfo classInfo : classInfos)
                {
                    if (classInfo.getFullName().equals(typeName))
                    {
                        for (MethodInfo methodInfo : classInfo.getMethodInfos())
                        {
                            if (methodInfo.getMethodName().equals(methodName)
                                    && methodInfo.getParamInfos().size()
                                    == methodInvocation.getArguments().size())
                            {
                                return methodInfo.getReturnType();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void extracted(JCTree.JCMethodInvocation invocation, int index, JCTree.JCLambda lambda, Class<? extends IExpression> ie)
    {
        JCTree.JCExpression body = null;
        if (ie == NewExpression.class)
        {
            body = tryGetNewExpression(lambda);
        }
        else
        {
            body = tryGetExpression(lambda);
        }
        ListBuffer<JCTree.JCVariableDecl> decls = new ListBuffer<JCTree.JCVariableDecl>();
        JCTree.JCVariableDecl def = treeMaker.VarDef(
                treeMaker.Modifiers(0),
                names.fromString("unused"), null, null);
        decls.add(def);
        decls.addAll(lambda.params);
        JCTree.JCLambda lam = treeMaker.Lambda(decls.toList(), body);
        lam.setPos(lambda.getPreferredPosition());
        ListBuffer<JCTree.JCExpression> args = new ListBuffer<>();
        for (int i = 0; i < invocation.args.size(); i++)
        {
            if (i == index)
            {
                args.add(lam);
                continue;
            }
            args.add(invocation.args.get(i));
        }
        invocation.args = args.toList();
    }

    private JCTree.JCMethodInvocation tryGetNewExpression(JCTree.JCLambda lambda)
    {
        JCTree.JCMethodInvocation r = tryGetExpression(lambda);
        if (!r.getMethodSelect().equals(expressionMap.get(IExpression.Type.New)))
        {
            if (r.getMethodSelect().equals(expressionMap.get(IExpression.Type.Reference)))
            {
                r = treeMaker.Apply(
                        List.nil(),
                        expressionMap.get(IExpression.Type.New),
                        r.getArguments()
                );
            }
            else
            {
                throw new RuntimeException(lambda.toString());
            }
        }
        return r;
    }

    private JCTree.JCMethodInvocation tryGetExpression(JCTree.JCLambda lambda)
    {
        switch (lambda.getBodyKind())
        {
            case EXPRESSION:
            {
                JCTree.JCExpression expression = (JCTree.JCExpression) lambda.getBody();
                JCTree.JCExpression r = doStart(expression);
                if (r instanceof JCTree.JCMethodInvocation)
                {
                    return (JCTree.JCMethodInvocation) r;
                }
                else
                {
                    throw new RuntimeException(lambda + " " + r);
                }
            }
            case STATEMENT:
            {
                JCTree.JCBlock block = (JCTree.JCBlock) lambda.getBody();
                ListBuffer<JCTree.JCExpression> args = new ListBuffer<>();
                for (JCTree.JCStatement statement : block.getStatements())
                {
                    if (statement instanceof JCTree.JCExpressionStatement)
                    {
                        JCTree.JCExpression r = doStart(((JCTree.JCExpressionStatement) statement).getExpression());
                        if (r instanceof JCTree.JCMethodInvocation)
                        {
                            args.add(r);
                        }
                        else
                        {
                            throw new RuntimeException(lambda + " " + r);
                        }
                    }
                }
                return treeMaker.Apply(
                        List.nil(),
                        expressionMap.get(IExpression.Type.Block),
                        args.toList()
                );
            }
            default:
                throw new RuntimeException(lambda.toString());
        }
    }

    private JCTree.JCExpression doStart(JCTree.JCExpression expression)
    {
        if (expression instanceof JCTree.JCBinary)
        {
            JCTree.JCBinary binary = (JCTree.JCBinary) expression;
            JCTree.JCExpression left = doStart(binary.getLeftOperand());
            JCTree.JCExpression right = doStart(binary.getRightOperand());
            JCTree.JCFieldAccess op = opMap.get(binary.getTag());
            return treeMaker.Apply(
                    List.nil(),
                    expressionMap.get(IExpression.Type.Binary),
                    List.of(left, right, op)
            );
        }
        else if (expression instanceof JCTree.JCUnary)
        {
            JCTree.JCUnary unary = (JCTree.JCUnary) expression;
            JCTree.JCExpression value = doStart(unary.getExpression());
            return treeMaker.Apply(
                    List.nil(),
                    expressionMap.get(IExpression.Type.Unary),
                    List.of(value, opMap.get(unary.getTag()))
            );
        }
        else if (expression instanceof JCTree.JCIdent)
        {
            JCTree.JCIdent ident = (JCTree.JCIdent) expression;
            String id = ident.getName().toString();
            boolean flag = false;
            for (ImportInfo importInfo : importInfos)
            {
                String[] imp = importInfo.getImportName().split("\\.");
                if (imp[imp.length - 1].equals(id) && !importInfo.isStatic())
                {
                    flag = true;
                    break;
                }
            }
            return flag ? treeMaker.Apply(
                    List.nil(),
                    expressionMap.get(IExpression.Type.Reference),
                    List.of(treeMaker.Select(ident, names._class))
            ) : treeMaker.Apply(
                    List.nil(),
                    expressionMap.get(IExpression.Type.Reference),
                    List.of(ident)
            );

        }
        else if (expression instanceof JCTree.JCLiteral)
        {
            JCTree.JCLiteral literal = (JCTree.JCLiteral) expression;
            return treeMaker.Apply(
                    List.nil(),
                    expressionMap.get(IExpression.Type.Value),
                    List.of(literal)
            );
        }
        else if (expression instanceof JCTree.JCFieldAccess)
        {
            JCTree.JCFieldAccess fieldAccess = (JCTree.JCFieldAccess) expression;
            JCTree.JCExpression selector = doStart(fieldAccess.getExpression());
            return treeMaker.Apply(
                    List.nil(),
                    expressionMap.get(IExpression.Type.FieldSelect),
                    List.of(selector, treeMaker.Literal(fieldAccess.getIdentifier().toString()))
            );
        }
        else if (expression instanceof JCTree.JCMethodInvocation)
        {
            JCTree.JCMethodInvocation methodInvocation = (JCTree.JCMethodInvocation) expression;
            ListBuffer<JCTree.JCExpression> listBuffer = new ListBuffer<>();

            if (methodInvocation.getMethodSelect() instanceof JCTree.JCFieldAccess)
            {
                JCTree.JCFieldAccess fieldAccess = (JCTree.JCFieldAccess) methodInvocation.getMethodSelect();
                JCTree.JCExpression selector = doStart(fieldAccess.getExpression());
                listBuffer.add(selector);
                listBuffer.add(treeMaker.Literal(fieldAccess.getIdentifier().toString()));
            }
            else if (methodInvocation.getMethodSelect() instanceof JCTree.JCIdent)
            {
                JCTree.JCIdent ident = (JCTree.JCIdent) methodInvocation.getMethodSelect();
                JCTree.JCFieldAccess loc = localClass.get();
                JCTree.JCMethodInvocation thiz = treeMaker.Apply(
                        List.nil(),
                        expressionMap.get(IExpression.Type.Reference),
                        List.of(loc == null ? treeMaker.Ident(names._this) : loc)
                );
                listBuffer.add(thiz);
                listBuffer.add(treeMaker.Literal(ident.getName().toString()));
            }

            for (JCTree.JCExpression argument : methodInvocation.getArguments())
            {
                listBuffer.add(doStart(argument));
            }

            return treeMaker.Apply(
                    List.nil(),
                    expressionMap.get(IExpression.Type.MethodCall),
                    listBuffer.toList()
            );
        }
        else if (expression instanceof JCTree.JCNewClass)
        {
            JCTree.JCNewClass newClass = (JCTree.JCNewClass) expression;
            ListBuffer<JCTree.JCExpression> listBuffer = new ListBuffer<>();
            JCTree.JCFieldAccess loc = treeMaker.Select(newClass.getIdentifier(), names.fromString("class"));
            listBuffer.append(loc);
            if (newClass.getClassBody() != null && !newClass.getClassBody().getMembers().isEmpty())
            {
                localClass.set(loc);
                JCTree.JCBlock member = (JCTree.JCBlock) newClass.getClassBody().getMembers().get(0);
                for (JCTree.JCStatement statement : member.getStatements())
                {
                    JCTree.JCExpressionStatement expressionStatement = (JCTree.JCExpressionStatement) statement;
                    listBuffer.add(doStart(expressionStatement.getExpression()));
                }
                localClass.remove();
            }
            return treeMaker.Apply(
                    List.nil(),
                    expressionMap.get(IExpression.Type.New),
                    listBuffer.toList()
            );
        }
        else if (expression instanceof JCTree.JCTypeCast)
        {
            JCTree.JCTypeCast typeCast = (JCTree.JCTypeCast) expression;
            return doStart(typeCast.getExpression());
        }
        else if (expression instanceof JCTree.JCParens)
        {
            JCTree.JCParens parens = (JCTree.JCParens) expression;
            JCTree.JCExpression val = doStart(parens.getExpression());
            return treeMaker.Apply(
                    List.nil(),
                    expressionMap.get(IExpression.Type.Parens),
                    List.of(val)
            );
        }
        else if (expression instanceof JCTree.JCAssign)
        {
            JCTree.JCAssign jcAssign = (JCTree.JCAssign) expression;
            JCTree.JCExpression left = doStart(jcAssign.getVariable());
            JCTree.JCExpression right = doStart(jcAssign.getExpression());
            return treeMaker.Apply(
                    List.nil(),
                    expressionMap.get(IExpression.Type.Assign),
                    List.of(left, right)
            );
        }
        return null;
    }

    private String findFullName(String typeName)
    {
        String[] typeSp = typeName.split("\\.");
        StringBuilder sb = new StringBuilder();
        for (ImportInfo Import : importInfos)
        {
            String importName = Import.getImportName();
            String[] sp = importName.split("\\.");
            if (sp[sp.length - 1].equals(typeSp[0]))
            {
                sb.append(importName);
                for (int i = 1; i < typeSp.length; i++)
                {
                    sb.append(".").append(typeSp[i]);
                }
                break;
            }
        }
        return sb.length() != 0 ? sb.toString() : typeName;
    }
}
