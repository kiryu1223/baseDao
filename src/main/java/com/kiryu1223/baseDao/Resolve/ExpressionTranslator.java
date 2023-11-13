package com.kiryu1223.baseDao.Resolve;

import com.kiryu1223.baseDao.ExpressionV2.FieldSelectExpression;
import com.kiryu1223.baseDao.ExpressionV2.IExpression;
import com.kiryu1223.baseDao.ExpressionV2.NewExpression;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import java.lang.ref.Reference;
import java.util.Map;

public class ExpressionTranslator extends TreeTranslator
{
    private final Map<Name, java.util.List<Info>> methodExpressionTypeMap;
    private final Map<JCTree.Tag, JCTree.JCFieldAccess> opMap;
    private final Map<IExpression.Type, JCTree.JCFieldAccess> expressionMap;
    private final TreeMaker treeMaker;
    private final Names names;
    private final ThreadLocal<JCTree.JCFieldAccess> localClass = new ThreadLocal<>();

    public ExpressionTranslator(
            Map<Name, java.util.List<Info>> methodExpressionTypeMap,
            Map<JCTree.Tag, JCTree.JCFieldAccess> opMap,
            Map<IExpression.Type, JCTree.JCFieldAccess> expressionMap,
            TreeMaker treeMaker, Names names)
    {
        this.methodExpressionTypeMap = methodExpressionTypeMap;
        this.opMap = opMap;
        this.expressionMap = expressionMap;
        this.treeMaker = treeMaker;
        this.names = names;
    }

    @Override
    public void visitApply(JCTree.JCMethodInvocation invocation)
    {
        Name name = null;
        if (invocation.getMethodSelect() instanceof JCTree.JCIdent)
        {
            JCTree.JCIdent ident = (JCTree.JCIdent) invocation.getMethodSelect();
            name = ident.getName();
        }
        else if (invocation.getMethodSelect() instanceof JCTree.JCFieldAccess)
        {
            JCTree.JCFieldAccess fieldAccess = (JCTree.JCFieldAccess) invocation.getMethodSelect();
            name = fieldAccess.getIdentifier();
        }
        if (name != null)
        {
            if (methodExpressionTypeMap.containsKey(name)
                    && invocation.getArguments().size() == 1
                    && invocation.getArguments().get(0) instanceof JCTree.JCLambda)
            {
                java.util.List<Info> infoList = methodExpressionTypeMap.get(name);
                JCTree.JCLambda lambda = (JCTree.JCLambda) invocation.getArguments().get(0);
                Info info = null;
                for (Info i : infoList)
                {
                    if (i.getCount() == lambda.getParameters().size())
                    {
                        info = i;
                        break;
                    }
                }
                if (info != null)
                {
                    JCTree.JCExpression body = null;
                    if (info.getType() == NewExpression.class)
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
                    invocation.args = List.of(lam);
                }
            }
        }
        super.visitApply(invocation);
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
        if (lambda.getBodyKind() != LambdaExpressionTree.BodyKind.EXPRESSION)
        {
            throw new RuntimeException(lambda.toString());
        }
        JCTree.JCExpression r = doStart((JCTree.JCExpression) lambda.getBody());
        if (r instanceof JCTree.JCMethodInvocation)
        {
            return (JCTree.JCMethodInvocation) r;
        }
        else
        {
            throw new RuntimeException(lambda + " " + r);
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
            return treeMaker.Apply(
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
        return null;
    }
}
