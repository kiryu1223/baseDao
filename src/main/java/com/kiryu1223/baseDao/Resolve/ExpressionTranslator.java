package com.kiryu1223.baseDao.Resolve;

import com.kiryu1223.baseDao.ExpressionV2.FieldSelectExpression;
import com.kiryu1223.baseDao.ExpressionV2.IExpression;
import com.kiryu1223.baseDao.ExpressionV2.NewExpression;
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
    private final Map<Name, java.util.List<Class<? extends IExpression>>> methodExpressionTypeMap;
    private final Map<JCTree.Tag, JCTree.JCFieldAccess> opMap;
    private final Map<IExpression.Type, JCTree.JCFieldAccess> expressionMap;
    private final TreeMaker treeMaker;
    private final Names names;

    public ExpressionTranslator(
            Map<Name, java.util.List<Class<? extends IExpression>>> methodExpressionTypeMap,
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
        if (invocation.getMethodSelect() instanceof JCTree.JCFieldAccess)
        {
            var fieldAccess = (JCTree.JCFieldAccess) invocation.getMethodSelect();
            if (methodExpressionTypeMap.containsKey(fieldAccess.getIdentifier()))
            {
                ListBuffer<JCTree.JCExpression> args = new ListBuffer<>();
                var list = methodExpressionTypeMap.get(fieldAccess.getIdentifier());
                var i = 0;
                for (var argument : invocation.getArguments())
                {
                    if (argument instanceof JCTree.JCLambda)
                    {
                        var aClass = list.get(i++);
                        var lambda = (JCTree.JCLambda) argument;
                        JCTree.JCExpression body = null;
                        if (aClass == NewExpression.class)
                        {
                            body = tryGetNewExpression(lambda);
                        }
                        else
                        {
                            body = tryGetExpression(lambda);
                        }
                        var decls = new ListBuffer<JCTree.JCVariableDecl>();
                        var def = treeMaker.VarDef(
                                treeMaker.Modifiers(0),
                                names.fromString("unused"), null, null, true);
                        decls.add(def);
                        decls.addAll(lambda.params);
                        var lam = treeMaker.Lambda(decls.toList(), body);
                        lam.setPos(argument.getPreferredPosition());
                        args.add(lam);
                    }
                    else
                    {
                        args.add(argument);
                    }
                }
                invocation.args = args.toList();
            }
        }
        super.visitApply(invocation);
    }

    private JCTree.JCMethodInvocation tryGetNewExpression(JCTree.JCLambda lambda)
    {
        var r = tryGetExpression(lambda);
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
        var r = doStart((JCTree.JCExpression) lambda.getBody());
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
            var binary = (JCTree.JCBinary) expression;
            var left = doStart(binary.getLeftOperand());
            var right = doStart(binary.getRightOperand());
            var op = opMap.get(binary.getTag());
            return treeMaker.Apply(
                    List.nil(),
                    expressionMap.get(IExpression.Type.Binary),
                    List.of(left, right, op)
            );
        }
        else if (expression instanceof JCTree.JCUnary)
        {
            var unary = (JCTree.JCUnary) expression;
            var value = doStart(unary.getExpression());
            return treeMaker.Apply(
                    List.nil(),
                    expressionMap.get(IExpression.Type.Unary),
                    List.of(value, opMap.get(unary.getTag()))
            );
        }
        else if (expression instanceof JCTree.JCIdent)
        {
            var ident = (JCTree.JCIdent) expression;
            return treeMaker.Apply(
                    List.nil(),
                    expressionMap.get(IExpression.Type.Reference),
                    List.of(ident)
            );
        }
        else if (expression instanceof JCTree.JCLiteral)
        {
            var literal = (JCTree.JCLiteral) expression;
            return treeMaker.Apply(
                    List.nil(),
                    expressionMap.get(IExpression.Type.Value),
                    List.of(literal)
            );
        }
        else if (expression instanceof JCTree.JCFieldAccess)
        {
            var fieldAccess = (JCTree.JCFieldAccess) expression;
            var selector = doStart(fieldAccess.getExpression());
            return treeMaker.Apply(
                    List.nil(),
                    expressionMap.get(IExpression.Type.FieldSelect),
                    List.of(selector, treeMaker.Literal(fieldAccess.getIdentifier().toString()))
            );
        }
        else if (expression instanceof JCTree.JCMethodInvocation)
        {
            var methodInvocation = (JCTree.JCMethodInvocation) expression;
            ListBuffer<JCTree.JCExpression> listBuffer = new ListBuffer<>();

            if (methodInvocation.getMethodSelect() instanceof JCTree.JCFieldAccess)
            {
                var fieldAccess = (JCTree.JCFieldAccess) methodInvocation.getMethodSelect();
                var selector = doStart(fieldAccess.getExpression());
                listBuffer.add(selector);
                listBuffer.add(treeMaker.Literal(fieldAccess.getIdentifier().toString()));
            }
            else if (methodInvocation.getMethodSelect() instanceof JCTree.JCIdent)
            {
                var ident = (JCTree.JCIdent) methodInvocation.getMethodSelect();
                listBuffer.add(treeMaker.Literal(TypeTag.BOT,null));
                listBuffer.add(treeMaker.Literal(ident.getName().toString()));
            }

            for (var argument : methodInvocation.getArguments())
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
            var newClass = (JCTree.JCNewClass) expression;
            ListBuffer<JCTree.JCExpression> listBuffer = new ListBuffer<>();
            listBuffer.append(treeMaker.Select(newClass.getIdentifier(), names.fromString("class")));
            if (newClass.getClassBody() != null && !newClass.getClassBody().getMembers().isEmpty())
            {
                var member = (JCTree.JCBlock) newClass.getClassBody().getMembers().get(0);
                for (var statement : member.getStatements())
                {
                    var expressionStatement = (JCTree.JCExpressionStatement) statement;
                    listBuffer.add(doStart(expressionStatement.getExpression()));
                }
            }
            return treeMaker.Apply(
                    List.nil(),
                    expressionMap.get(IExpression.Type.New),
                    listBuffer.toList()
            );
        }
        else if (expression instanceof JCTree.JCTypeCast)
        {
            var typeCast = (JCTree.JCTypeCast) expression;
            return doStart(typeCast.getExpression());
        }
        return null;
    }
}
