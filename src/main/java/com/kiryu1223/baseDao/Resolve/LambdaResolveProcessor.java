package com.kiryu1223.baseDao.Resolve;

import com.kiryu1223.baseDao.ExpressionV2.DbFuncType;
import com.kiryu1223.baseDao.ExpressionV2.IExpression;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.List;
import com.kiryu1223.baseDao.Error.UnsupportedFeatureException;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.persistence.Entity;
import java.util.*;

@SupportedAnnotationTypes({
        "javax.persistence.Entity",
        "com.kiryu1223.baseDao.Resolve.Resolve",
        "com.kiryu1223.baseDao.Resolve.Dao",
})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class LambdaResolveProcessor extends AbstractProcessor
{
    Messager messager;
    JavacTrees javacTrees;
    Context context;
    TreeMaker treeMaker;
    Names names;

    Map<IExpression.Type, JCTree.JCFieldAccess> ofmap = new HashMap<>();
    Map<JCTree.Tag, JCTree.JCFieldAccess> opmap = new HashMap<>();
    Map<String, JCTree.JCFieldAccess> strOpmap = new HashMap<>();
    Map<Class<?>, JCTree.JCFieldAccess> typeMap = new HashMap<>();
    Map<DbFuncType, JCTree.JCFieldAccess> dbFuncMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv)
    {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        javacTrees = JavacTrees.instance(processingEnv);
        context = ((JavacProcessingEnvironment) processingEnv).getContext();
        treeMaker = TreeMaker.instance(context);
        names = Names.instance(context);

        var expV2 = "com.kiryu1223.baseDao.ExpressionV2";

        var operator = treeMaker.Select(treeMaker.Ident(names.fromString(expV2)), names.fromString("Operator"));
        opmap.put(JCTree.Tag.EQ, treeMaker.Select(operator, names.fromString("EQ")));
        opmap.put(JCTree.Tag.NE, treeMaker.Select(operator, names.fromString("NE")));
        opmap.put(JCTree.Tag.GE, treeMaker.Select(operator, names.fromString("GE")));
        opmap.put(JCTree.Tag.LE, treeMaker.Select(operator, names.fromString("LE")));
        opmap.put(JCTree.Tag.GT, treeMaker.Select(operator, names.fromString("GT")));
        opmap.put(JCTree.Tag.LT, treeMaker.Select(operator, names.fromString("LT")));
        opmap.put(JCTree.Tag.AND, treeMaker.Select(operator, names.fromString("And")));
        opmap.put(JCTree.Tag.OR, treeMaker.Select(operator, names.fromString("Or")));
        opmap.put(JCTree.Tag.NOT, treeMaker.Select(operator, names.fromString("NOT")));
        opmap.put(JCTree.Tag.PLUS, treeMaker.Select(operator, names.fromString("PLUS")));
        opmap.put(JCTree.Tag.MINUS, treeMaker.Select(operator, names.fromString("MINUS")));
        opmap.put(JCTree.Tag.MUL, treeMaker.Select(operator, names.fromString("MUL")));
        opmap.put(JCTree.Tag.DIV, treeMaker.Select(operator, names.fromString("DIV")));
        opmap.put(JCTree.Tag.MOD, treeMaker.Select(operator, names.fromString("MOD")));

        strOpmap.put("contains", treeMaker.Select(operator, names.fromString("Like")));
        strOpmap.put("startsWith", treeMaker.Select(operator, names.fromString("StartLike")));
        strOpmap.put("endsWith", treeMaker.Select(operator, names.fromString("EndLike")));
        strOpmap.put("equals", treeMaker.Select(operator, names.fromString("EQ")));
        strOpmap.put("in", treeMaker.Select(operator, names.fromString("IN")));

        var iExpression = treeMaker.Select(treeMaker.Ident(names.fromString(expV2)), names.fromString("IExpression"));
        ofmap.put(IExpression.Type.Binary, treeMaker.Select(iExpression, names.fromString("binary")));
        ofmap.put(IExpression.Type.Value, treeMaker.Select(iExpression, names.fromString("value")));
        ofmap.put(IExpression.Type.DbRef, treeMaker.Select(iExpression, names.fromString("dbRef")));
        ofmap.put(IExpression.Type.Unary, treeMaker.Select(iExpression, names.fromString("unary")));
        ofmap.put(IExpression.Type.New, treeMaker.Select(iExpression, names.fromString("New")));
        ofmap.put(IExpression.Type.Mapping, treeMaker.Select(iExpression, names.fromString("mapping")));
        ofmap.put(IExpression.Type.DbFunc, treeMaker.Select(iExpression, names.fromString("dbFunc")));
        ofmap.put(IExpression.Type.Parens, treeMaker.Select(iExpression, names.fromString("parens")));

        typeMap.put(Long.class, treeMaker.Select(treeMaker.Ident(names.fromString("Long")), names.fromString("class")));
        typeMap.put(Double.class, treeMaker.Select(treeMaker.Ident(names.fromString("Double")), names.fromString("class")));

        var dbFunc = treeMaker.Select(treeMaker.Ident(names.fromString(expV2)), names.fromString("DbFuncType"));
        dbFuncMap.put(DbFuncType.Count, treeMaker.Select(dbFunc, names.fromString("Count")));
        dbFuncMap.put(DbFuncType.Sum, treeMaker.Select(dbFunc, names.fromString("Sum")));
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        if (!roundEnv.processingOver())
        {
            var entities = roundEnv.getElementsAnnotatedWith(Entity.class);
            var resolves = roundEnv.getElementsAnnotatedWith(Resolve.class);
            var daos = roundEnv.getElementsAnnotatedWith(Dao.class);

            Map<String, Map<String, JCTree.JCFieldAccess>> returnMap = new HashMap<>();
            Map<String, Map<String, JCTree.JCIdent>> fieldMap = new HashMap<>();
            for (Element element : entities)
            {
                JCTree jcTree = javacTrees.getTree(element);
                treeMaker.pos = jcTree.pos;
                var currentClass = element.getSimpleName() + ".class";

                Map<String, JCTree.JCFieldAccess> rMap = new HashMap<>();
                Map<String, JCTree.JCIdent> fMap = new HashMap<>();
                jcTree.accept(new TreeTranslator()
                {
                    @Override
                    public void visitMethodDef(JCTree.JCMethodDecl tree)
                    {
                        if (tree.getParameters().isEmpty() && tree.getReturnType() != null)
                        {
                            JCTree.JCIdent returnType = null;
                            if (tree.getReturnType() instanceof JCTree.JCPrimitiveTypeTree)
                            {
                                var primitiveType = (JCTree.JCPrimitiveTypeTree) tree.getReturnType();
                                switch (primitiveType.getPrimitiveTypeKind())
                                {
                                    case BOOLEAN:
                                        returnType = treeMaker.Ident(names.fromString("Boolean"));
                                        break;
                                    case BYTE:
                                        returnType = treeMaker.Ident(names.fromString("Byte"));
                                        break;
                                    case SHORT:
                                        returnType = treeMaker.Ident(names.fromString("Short"));
                                        break;
                                    case INT:
                                        returnType = treeMaker.Ident(names.fromString("Integer"));
                                        break;
                                    case LONG:
                                        returnType = treeMaker.Ident(names.fromString("Long"));
                                        break;
                                    case CHAR:
                                        returnType = treeMaker.Ident(names.fromString("Character"));
                                        break;
                                    case FLOAT:
                                        returnType = treeMaker.Ident(names.fromString("Float"));
                                        break;
                                    case DOUBLE:
                                        returnType = treeMaker.Ident(names.fromString("Double"));
                                        break;
                                    case VOID:
                                        returnType = treeMaker.Ident(names.fromString("Void"));
                                        break;
                                }
                            }
                            else
                            {
                                returnType = (JCTree.JCIdent) tree.getReturnType();
                            }
                            var sts = tree.getBody().getStatements();
                            if (sts.get(sts.size() - 1) instanceof JCTree.JCReturn)
                            {
                                var ret = (JCTree.JCReturn) sts.get(sts.size() - 1);
                                if (ret.getExpression() instanceof JCTree.JCIdent)
                                {
                                    fMap.put(tree.getName().toString(), (JCTree.JCIdent) ret.getExpression());
                                    rMap.put(ret.getExpression().toString(), treeMaker.Select(returnType, names.fromString("class")));
                                }
                            }
                        }
                        super.visitMethodDef(tree);
                    }
                });

                returnMap.put(currentClass, rMap);
                fieldMap.put(currentClass, fMap);
            }
            for (Element element : resolves)
            {
                JCTree jcTree = javacTrees.getTree(element);
                treeMaker.pos = jcTree.pos;

                Map<Integer, java.util.List<JCTree.JCFieldAccess>> classMap = new HashMap<>();
                //第一次循环
                jcTree.accept(new TreeTranslator()
                {
                    @Override
                    public void visitApply(JCTree.JCMethodInvocation tree)
                    {
                        JCTree.JCExpression methodSelect = tree.getMethodSelect();
                        if (methodSelect instanceof JCTree.JCFieldAccess)
                        {
                            JCTree.JCFieldAccess fa = (JCTree.JCFieldAccess) methodSelect;
                            var name = fa.getIdentifier().toString();
                            if (name.equals("query") || name.equals("delete") || name.equals("update")
                                    || name.equals("innerJoin") || name.equals("leftJoin") || name.equals("rightJoin")
                                    || name.equals("fullJoin"))
                            {
                                if (!classMap.containsKey(tree.getStartPosition()))
                                {
                                    classMap.put(tree.getStartPosition(), new ArrayList<>());
                                }
                                var tmp = classMap.get(tree.getStartPosition());
                                for (var clazz : tree.getArguments())
                                {
                                    tmp.add((JCTree.JCFieldAccess) clazz);
                                }
                            }
                        }
                        super.visitApply(tree);
                    }
                });

                //第二次循环
                jcTree.accept(new TreeTranslator()
                {
                    @Override
                    public void visitApply(JCTree.JCMethodInvocation tree)
                    {
                        if (classMap.containsKey(tree.getStartPosition()))
                        {
                            JCTree.JCExpression methodSelect = tree.getMethodSelect();
                            if (methodSelect instanceof JCTree.JCFieldAccess)
                            {
                                var fa = (JCTree.JCFieldAccess) methodSelect;
                                var name = fa.getIdentifier().toString();
                                switch (name)
                                {
                                    case "If":
                                    {
                                        var lambda = (JCTree.JCLambda) tree.getArguments().get(1);
                                        If(lambda, tree, classMap, fieldMap);
                                        break;
                                    }
                                    case "IfElse":
                                    {
                                        var lambdaIf = (JCTree.JCLambda) tree.getArguments().get(1);
                                        var lambdaElse = (JCTree.JCLambda) tree.getArguments().get(2);
                                        If(lambdaIf, tree, classMap, fieldMap);
                                        If(lambdaElse, tree, classMap, fieldMap);
                                        break;
                                    }
                                    case "where":
                                    case "on":
                                    {
                                        where(tree.getStartPosition(), tree, classMap, fieldMap);
                                        break;
                                    }
                                    case "select":
                                    case "selectDistinct":
                                    {
                                        select(tree, classMap, fieldMap, returnMap);
                                        break;
                                    }
                                    case "orderBy":
                                    case "descOrderBy":
                                    {
                                        orderBy(tree.getStartPosition(), tree, classMap, fieldMap);
                                        break;
                                    }
                                }
                            }
                        }
                        super.visitApply(tree);
                    }
                });
            }
            for (Element element : daos)
            {
                JCTree jcTree = javacTrees.getTree(element);
                treeMaker.pos = jcTree.pos;

                Map<Integer, java.util.List<JCTree.JCFieldAccess>> classMap = new HashMap<>();
                //第一次循环
                jcTree.accept(new TreeTranslator()
                {
                    @Override
                    public void visitApply(JCTree.JCMethodInvocation tree)
                    {
                        JCTree.JCExpression methodSelect = tree.getMethodSelect();
                        if (methodSelect instanceof JCTree.JCFieldAccess)
                        {
                            JCTree.JCFieldAccess fa = (JCTree.JCFieldAccess) methodSelect;
                            var name = fa.getIdentifier().toString();
                            if (name.equals("query") || name.equals("delete") || name.equals("update")
                                    || name.equals("innerJoin") || name.equals("leftJoin") || name.equals("rightJoin")
                                    || name.equals("fullJoin"))
                            {
                                if (!classMap.containsKey(tree.getStartPosition()))
                                {
                                    classMap.put(tree.getStartPosition(), new ArrayList<>());
                                }
                                var tmp = classMap.get(tree.getStartPosition());
                                for (var clazz : tree.getArguments())
                                {
                                    tmp.add((JCTree.JCFieldAccess) clazz);
                                }
                            }
                        }
                        super.visitApply(tree);
                    }
                });

                //第二次循环
                jcTree.accept(new TreeTranslator()
                {
                    @Override
                    public void visitApply(JCTree.JCMethodInvocation tree)
                    {
                        if (classMap.containsKey(tree.getStartPosition()))
                        {
                            JCTree.JCExpression methodSelect = tree.getMethodSelect();
                            if (methodSelect instanceof JCTree.JCFieldAccess)
                            {
                                var fa = (JCTree.JCFieldAccess) methodSelect;
                                var name = fa.getIdentifier().toString();
                                switch (name)
                                {
                                    case "If":
                                    {
                                        var lambda = (JCTree.JCLambda) tree.getArguments().get(1);
                                        If(lambda, tree, classMap, fieldMap);
                                        break;
                                    }
                                    case "IfElse":
                                    {
                                        var lambdaIf = (JCTree.JCLambda) tree.getArguments().get(1);
                                        var lambdaElse = (JCTree.JCLambda) tree.getArguments().get(2);
                                        If(lambdaIf, tree, classMap, fieldMap);
                                        If(lambdaElse, tree, classMap, fieldMap);
                                        break;
                                    }
                                    case "where":
                                    case "on":
                                    {
                                        where(tree.getStartPosition(), tree, classMap, fieldMap);
                                        break;
                                    }
                                    case "select":
                                    case "selectDistinct":
                                    {
                                        select(tree, classMap, fieldMap, returnMap);
                                        break;
                                    }
                                    case "orderBy":
                                    case "descOrderBy":
                                    {
                                        orderBy(tree.getStartPosition(), tree, classMap, fieldMap);
                                        break;
                                    }
                                }
                            }
                        }
                        super.visitApply(tree);
                    }
                });
            }
        }
        return true;
    }

    private void If(JCTree.JCLambda lambda, JCTree.JCMethodInvocation tree, Map<Integer, java.util.List<JCTree.JCFieldAccess>> classMap, Map<String, Map<String, JCTree.JCIdent>> fieldMap)
    {
        if (lambda.getBodyKind() == LambdaExpressionTree.BodyKind.STATEMENT)
        {
            for (var statement : ((JCTree.JCBlock) lambda.getBody()).getStatements())
            {
                var expression = ((JCTree.JCExpressionStatement) statement).getExpression();
                if (expression instanceof JCTree.JCMethodInvocation)
                {
                    var methodInvocation = (JCTree.JCMethodInvocation) expression;
                    var select = (JCTree.JCFieldAccess) methodInvocation.getMethodSelect();
                    switch (select.getIdentifier().toString())
                    {
                        case "where":
                            where(tree.getStartPosition(), methodInvocation, classMap, fieldMap);
                            break;
                        case "orderBy":
                            orderBy(tree.getStartPosition(), methodInvocation, classMap, fieldMap);
                            break;
                    }
                }

            }
        }
    }

    private void where(int startPosition, JCTree.JCMethodInvocation tree, Map<Integer, java.util.List<JCTree.JCFieldAccess>> classMap, Map<String, Map<String, JCTree.JCIdent>> fieldMap)
    {
        var arg = tree.getArguments().get(0);
        if (arg instanceof JCTree.JCLambda)
        {
            var lambda = (JCTree.JCLambda) arg;
            if (lambda.getBodyKind() == LambdaExpressionTree.BodyKind.EXPRESSION)
            {
                java.util.List<String> names = new ArrayList<>();
                for (VariableTree parameter : lambda.getParameters())
                {
                    names.add(parameter.getName().toString());
                }
                var resolved = doResolve((JCTree.JCExpression) lambda.getBody(), names, classMap.get(startPosition), fieldMap);
                if (resolved != null)
                {
                    resolved.setPos(arg.getPreferredPosition());
                    tree.args = List.of(resolved);
                }
                else
                {
                    throw new RuntimeException(tree.toString());
                }
            }
        }
    }

    private void select(JCTree.JCMethodInvocation tree, Map<Integer, java.util.List<JCTree.JCFieldAccess>> classMap, Map<String, Map<String, JCTree.JCIdent>> fieldMap, Map<String, Map<String, JCTree.JCFieldAccess>> returnMap)
    {
        var arg = tree.getArguments().get(0);
        if (arg instanceof JCTree.JCLambda)
        {
            var lambda = (JCTree.JCLambda) arg;
            if (lambda.getBodyKind() == LambdaExpressionTree.BodyKind.EXPRESSION)
            {
                java.util.List<String> names = new ArrayList<>();
                for (VariableTree parameter : lambda.getParameters())
                {
                    names.add(parameter.getName().toString());
                }
                var resolved = doResolve((JCTree.JCExpression) lambda.getBody(), names, classMap.get(tree.getStartPosition()), fieldMap);
                if (resolved != null)
                {
                    if (resolved.getMethodSelect().equals(ofmap.get(IExpression.Type.New)))
                    {
                        resolved.setPos(arg.getPreferredPosition());
                        tree.args = List.of(resolved);
                    }
                    else if (resolved.getMethodSelect().equals(ofmap.get(IExpression.Type.DbRef)))
                    {
                        var index = (JCTree.JCLiteral) resolved.getArguments().get(0);
                        var dbRef = (JCTree.JCLiteral) resolved.getArguments().get(1);
                        if (dbRef.getValue().equals(""))
                        {
                            var clazz = classMap.get(tree.getStartPosition()).get((Integer) index.getValue());
                            var methodInvocation = treeMaker.Apply(List.nil(), ofmap.get(IExpression.Type.New), List.of(clazz));
                            methodInvocation.setPos(arg.getPreferredPosition());
                            tree.args = List.of(methodInvocation);
                        }
                        else
                        {
                            var clazzName = classMap.get(tree.getStartPosition()).get((Integer) index.getValue()).toString();
                            var clazz = returnMap.get(clazzName).get((String) dbRef.getValue());
                            var methodInvocation = treeMaker.Apply(List.nil(), ofmap.get(IExpression.Type.New), List.of(clazz, resolved));
                            methodInvocation.setPos(arg.getPreferredPosition());
                            tree.args = List.of(methodInvocation);
                        }
                    }
                    else if (resolved.getMethodSelect().equals(ofmap.get(IExpression.Type.DbFunc)))
                    {
                        var dbFunc = (JCTree.JCFieldAccess) resolved.getArguments().get(0);
                        switch (dbFunc.getIdentifier().toString())
                        {
                            case "Count":
                            {
                                var methodInvocation = treeMaker.Apply(List.nil(), ofmap.get(IExpression.Type.New), List.of(typeMap.get(Long.class), resolved));
                                methodInvocation.setPos(arg.getPreferredPosition());
                                tree.args = List.of(methodInvocation);
                                break;
                            }
                            case "Sum":
                            {
                                var methodInvocation = treeMaker.Apply(List.nil(), ofmap.get(IExpression.Type.New), List.of(typeMap.get(Double.class), resolved));
                                methodInvocation.setPos(arg.getPreferredPosition());
                                tree.args = List.of(methodInvocation);
                                break;
                            }
                        }
                    }
                    else if (resolved.getMethodSelect().equals(ofmap.get(IExpression.Type.Value)))
                    {
                        throw new UnsupportedFeatureException(tree);
                    }
                }
                else
                {
                    throw new RuntimeException(tree.toString());
                }
            }
        }
    }

    private void orderBy(int startPosition, JCTree.JCMethodInvocation tree, Map<Integer, java.util.List<JCTree.JCFieldAccess>> classMap, Map<String, Map<String, JCTree.JCIdent>> fieldMap)
    {
        var arg = tree.getArguments().get(0);
        if (arg instanceof JCTree.JCLambda)
        {
            var lambda = (JCTree.JCLambda) arg;
            if (lambda.getBodyKind() == LambdaExpressionTree.BodyKind.EXPRESSION)
            {
                java.util.List<String> names = new ArrayList<>();
                for (VariableTree parameter : lambda.getParameters())
                {
                    names.add(parameter.getName().toString());
                }
                var resolved = doResolve((JCTree.JCExpression) lambda.getBody(), names, classMap.get(startPosition), fieldMap);
                if (resolved != null)
                {
                    resolved.setPos(arg.getPreferredPosition());
                    tree.args = List.of(resolved);
                }
                else
                {
                    throw new RuntimeException(tree.toString());
                }
            }
        }
    }

    private JCTree.JCMethodInvocation doResolve(JCTree.JCExpression expression, java.util.List<String> names, java.util.List<JCTree.JCFieldAccess> classes, Map<String, Map<String, JCTree.JCIdent>> fieldMap)
    {
        if (expression instanceof JCTree.JCMethodInvocation)
        {
            return doResolveInvoke((JCTree.JCMethodInvocation) expression, names, classes, fieldMap);
        }
        else if (expression instanceof JCTree.JCIdent)
        {
            return doResolveIdent((JCTree.JCIdent) expression, names, classes, fieldMap);
        }
        else if (expression instanceof JCTree.JCLiteral)
        {
            return doResolveLiteral((JCTree.JCLiteral) expression);
        }
        else if (expression instanceof JCTree.JCBinary)
        {
            return doResolveBinary((JCTree.JCBinary) expression, names, classes, fieldMap);
        }
        else if (expression instanceof JCTree.JCUnary)
        {
            var unary = (JCTree.JCUnary) expression;
            var done = doResolve(unary.getExpression(), names, classes, fieldMap);
            return treeMaker.Apply(List.nil(), ofmap.get(IExpression.Type.Unary), List.of(done, opmap.get(unary.getTag())));
        }
        else if (expression instanceof JCTree.JCTypeCast)
        {
            return doResolve(((JCTree.JCTypeCast) expression).getExpression(), names, classes, fieldMap);
        }
        else if (expression instanceof JCTree.JCNewClass)
        {
            return doResolveNewClass((JCTree.JCNewClass) expression, names, classes, fieldMap);
        }
        else if (expression instanceof JCTree.JCParens)
        {
            return doResolveParens((JCTree.JCParens) expression, names, classes, fieldMap);
        }
        return null;
    }

    private JCTree.JCMethodInvocation doResolveIdent(JCTree.JCIdent ident, java.util.List<String> names, java.util.List<JCTree.JCFieldAccess> classes, Map<String, Map<String, JCTree.JCIdent>> fieldMap)
    {
        if (names.contains(ident.getName().toString()))
        {
            var index = names.indexOf(ident.getName().toString());
            var clazz = classes.get(index);
            var map = fieldMap.get(clazz.toString());
            return treeMaker.Apply(List.nil(), ofmap.get(IExpression.Type.DbRef), List.of(treeMaker.Literal(index), treeMaker.Literal("")));
        }
        else
        {
            return treeMaker.Apply(List.nil(), ofmap.get(IExpression.Type.Value), List.of(ident));
        }
    }

    private JCTree.JCMethodInvocation doResolveInvoke(JCTree.JCMethodInvocation methodInvocation, java.util.List<String> names, java.util.List<JCTree.JCFieldAccess> classes, Map<String, Map<String, JCTree.JCIdent>> fieldMap)
    {
        var invoke = methodInvocation.getMethodSelect();
        if (invoke instanceof JCTree.JCIdent)
        {
            var ident = (JCTree.JCIdent) invoke;
            switch (ident.getName().toString())
            {
                case "Count":
                {
                    var value = doResolve(methodInvocation.getArguments().get(0), names, classes, fieldMap);
                    return treeMaker.Apply(List.nil(), ofmap.get(IExpression.Type.DbFunc), List.of(dbFuncMap.get(DbFuncType.Count), value));
                }
                case "Sum":
                {
                    var value = doResolve(methodInvocation.getArguments().get(0), names, classes, fieldMap);
                    return treeMaker.Apply(List.nil(), ofmap.get(IExpression.Type.DbFunc), List.of(dbFuncMap.get(DbFuncType.Sum), value));
                }
            }
        }
        else if (invoke instanceof JCTree.JCFieldAccess)
        {
            var fieldAccess = (JCTree.JCFieldAccess) invoke;
            var identifier = fieldAccess.getIdentifier();
            if (fieldAccess.getExpression() instanceof JCTree.JCMethodInvocation)
            {
                var fieldAccessExpression = (JCTree.JCMethodInvocation) fieldAccess.getExpression();
                switch (identifier.toString())
                {
                    case "contains":
                    case "startsWith":
                    case "endsWith":
                    case "equals":
                        var op = strOpmap.get(identifier.toString());
                        var left = doResolveInvoke(fieldAccessExpression, names, classes, fieldMap);
                        var right = doResolve(methodInvocation.getArguments().get(0), names, classes, fieldMap);
                        return treeMaker.Apply(List.nil(), ofmap.get(IExpression.Type.Binary), List.of(left, right, op));
                }
                return doResolveInvoke(fieldAccessExpression, names, classes, fieldMap);
            }
            else if (fieldAccess.getExpression() instanceof JCTree.JCIdent)
            {
                var ident = (JCTree.JCIdent) fieldAccess.getExpression();
                if (names.contains(ident.getName().toString()))
                {
                    var index = names.indexOf(ident.getName().toString());
                    var clazz = classes.get(index);
                    var map = fieldMap.get(clazz.toString());
                    var fieldName = map.get(identifier.toString());
                    return treeMaker.Apply(List.nil(), ofmap.get(IExpression.Type.DbRef), List.of(treeMaker.Literal(index), treeMaker.Literal(fieldName.toString())));
                }
                else
                {
                    if (!methodInvocation.getArguments().isEmpty())
                    {
                        if (identifier.toString().equals("contains"))
                        {
                            var op = strOpmap.get("in");
                            var left = doResolve(methodInvocation.getArguments().get(0), names, classes, fieldMap);
                            var right = treeMaker.Apply(List.nil(), ofmap.get(IExpression.Type.Value), List.of(ident));
                            return treeMaker.Apply(List.nil(), ofmap.get(IExpression.Type.Binary), List.of(left, right, op));
                        }
                    }
                    else
                    {
                        return treeMaker.Apply(List.nil(), ofmap.get(IExpression.Type.Value), List.of(methodInvocation));
                    }
                }
            }
            else if (fieldAccess.getExpression() instanceof JCTree.JCFieldAccess)
            {
                //todo
            }
        }
        return null;
    }

    private JCTree.JCMethodInvocation doResolveLiteral(JCTree.JCLiteral literal)
    {
        return treeMaker.Apply(List.nil(), ofmap.get(IExpression.Type.Value), List.of(literal));
    }

    private JCTree.JCMethodInvocation doResolveNewClass(JCTree.JCNewClass newClass, java.util.List<String> names, java.util.List<JCTree.JCFieldAccess> classes, Map<String, Map<String, JCTree.JCIdent>> fieldMap)
    {
        ListBuffer<JCTree.JCExpression> listBuffer = new ListBuffer<>();
        listBuffer.append(treeMaker.Select(newClass.getIdentifier(), this.names.fromString("class")));
        if (newClass.getClassBody() != null && !newClass.getClassBody().getMembers().isEmpty())
        {
            var member = (JCTree.JCBlock) newClass.getClassBody().getMembers().get(0);
            for (var statement : member.getStatements())
            {
                var expressionStatement = (JCTree.JCExpressionStatement) statement;
                if (expressionStatement.getExpression() instanceof JCTree.JCMethodInvocation)
                {
                    var methodInvocation = (JCTree.JCMethodInvocation) expressionStatement.getExpression();
                    if (methodInvocation.getMethodSelect() instanceof JCTree.JCIdent && !methodInvocation.getArguments().isEmpty())
                    {
                        var ident = (JCTree.JCIdent) methodInvocation.getMethodSelect();
                        var source = treeMaker.Literal(ident.getName().toString());
                        var value = doResolve(methodInvocation.getArguments().get(0), names, classes, fieldMap);
                        var mapping = treeMaker.Apply(List.nil(), ofmap.get(IExpression.Type.Mapping), List.of(source, value));
                        listBuffer.append(mapping);
                    }
                }
            }
        }
        return treeMaker.Apply(List.nil(), ofmap.get(IExpression.Type.New), listBuffer.toList());
    }

    private JCTree.JCMethodInvocation doResolveBinary(JCTree.JCBinary binary, java.util.List<String> names, java.util.List<JCTree.JCFieldAccess> classes, Map<String, Map<String, JCTree.JCIdent>> fieldMap)
    {
        var left = doResolve(binary.getLeftOperand(), names, classes, fieldMap);
        var right = doResolve(binary.getRightOperand(), names, classes, fieldMap);
        var op = opmap.get(binary.getTag());
        return treeMaker.Apply(List.nil(), ofmap.get(IExpression.Type.Binary), List.of(left, right, op));
    }

    private JCTree.JCMethodInvocation doResolveParens(JCTree.JCParens parens, java.util.List<String> names, java.util.List<JCTree.JCFieldAccess> classes, Map<String, Map<String, JCTree.JCIdent>> fieldMap)
    {
        var done = doResolve(parens.getExpression(), names, classes, fieldMap);
        return treeMaker.Apply(List.nil(), ofmap.get(IExpression.Type.Parens), List.of(done));
    }
}
