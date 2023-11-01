package com.kiryu1223.baseDao.JProperty;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GetterSetterTranslator extends TreeTranslator
{
    private final Map<Name, Map<Name, JCTree.JCVariableDecl>> classPrivateFieldsMap;
    private final Map<Name, Name> nameMap;
    private final TreeMaker treeMaker;
    private final Names names;
    private final Name currentName;
    private final Set<Integer> poses = new HashSet<>();

    public GetterSetterTranslator(Map<Name, Map<Name, JCTree.JCVariableDecl>> classPrivateFieldsMap, Map<Name, Name> nameMap, TreeMaker treeMaker, Names names, Name currentName)
    {
        this.classPrivateFieldsMap = classPrivateFieldsMap;
        this.nameMap = nameMap;
        this.treeMaker = treeMaker;
        this.names = names;
        this.currentName = currentName;
    }

    @Override
    public void visitBinary(JCTree.JCBinary binary)
    {
        var pos = binary.getPreferredPosition();
        if (poses.contains(pos))
        {
            super.visitBinary(binary);
            return;
        }
        poses.add(pos);

        var left = visitGetSet(binary.getLeftOperand(), nameMap, PropertyProcessor.P.Get);
        if (left != null) binary.lhs = left;
        var right = visitGetSet(binary.getRightOperand(), nameMap, PropertyProcessor.P.Get);
        if (right != null) binary.rhs = right;
        super.visitBinary(binary);
    }

    @Override
    public void visitAssign(JCTree.JCAssign assign)
    {
        var pos = assign.getPreferredPosition();
        if (poses.contains(pos))
        {
            super.visitAssign(assign);
            return;
        }
        poses.add(pos);

        var right = visitGetSet(assign.getExpression(), nameMap, PropertyProcessor.P.Get);
        if (right != null)
        {
            assign.rhs = right;
        }
        var left = visitGetSet(assign.getVariable(), nameMap, PropertyProcessor.P.Set);
        if (left != null)
        {
            left.args = List.of(assign.rhs);
            left.setPos(pos);
            super.visitApply(left);
        }
        else
        {
            super.visitAssign(assign);
        }
    }

    @Override
    public void visitAssignop(JCTree.JCAssignOp assignOp)
    {
        var pos = assignOp.getPreferredPosition();
        if (poses.contains(pos))
        {
            super.visitAssignop(assignOp);
            return;
        }
        poses.add(pos);

        var left = visitGetSet(assignOp.getVariable(), nameMap, PropertyProcessor.P.Set);
        var right = visitGetSet(assignOp.getExpression(), nameMap, PropertyProcessor.P.Get);
        if (left != null)
        {
            var leftGet = visitGetSet(assignOp.getVariable(), nameMap, PropertyProcessor.P.Get);
            var binary = treeMaker.Binary(
                    assignOp.getTag().noAssignOp(),
                    leftGet,
                    right != null ? right : assignOp.getExpression()
            );
            left.args = List.of(binary);
            left.setPos(pos);
            super.visitApply(left);
        }
        else if (right != null)
        {
            assignOp.rhs = right;
            super.visitAssignop(assignOp);
        }
        else
        {
            super.visitAssignop(assignOp);
        }
    }

    @Override
    public void visitVarDef(JCTree.JCVariableDecl variableDecl)
    {
        var pos = variableDecl.getPreferredPosition();
        if (poses.contains(pos))
        {
            super.visitVarDef(variableDecl);
            return;
        }
        poses.add(pos);

        if (variableDecl.getInitializer() != null)
        {
            var right = visitGetSet(variableDecl.getInitializer(), nameMap, PropertyProcessor.P.Get);
            if (right != null)
            {
                variableDecl.init = right;
            }
        }
        super.visitVarDef(variableDecl);
    }

    @Override
    public void visitUnary(JCTree.JCUnary unary)
    {
        var pos = unary.getPreferredPosition();
        if (poses.contains(pos))
        {
            super.visitUnary(unary);
            return;
        }
        poses.add(pos);

        var main = visitGetSet(unary.getExpression(), nameMap, PropertyProcessor.P.Set);
        if (main != null)
        {
            var v = visitGetSet(unary.getExpression(), nameMap, PropertyProcessor.P.Get);
            JCTree.JCOperatorExpression operatorExpression = null;
            switch (unary.getTag())
            {
                case POS:
                case NEG:
                case NOT:
                case COMPL:
                    operatorExpression = treeMaker.Unary(unary.getTag(), v);
                    break;
                case PREINC:
                case POSTINC:
                    operatorExpression = treeMaker.Binary(
                            JCTree.Tag.PLUS,
                            v,
                            treeMaker.Literal(1)
                    );
                    break;
                case PREDEC:
                case POSTDEC:
                    operatorExpression = treeMaker.Binary(
                            JCTree.Tag.MINUS,
                            v,
                            treeMaker.Literal(1)
                    );
                    break;
            }
            if (operatorExpression == null)
            {
                throw new RuntimeException("Unary error ---> " + unary.toString());
            }
            main.args = List.of(operatorExpression);
            main.setPos(pos);
            super.visitApply(main);
        }
        else
        {
            super.visitUnary(unary);
        }
    }

    @Override
    public void visitApply(JCTree.JCMethodInvocation invocation)
    {
        var pos = invocation.getPreferredPosition();
        if (poses.contains(pos))
        {
            super.visitApply(invocation);
            return;
        }
        poses.add(pos);

        var args = new ListBuffer<JCTree.JCExpression>();
        for (var argument : invocation.getArguments())
        {
            var visited = visitGetSet(argument, nameMap, PropertyProcessor.P.Get);
            if (visited != null)
            {
                args.add(visited);
            }
            else
            {
                args.append(argument);
            }
        }
        invocation.args = args.toList();
        super.visitApply(invocation);
    }

    @Override
    public void visitSelect(JCTree.JCFieldAccess fieldAccess)
    {
        var pos = fieldAccess.getPreferredPosition();
        if (poses.contains(pos))
        {
            super.visitSelect(fieldAccess);
            return;
        }
        poses.add(pos);

        var access = visitGetSet(fieldAccess, nameMap, PropertyProcessor.P.Get);
        if (access != null)
        {
            access.setPos(pos);
            super.visitApply(access);
        }
        else
        {
            super.visitSelect(fieldAccess);
        }
    }

    @Override
    public void visitLambda(JCTree.JCLambda lambda)
    {
        var pos = lambda.getPreferredPosition();
        if (poses.contains(pos))
        {
            super.visitLambda(lambda);
            return;
        }
        poses.add(pos);
        java.util.List<Name> nameList = new ArrayList<>();
        for (var parameter : lambda.getParameters())
        {
            var variableDecl = (JCTree.JCVariableDecl) parameter;
            nameList.add(variableDecl.getName());
        }
        lambda.getBody().accept(new TreeTranslator()
        {
            @Override
            public void visitBinary(JCTree.JCBinary binary)
            {
                var pos = binary.getPreferredPosition();
                if (poses.contains(pos))
                {
                    super.visitBinary(binary);
                    return;
                }
                poses.add(pos);

                var left = visitAny(binary.getLeftOperand(), nameList, PropertyProcessor.P.Get);
                if (left != null) binary.lhs = left;
                var right = visitAny(binary.getRightOperand(), nameList, PropertyProcessor.P.Get);
                if (right != null) binary.rhs = right;
                super.visitBinary(binary);
            }

            @Override
            public void visitAssign(JCTree.JCAssign assign)
            {
                var pos = assign.getPreferredPosition();
                if (poses.contains(pos))
                {
                    super.visitAssign(assign);
                    return;
                }
                poses.add(pos);

                var right = visitAny(assign.getExpression(), nameList, PropertyProcessor.P.Get);
                if (right != null)
                {
                    assign.rhs = right;
                }
                var left = visitAny(assign.getVariable(), nameList, PropertyProcessor.P.Set);
                if (left != null)
                {
                    left.args = List.of(assign.rhs);
                    left.setPos(assign.getPreferredPosition());
                    super.visitApply(left);
                }
                else
                {
                    super.visitAssign(assign);
                }
            }

            @Override
            public void visitAssignop(JCTree.JCAssignOp assignOp)
            {
                var pos = assignOp.getPreferredPosition();
                if (poses.contains(pos))
                {
                    super.visitAssignop(assignOp);
                    return;
                }
                poses.add(pos);

                var left = visitAny(assignOp.getVariable(), nameList, PropertyProcessor.P.Set);
                var right = visitAny(assignOp.getExpression(), nameList, PropertyProcessor.P.Get);
                if (left != null)
                {
                    var leftGet = visitAny(assignOp.getVariable(), nameList, PropertyProcessor.P.Get);
                    var binary = treeMaker.Binary(
                            assignOp.getTag().noAssignOp(),
                            leftGet,
                            right != null ? right : assignOp.getExpression()
                    );
                    left.args = List.of(binary);
                    left.setPos(assignOp.getPreferredPosition());
                    super.visitApply(left);
                }
                else if (right != null)
                {
                    assignOp.rhs = right;
                    super.visitAssignop(assignOp);
                }
                else
                {
                    super.visitAssignop(assignOp);
                }
            }

            @Override
            public void visitVarDef(JCTree.JCVariableDecl variableDecl)
            {
                var pos = variableDecl.getPreferredPosition();
                if (poses.contains(pos))
                {
                    super.visitVarDef(variableDecl);
                    return;
                }

                if (variableDecl.getInitializer() != null)
                {
                    var right = visitAny(variableDecl.getInitializer(), nameList, PropertyProcessor.P.Get);
                    if (right != null)
                    {
                        variableDecl.init = right;
                    }
                }
                super.visitVarDef(variableDecl);
            }

            @Override
            public void visitUnary(JCTree.JCUnary unary)
            {
                var pos = unary.getPreferredPosition();
                if (poses.contains(pos))
                {
                    super.visitUnary(unary);
                    return;
                }
                poses.add(pos);

                var main = visitAny(unary.getExpression(), nameList, PropertyProcessor.P.Set);
                if (main != null)
                {
                    var v = visitAny(unary.getExpression(), nameList, PropertyProcessor.P.Get);
                    JCTree.JCOperatorExpression operatorExpression = null;
                    switch (unary.getTag())
                    {
                        case POS:
                        case NEG:
                        case NOT:
                        case COMPL:
                            operatorExpression = treeMaker.Unary(unary.getTag(), v);
                            break;
                        case PREINC:
                        case POSTINC:
                            operatorExpression = treeMaker.Binary(
                                    JCTree.Tag.PLUS,
                                    v,
                                    treeMaker.Literal(1)
                            );
                            break;
                        case PREDEC:
                        case POSTDEC:
                            operatorExpression = treeMaker.Binary(
                                    JCTree.Tag.MINUS,
                                    v,
                                    treeMaker.Literal(1)
                            );
                            break;
                    }
                    if (operatorExpression == null)
                    {
                        throw new RuntimeException("Unary error ---> " + unary.toString());
                    }
                    main.args = List.of(operatorExpression);
                    main.setPos(unary.getPreferredPosition());
                    super.visitApply(main);
                }
                else
                {
                    super.visitUnary(unary);
                }
            }

            @Override
            public void visitApply(JCTree.JCMethodInvocation invocation)
            {
                var pos = invocation.getPreferredPosition();
                if (poses.contains(pos))
                {
                    super.visitApply(invocation);
                    return;
                }
                poses.add(pos);

                var args = new ListBuffer<JCTree.JCExpression>();
                for (var argument : invocation.getArguments())
                {
                    var visited = visitAny(argument, nameList, PropertyProcessor.P.Get);
                    if (visited != null)
                    {
                        args.add(visited);
                    }
                    else
                    {
                        args.append(argument);
                    }
                }
                invocation.args = args.toList();
                super.visitApply(invocation);
            }

            @Override
            public void visitSelect(JCTree.JCFieldAccess fieldAccess)
            {
                var pos = fieldAccess.getPreferredPosition();
                if (poses.contains(pos))
                {
                    super.visitSelect(fieldAccess);
                    return;
                }
                poses.add(pos);

                var access = visitAny(fieldAccess, nameList, PropertyProcessor.P.Get);
                if (access != null)
                {
                    access.setPos(fieldAccess.getPreferredPosition());
                    super.visitApply(access);
                }
                else
                {
                    super.visitSelect(fieldAccess);
                }
            }
        });
        super.visitLambda(lambda);
    }

    private JCTree.JCMethodInvocation visitGetSet(JCTree.JCExpression expression, Map<Name, Name> nameMap, PropertyProcessor.P p)
    {
        if (expression instanceof JCTree.JCFieldAccess)
        {
            var select = ((JCTree.JCFieldAccess) expression).getExpression();
            if (select instanceof JCTree.JCIdent && nameMap.containsKey(((JCTree.JCIdent)
                    ((JCTree.JCFieldAccess) expression).getExpression()).getName()))
            {
                var pos = expression.getPreferredPosition();
                var fieldAccess = (JCTree.JCFieldAccess) expression;
                var type = nameMap.get(((JCTree.JCIdent) fieldAccess.getExpression()).getName());
                if (currentName.equals(type)) return null;
                var variableDeclMap = classPrivateFieldsMap.get(type);
                var variableDecl = variableDeclMap.get(fieldAccess.getIdentifier());
                if (variableDecl == null) return null;
                var fieldType = (JCTree.JCExpression) variableDecl.getType();
                var selectName = fieldAccess.getIdentifier().toString();
                var getter = GetSetHelper.getter(selectName, fieldType);
                var setter = GetSetHelper.setter(selectName, fieldType);
                var invoke = treeMaker.Apply(
                        List.nil(),
                        treeMaker.Select(fieldAccess.getExpression(), names.fromString(p == PropertyProcessor.P.Get ? getter : setter)),
                        List.nil()
                );
                invoke.setPos(pos);
                return invoke;
            }
            else if (select instanceof JCTree.JCTypeCast)
            {
                //TODO: 2023/10/27
            }
            else if (select instanceof JCTree.JCParens)
            {
                //TODO: 2023/10/27
            }
            else if (select instanceof JCTree.JCNewClass)
            {
                //TODO: 2023/10/27
            }
        }
        return null;
    }

    private JCTree.JCMethodInvocation visitAny(JCTree.JCExpression expression, java.util.List<Name> nameList, PropertyProcessor.P p)
    {
        if (expression instanceof JCTree.JCFieldAccess)
        {
            var fieldAccess = (JCTree.JCFieldAccess) expression;
            if (fieldAccess.getExpression() instanceof JCTree.JCIdent
                    && nameList.contains(((JCTree.JCIdent) fieldAccess.getExpression()).getName()))
            {
                var selectName = fieldAccess.getIdentifier().toString();
                switch (p)
                {
                    case Get:
                    {
                        var getter = "get" +
                                (selectName.startsWith("is")
                                        ? GetSetHelper.firstUpperCase(selectName.substring(2))
                                        : GetSetHelper.firstUpperCase(selectName));

                        return treeMaker.Apply(
                                List.nil(),
                                treeMaker.Select(
                                        fieldAccess.getExpression(),
                                        names.fromString(getter)
                                ),
                                List.nil()
                        );
                    }
                    case Set:
                    {
                        var setter = "set" +
                                (selectName.startsWith("is")
                                        ? GetSetHelper.firstUpperCase(selectName.substring(2))
                                        : GetSetHelper.firstUpperCase(selectName));

                        return treeMaker.Apply(
                                List.nil(),
                                treeMaker.Select(
                                        fieldAccess.getExpression(),
                                        names.fromString(setter)
                                ),
                                List.nil()
                        );
                    }
                }
            }
        }
        return null;
    }
}
