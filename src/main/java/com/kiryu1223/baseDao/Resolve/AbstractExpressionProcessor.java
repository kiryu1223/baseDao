package com.kiryu1223.baseDao.Resolve;

import com.kiryu1223.baseDao.JProperty.GetSetHelper;
import com.kiryu1223.baseDao.JProperty.GetterSetterTranslator;
import com.kiryu1223.baseDao.ExpressionV2.DbFuncType;
import com.kiryu1223.baseDao.ExpressionV2.IExpression;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Name;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import java.lang.annotation.Annotation;
import java.util.*;

public abstract class AbstractExpressionProcessor extends AbstractProcessor
{
    private JavacTrees javacTrees;
    private Context context;
    private TreeMaker treeMaker;
    private Names names;
    private final Map<IExpression.Type, JCTree.JCFieldAccess> expressionMap = new HashMap<>();
    private final Map<JCTree.Tag, JCTree.JCFieldAccess> opMap = new HashMap<>();
    private final Map<String, JCTree.JCFieldAccess> strOpmap = new HashMap<>();
    private final Map<Class<?>, JCTree.JCFieldAccess> typeMap = new HashMap<>();
    private final Map<DbFuncType, JCTree.JCFieldAccess> dbFuncMap = new HashMap<>();
    private final Map<Name, java.util.List<Class<? extends IExpression>>> methodExpressionTypeMap = new HashMap<>();
    private final Map<Name, Name> classMappingMethodMap = new HashMap<>();
    Map<Name, Map<Name, JCTree.JCVariableDecl>> classPrivateFieldsMap = new HashMap<>();

    @Override
    public Set<String> getSupportedAnnotationTypes()
    {
        SupportedAnnotationTypes sat = new SupportedAnnotationTypes()
        {
            @Override
            public Class<? extends Annotation> annotationType()
            {
                return SupportedAnnotationTypes.class;
            }

            @Override
            public String[] value()
            {
                return new String[]{"*"};
            }
        };
        var initialized = isInitialized();
        var stripModulePrefixes = initialized && processingEnv.getSourceVersion().compareTo(SourceVersion.RELEASE_8) <= 0;
        return arrayToSet(sat.value(), stripModulePrefixes);

    }

    private static Set<String> arrayToSet(String[] array, boolean stripModulePrefixes)
    {
        assert array != null;
        Set<String> set = new HashSet<>(array.length);
        for (String s : array)
        {
            if (stripModulePrefixes)
            {
                int index = s.indexOf('/');
                if (index != -1)
                    s = s.substring(index + 1);
            }
            set.add(s);
        }
        return Collections.unmodifiableSet(set);
    }

    @Override
    public SourceVersion getSupportedSourceVersion()
    {
        if (getClass().isAnnotationPresent(SupportedSourceVersion.class))
        {
            return getClass().getAnnotation(SupportedSourceVersion.class).value();
        }
        return SourceVersion.RELEASE_11;
    }

    public abstract void registerManager();

    protected void register(Class<?> clazz)
    {
        for (var method : clazz.getMethods())
        {
            java.util.List<Class<? extends IExpression>> list = new ArrayList<>();
            for (var parameter : method.getParameters())
            {
                var type = parameter.getType();
                if (parameter.isAnnotationPresent(Expression.class) && type.isInterface()
                        && type.getMethods().length == 1)
                {
                    var expression = parameter.getAnnotation(Expression.class);
                    list.add(expression.value());
                }
            }
            if (!list.isEmpty())
            {
                methodExpressionTypeMap.put(names.fromString(method.getName()), list);
            }
        }
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv)
    {
        super.init(processingEnv);
        javacTrees = JavacTrees.instance(processingEnv);
        context = ((JavacProcessingEnvironment) processingEnv).getContext();
        treeMaker = TreeMaker.instance(context);
        names = Names.instance(context);
        registerManager();
        GetSetHelper.setNames(names);
        var expV2 = "com.kiryu1223.baseDao.ExpressionV2";

        var operator = treeMaker.Select(treeMaker.Ident(names.fromString(expV2)), names.fromString("Operator"));
        opMap.put(JCTree.Tag.EQ, treeMaker.Select(operator, names.fromString("EQ")));
        opMap.put(JCTree.Tag.NE, treeMaker.Select(operator, names.fromString("NE")));
        opMap.put(JCTree.Tag.GE, treeMaker.Select(operator, names.fromString("GE")));
        opMap.put(JCTree.Tag.LE, treeMaker.Select(operator, names.fromString("LE")));
        opMap.put(JCTree.Tag.GT, treeMaker.Select(operator, names.fromString("GT")));
        opMap.put(JCTree.Tag.LT, treeMaker.Select(operator, names.fromString("LT")));
        opMap.put(JCTree.Tag.AND, treeMaker.Select(operator, names.fromString("And")));
        opMap.put(JCTree.Tag.OR, treeMaker.Select(operator, names.fromString("Or")));
        opMap.put(JCTree.Tag.NOT, treeMaker.Select(operator, names.fromString("NOT")));
        opMap.put(JCTree.Tag.PLUS, treeMaker.Select(operator, names.fromString("PLUS")));
        opMap.put(JCTree.Tag.MINUS, treeMaker.Select(operator, names.fromString("MINUS")));
        opMap.put(JCTree.Tag.MUL, treeMaker.Select(operator, names.fromString("MUL")));
        opMap.put(JCTree.Tag.DIV, treeMaker.Select(operator, names.fromString("DIV")));
        opMap.put(JCTree.Tag.MOD, treeMaker.Select(operator, names.fromString("MOD")));

        strOpmap.put("contains", treeMaker.Select(operator, names.fromString("Like")));
        strOpmap.put("startsWith", treeMaker.Select(operator, names.fromString("StartLike")));
        strOpmap.put("endsWith", treeMaker.Select(operator, names.fromString("EndLike")));
        strOpmap.put("equals", treeMaker.Select(operator, names.fromString("EQ")));
        strOpmap.put("in", treeMaker.Select(operator, names.fromString("IN")));

        var iExpression = treeMaker.Select(treeMaker.Ident(names.fromString(expV2)), names.fromString("IExpression"));
        expressionMap.put(IExpression.Type.Binary, treeMaker.Select(iExpression, names.fromString("binary")));
        expressionMap.put(IExpression.Type.Value, treeMaker.Select(iExpression, names.fromString("value")));
        expressionMap.put(IExpression.Type.Unary, treeMaker.Select(iExpression, names.fromString("unary")));
        expressionMap.put(IExpression.Type.New, treeMaker.Select(iExpression, names.fromString("New")));
        expressionMap.put(IExpression.Type.Mapping, treeMaker.Select(iExpression, names.fromString("mapping")));
        expressionMap.put(IExpression.Type.Mappings, treeMaker.Select(iExpression, names.fromString("mappings")));
        expressionMap.put(IExpression.Type.DbFunc, treeMaker.Select(iExpression, names.fromString("dbFunc")));
        expressionMap.put(IExpression.Type.Parens, treeMaker.Select(iExpression, names.fromString("parens")));
        expressionMap.put(IExpression.Type.FieldSelect, treeMaker.Select(iExpression, names.fromString("fieldSelect")));
        expressionMap.put(IExpression.Type.MethodCall, treeMaker.Select(iExpression, names.fromString("methodCall")));
        expressionMap.put(IExpression.Type.Reference, treeMaker.Select(iExpression, names.fromString("reference")));

        typeMap.put(Long.class, treeMaker.Select(treeMaker.Ident(names.fromString("Long")), names.fromString("class")));
        typeMap.put(Double.class, treeMaker.Select(treeMaker.Ident(names.fromString("Double")), names.fromString("class")));

        var dbFunc = treeMaker.Select(treeMaker.Ident(names.fromString(expV2)), names.fromString("DbFuncType"));
        dbFuncMap.put(DbFuncType.Count, treeMaker.Select(dbFunc, names.fromString("Count")));
        dbFuncMap.put(DbFuncType.Sum, treeMaker.Select(dbFunc, names.fromString("Sum")));

        System.out.println("staticExpressionTree & JProperty 启动!");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        if (!roundEnv.processingOver())
        {
            var rootData = roundEnv.getRootElements();

            //表达式树流程
            for (var root : rootData)
            {
                if (!root.getKind().equals(ElementKind.CLASS)) continue;
                var jcClassDecl = javacTrees.getTree((TypeElement) root);
                treeMaker.pos = jcClassDecl.getPreferredPosition();
                jcClassDecl.accept(new ExpressionTranslator(
                        methodExpressionTypeMap,
                        opMap,
                        expressionMap,
                        treeMaker,
                        names));
            }

            //获取全部类私有属性流程
            for (var rootElement : rootData)
            {
                var jcTree = javacTrees.getTree(rootElement);
                treeMaker.pos = jcTree.getPreferredPosition();
                Map<Name, JCTree.JCVariableDecl> privateFields = new HashMap<>();
                jcTree.accept(new TreeTranslator()
                {
                    @Override
                    public void visitClassDef(JCTree.JCClassDecl tree)
                    {
                        for (var member : tree.getMembers())
                        {
                            if (member.getKind() == Tree.Kind.VARIABLE)
                            {
                                var variableDecl = (JCTree.JCVariableDecl) member;
                                if (!variableDecl.getModifiers().getFlags().contains(Modifier.PUBLIC))
                                {
                                    privateFields.put(variableDecl.getName(), variableDecl);
                                }
                            }
                        }
                        super.visitClassDef(tree);
                    }
                });
                if (!privateFields.isEmpty())
                {
                    classPrivateFieldsMap.put(((JCTree.JCClassDecl) jcTree).getSimpleName(), privateFields);
                }
            }

            //替换流程
            for (var rootElement : rootData)
            {
                var jcTree = javacTrees.getTree((TypeElement) rootElement);
                treeMaker.pos = jcTree.getPreferredPosition();
                var currentName = jcTree.getSimpleName();
                Set<Integer> poses = new HashSet<>();
                jcTree.accept(new TreeTranslator()
                {
                    @Override
                    public void visitMethodDef(JCTree.JCMethodDecl methodDecl)
                    {
                        var pos = methodDecl.getPreferredPosition();
                        if (poses.contains(pos))
                        {
                            super.visitMethodDef(methodDecl);
                            return;
                        }

                        Map<Name, Name> nameMap = new HashMap<>();
                        methodDecl.accept(new TreeTranslator()
                        {
                            @Override
                            public void visitVarDef(JCTree.JCVariableDecl varDef)
                            {
                                if (varDef.getType() != null)
                                {
                                    var type = varDef.getType();
                                    if (type instanceof JCTree.JCIdent)
                                    {
                                        var name = ((JCTree.JCIdent) type).getName();
                                        if (classPrivateFieldsMap.containsKey(name))
                                        {
                                            nameMap.put(varDef.getName(), name);
                                        }
                                    }
                                    else if (type instanceof JCTree.JCTypeApply)
                                    {
                                        var typeApply = (JCTree.JCTypeApply) type;
                                        var name = ((JCTree.JCIdent) typeApply.getType()).getName();
                                        if (classPrivateFieldsMap.containsKey(name))
                                        {
                                            nameMap.put(varDef.getName(), ((JCTree.JCIdent) typeApply.getType()).getName());
                                        }
                                    }
                                }
                                else
                                {
                                    var initializer = varDef.getInitializer();
                                    if (initializer != null)
                                    {
                                        var name = GetSetHelper.getType(varDef.getInitializer(), nameMap);
                                        if (name != null && classPrivateFieldsMap.containsKey(name))
                                        {
                                            nameMap.put(varDef.getName(), name);
                                        }
                                    }
                                }
                                super.visitVarDef(varDef);
                            }
                        });

                        methodDecl.accept(new GetterSetterTranslator(
                                classPrivateFieldsMap,
                                nameMap,
                                treeMaker,
                                names,
                                currentName
                        ));

                        super.visitMethodDef(methodDecl);

                        poses.add(pos);
                    }
                });
            }
        }
        return false;
    }
//
//    private void start(Set<? extends Element> elements, Map<String, Map<String, JCTree.JCFieldAccess>> returnMap, Map<String, Map<String, JCTree.JCIdent>> fieldMap)
//    {
//        for (Element element : elements)
//        {
//            var jcTree = javacTrees.getTree(element);
//            treeMaker.pos = jcTree.pos;
//
//            Map<Integer, java.util.List<JCTree.JCFieldAccess>> classMap = new HashMap<>();
//            //第一次循环
//            jcTree.accept(new TreeTranslator()
//            {
//                @Override
//                public void visitApply(JCTree.JCMethodInvocation tree)
//                {
//                    JCTree.JCExpression methodSelect = tree.getMethodSelect();
//                    if (methodSelect instanceof JCTree.JCFieldAccess)
//                    {
//                        JCTree.JCFieldAccess fa = (JCTree.JCFieldAccess) methodSelect;
//                        var name = fa.getIdentifier().toString();
//                        if (name.equals("query") || name.equals("delete") || name.equals("update")
//                                || name.equals("innerJoin") || name.equals("leftJoin") || name.equals("rightJoin")
//                                || name.equals("fullJoin"))
//                        {
//                            if (!classMap.containsKey(tree.getStartPosition()))
//                            {
//                                classMap.put(tree.getStartPosition(), new ArrayList<>());
//                            }
//                            var tmp = classMap.get(tree.getStartPosition());
//                            for (var clazz : tree.getArguments())
//                            {
//                                tmp.add((JCTree.JCFieldAccess) clazz);
//                            }
//                        }
//                    }
//                    super.visitApply(tree);
//                }
//            });
//
//            //第二次循环
//            jcTree.accept(new TreeTranslator()
//            {
//                @Override
//                public void visitApply(JCTree.JCMethodInvocation tree)
//                {
//                    if (classMap.containsKey(tree.getStartPosition()))
//                    {
//                        JCTree.JCExpression methodSelect = tree.getMethodSelect();
//                        if (methodSelect instanceof JCTree.JCFieldAccess)
//                        {
//                            var fa = (JCTree.JCFieldAccess) methodSelect;
//                            var name = fa.getIdentifier().toString();
//                            switch (name)
//                            {
//                                case "If":
//                                {
//                                    var lambda = (JCTree.JCLambda) tree.getArguments().get(1);
//                                    If(lambda, tree, classMap, fieldMap);
//                                    break;
//                                }
//                                case "IfElse":
//                                {
//                                    var lambdaIf = (JCTree.JCLambda) tree.getArguments().get(1);
//                                    var lambdaElse = (JCTree.JCLambda) tree.getArguments().get(2);
//                                    If(lambdaIf, tree, classMap, fieldMap);
//                                    If(lambdaElse, tree, classMap, fieldMap);
//                                    break;
//                                }
//                                case "where":
//                                case "on":
//                                {
//                                    where(tree.getStartPosition(), tree, classMap, fieldMap);
//                                    break;
//                                }
//                                case "select":
//                                case "selectDistinct":
//                                {
//                                    select(tree, classMap, fieldMap, returnMap);
//                                    break;
//                                }
//                                case "orderBy":
//                                case "descOrderBy":
//                                {
//                                    orderBy(tree.getStartPosition(), tree, classMap, fieldMap);
//                                    break;
//                                }
//                                case "pushData":
//                                {
//                                    pushData(tree.getStartPosition(), tree, classMap, fieldMap);
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                    super.visitApply(tree);
//                }
//            });
//        }
//    }
//
//    private void pushData(int startPosition, JCTree.JCMethodInvocation tree, Map<Integer, java.util.List<JCTree.JCFieldAccess>> classMap, Map<String, Map<String, JCTree.JCIdent>> fieldMap)
//    {
//        var arg = tree.getArguments().get(0);
//        if (arg instanceof JCTree.JCLambda)
//        {
//            var lambda = (JCTree.JCLambda) arg;
//            java.util.List<String> names = new ArrayList<>();
//            for (VariableTree parameter : lambda.getParameters())
//            {
//                names.add(parameter.getName().toString());
//            }
//            var resolved = doResolve(lambda.getBody(), names, classMap.get(startPosition), fieldMap);
//            if (resolved != null)
//            {
//                if (resolved.getMethodSelect().equals(expressionMap.get(IExpression.Type.Mapping)))
//                {
//                    resolved = treeMaker.Apply(List.nil(), expressionMap.get(IExpression.Type.Mappings), List.of(resolved));
//                }
//                resolved.setPos(arg.getPreferredPosition());
//                tree.args = List.of(resolved);
//            }
//            else
//            {
//                throw new RuntimeException(tree.toString());
//            }
//        }
//    }
//
//    private void If(JCTree.JCLambda lambda, JCTree.JCMethodInvocation tree, Map<Integer, java.util.List<JCTree.JCFieldAccess>> classMap, Map<String, Map<String, JCTree.JCIdent>> fieldMap)
//    {
//        if (lambda.getBodyKind() == LambdaExpressionTree.BodyKind.STATEMENT)
//        {
//            for (var statement : ((JCTree.JCBlock) lambda.getBody()).getStatements())
//            {
//                var expression = ((JCTree.JCExpressionStatement) statement).getExpression();
//                if (expression instanceof JCTree.JCMethodInvocation)
//                {
//                    var methodInvocation = (JCTree.JCMethodInvocation) expression;
//                    var select = (JCTree.JCFieldAccess) methodInvocation.getMethodSelect();
//                    switch (select.getIdentifier().toString())
//                    {
//                        case "where":
//                            where(tree.getStartPosition(), methodInvocation, classMap, fieldMap);
//                            break;
//                        case "orderBy":
//                            orderBy(tree.getStartPosition(), methodInvocation, classMap, fieldMap);
//                            break;
//                    }
//                }
//
//            }
//        }
//    }
//
//    private void where(int startPosition, JCTree.JCMethodInvocation tree, Map<Integer, java.util.List<JCTree.JCFieldAccess>> classMap, Map<String, Map<String, JCTree.JCIdent>> fieldMap)
//    {
//        var arg = tree.getArguments().get(0);
//        if (arg instanceof JCTree.JCLambda)
//        {
//            var lambda = (JCTree.JCLambda) arg;
//            if (lambda.getBodyKind() == LambdaExpressionTree.BodyKind.EXPRESSION)
//            {
//                java.util.List<String> names = new ArrayList<>();
//                for (VariableTree parameter : lambda.getParameters())
//                {
//                    names.add(parameter.getName().toString());
//                }
//                var resolved = doResolve(lambda.getBody(), names, classMap.get(startPosition), fieldMap);
//                if (resolved != null)
//                {
//                    resolved.setPos(arg.getPreferredPosition());
//                    tree.args = List.of(resolved);
//                }
//                else
//                {
//                    throw new RuntimeException(tree.toString());
//                }
//            }
//        }
//    }
//
//    private void select(JCTree.JCMethodInvocation tree, Map<Integer, java.util.List<JCTree.JCFieldAccess>> classMap, Map<String, Map<String, JCTree.JCIdent>> fieldMap, Map<String, Map<String, JCTree.JCFieldAccess>> returnMap)
//    {
//        var arg = tree.getArguments().get(0);
//        if (arg instanceof JCTree.JCLambda)
//        {
//            var lambda = (JCTree.JCLambda) arg;
//            if (lambda.getBodyKind() == LambdaExpressionTree.BodyKind.EXPRESSION)
//            {
//                java.util.List<String> names = new ArrayList<>();
//                for (VariableTree parameter : lambda.getParameters())
//                {
//                    names.add(parameter.getName().toString());
//                }
//                var resolved = doResolve(lambda.getBody(), names, classMap.get(tree.getStartPosition()), fieldMap);
//                if (resolved != null)
//                {
//                    if (resolved.getMethodSelect().equals(expressionMap.get(IExpression.Type.New)))
//                    {
//                        resolved.setPos(arg.getPreferredPosition());
//                        tree.args = List.of(resolved);
//                    }
//                    else if (resolved.getMethodSelect().equals(expressionMap.get(IExpression.Type.DbRef)))
//                    {
//                        var index = (JCTree.JCLiteral) resolved.getArguments().get(0);
//                        var dbRef = (JCTree.JCLiteral) resolved.getArguments().get(1);
//                        if (dbRef.getValue().equals(""))
//                        {
//                            var clazz = classMap.get(tree.getStartPosition()).get((Integer) index.getValue());
//                            var methodInvocation = treeMaker.Apply(List.nil(), expressionMap.get(IExpression.Type.New), List.of(clazz));
//                            methodInvocation.setPos(arg.getPreferredPosition());
//                            tree.args = List.of(methodInvocation);
//                        }
//                        else
//                        {
//                            var clazzName = classMap.get(tree.getStartPosition()).get((Integer) index.getValue()).toString();
//                            var clazz = returnMap.get(clazzName).get((String) dbRef.getValue());
//                            var methodInvocation = treeMaker.Apply(List.nil(), expressionMap.get(IExpression.Type.New), List.of(clazz, resolved));
//                            methodInvocation.setPos(arg.getPreferredPosition());
//                            tree.args = List.of(methodInvocation);
//                        }
//                    }
//                    else if (resolved.getMethodSelect().equals(expressionMap.get(IExpression.Type.DbFunc)))
//                    {
//                        var dbFunc = (JCTree.JCFieldAccess) resolved.getArguments().get(0);
//                        switch (dbFunc.getIdentifier().toString())
//                        {
//                            case "Count":
//                            {
//                                var methodInvocation = treeMaker.Apply(List.nil(), expressionMap.get(IExpression.Type.New), List.of(typeMap.get(Long.class), resolved));
//                                methodInvocation.setPos(arg.getPreferredPosition());
//                                tree.args = List.of(methodInvocation);
//                                break;
//                            }
//                            case "Sum":
//                            {
//                                var methodInvocation = treeMaker.Apply(List.nil(), expressionMap.get(IExpression.Type.New), List.of(typeMap.get(Double.class), resolved));
//                                methodInvocation.setPos(arg.getPreferredPosition());
//                                tree.args = List.of(methodInvocation);
//                                break;
//                            }
//                        }
//                    }
//                    else if (resolved.getMethodSelect().equals(expressionMap.get(IExpression.Type.Value)))
//                    {
//                        throw new UnsupportedFeatureException(tree);
//                    }
//                }
//                else
//                {
//                    throw new RuntimeException(tree.toString());
//                }
//            }
//        }
//    }
//
//    private void orderBy(int startPosition, JCTree.JCMethodInvocation tree, Map<Integer, java.util.List<JCTree.JCFieldAccess>> classMap, Map<String, Map<String, JCTree.JCIdent>> fieldMap)
//    {
//        var arg = tree.getArguments().get(0);
//        if (arg instanceof JCTree.JCLambda)
//        {
//            var lambda = (JCTree.JCLambda) arg;
//            if (lambda.getBodyKind() == LambdaExpressionTree.BodyKind.EXPRESSION)
//            {
//                java.util.List<String> names = new ArrayList<>();
//                for (VariableTree parameter : lambda.getParameters())
//                {
//                    names.add(parameter.getName().toString());
//                }
//                var resolved = doResolve(lambda.getBody(), names, classMap.get(startPosition), fieldMap);
//                if (resolved != null)
//                {
//                    resolved.setPos(arg.getPreferredPosition());
//                    tree.args = List.of(resolved);
//                }
//                else
//                {
//                    throw new RuntimeException(tree.toString());
//                }
//            }
//        }
//    }
//
//    private JCTree.JCMethodInvocation doResolve(JCTree expression, java.util.List<String> names, java.util.List<JCTree.JCFieldAccess> classes, Map<String, Map<String, JCTree.JCIdent>> fieldMap)
//    {
//        if (expression instanceof JCTree.JCMethodInvocation)
//        {
//            return doResolveInvoke((JCTree.JCMethodInvocation) expression, names, classes, fieldMap);
//        }
//        else if (expression instanceof JCTree.JCIdent)
//        {
//            return doResolveIdent((JCTree.JCIdent) expression, names, classes, fieldMap);
//        }
//        else if (expression instanceof JCTree.JCLiteral)
//        {
//            return doResolveLiteral((JCTree.JCLiteral) expression);
//        }
//        else if (expression instanceof JCTree.JCBinary)
//        {
//            return doResolveBinary((JCTree.JCBinary) expression, names, classes, fieldMap);
//        }
//        else if (expression instanceof JCTree.JCUnary)
//        {
//            var unary = (JCTree.JCUnary) expression;
//            var done = doResolve(unary.getExpression(), names, classes, fieldMap);
//            return treeMaker.Apply(List.nil(), expressionMap.get(IExpression.Type.Unary), List.of(done, opMap.get(unary.getTag())));
//        }
//        else if (expression instanceof JCTree.JCTypeCast)
//        {
//            return doResolve(((JCTree.JCTypeCast) expression).getExpression(), names, classes, fieldMap);
//        }
//        else if (expression instanceof JCTree.JCNewClass)
//        {
//            return doResolveNewClass((JCTree.JCNewClass) expression, names, classes, fieldMap);
//        }
//        else if (expression instanceof JCTree.JCParens)
//        {
//            return doResolveParens((JCTree.JCParens) expression, names, classes, fieldMap);
//        }
//        else if (expression instanceof JCTree.JCExpressionStatement)
//        {
//            return doResolveExpressionStatement((JCTree.JCExpressionStatement) expression, names, classes, fieldMap);
//        }
//        else if (expression instanceof JCTree.JCBlock)
//        {
//            return doResolveBlock((JCTree.JCBlock) expression, names, classes, fieldMap);
//        }
//        return null;
//    }
//
//    private JCTree.JCMethodInvocation doResolveBlock(JCTree.JCBlock block, java.util.List<String> names, java.util.List<JCTree.JCFieldAccess> classes, Map<String, Map<String, JCTree.JCIdent>> fieldMap)
//    {
//        ListBuffer<JCTree.JCExpression> listBuffer = new ListBuffer<>();
//        for (var statement : block.getStatements())
//        {
//            listBuffer.append(doResolve(statement, names, classes, fieldMap));
//        }
//        return treeMaker.Apply(List.nil(), expressionMap.get(IExpression.Type.Mappings), listBuffer.toList());
//    }
//
//    private JCTree.JCMethodInvocation doResolveExpressionStatement(JCTree.JCExpressionStatement expressionStatement, java.util.List<String> names, java.util.List<JCTree.JCFieldAccess> classes, Map<String, Map<String, JCTree.JCIdent>> fieldMap)
//    {
//        return doResolve(expressionStatement.getExpression(), names, classes, fieldMap);
//    }
//
//    private JCTree.JCMethodInvocation doResolveIdent(JCTree.JCIdent ident, java.util.List<String> names, java.util.List<JCTree.JCFieldAccess> classes, Map<String, Map<String, JCTree.JCIdent>> fieldMap)
//    {
//        if (names.contains(ident.getName().toString()))
//        {
//            var index = names.indexOf(ident.getName().toString());
//            var clazz = classes.get(index);
//            var map = fieldMap.get(clazz.toString());
//            return treeMaker.Apply(List.nil(), expressionMap.get(IExpression.Type.DbRef), List.of(treeMaker.Literal(index), treeMaker.Literal("")));
//        }
//        else
//        {
//            return treeMaker.Apply(List.nil(), expressionMap.get(IExpression.Type.Value), List.of(ident));
//        }
//    }
//
//    private JCTree.JCMethodInvocation doResolveInvoke(JCTree.JCMethodInvocation methodInvocation, java.util.List<String> names, java.util.List<JCTree.JCFieldAccess> classes, Map<String, Map<String, JCTree.JCIdent>> fieldMap)
//    {
//        var invoke = methodInvocation.getMethodSelect();
//        if (invoke instanceof JCTree.JCIdent)
//        {
//            var ident = (JCTree.JCIdent) invoke;
//            switch (ident.getName().toString())
//            {
//                case "Count":
//                {
//                    var value = doResolve(methodInvocation.getArguments().get(0), names, classes, fieldMap);
//                    return treeMaker.Apply(List.nil(), expressionMap.get(IExpression.Type.DbFunc), List.of(dbFuncMap.get(DbFuncType.Count), value));
//                }
//                case "Sum":
//                {
//                    var value = doResolve(methodInvocation.getArguments().get(0), names, classes, fieldMap);
//                    return treeMaker.Apply(List.nil(), expressionMap.get(IExpression.Type.DbFunc), List.of(dbFuncMap.get(DbFuncType.Sum), value));
//                }
//            }
//        }
//        else if (invoke instanceof JCTree.JCFieldAccess)
//        {
//            var fieldAccess = (JCTree.JCFieldAccess) invoke;
//            var identifier = fieldAccess.getIdentifier();
//            if (fieldAccess.getExpression() instanceof JCTree.JCIdent)
//            {
//                var ident = (JCTree.JCIdent) fieldAccess.getExpression();
//                var name = ((JCTree.JCIdent) fieldAccess.getExpression()).getName().toString();
//                if (names.contains(name))
//                {
//                    var index = names.indexOf(ident.getName().toString());
//                    var clazz = classes.get(index);
//                    var map = fieldMap.get(clazz.toString());
//                    var fieldName = map.get(identifier.toString());
//                    if (identifier.toString().startsWith("get"))
//                    {
//                        return treeMaker.Apply(List.nil(), expressionMap.get(IExpression.Type.DbRef), List.of(treeMaker.Literal(index), treeMaker.Literal(fieldName.toString())));
//                    }
//                    else if (identifier.toString().startsWith("set"))
//                    {
//                        var value = doResolve(methodInvocation.getArguments().get(0), names, classes, fieldMap);
//                        return treeMaker.Apply(List.nil(), expressionMap.get(IExpression.Type.Mapping), List.of(treeMaker.Literal(fieldName.toString()), value));
//                    }
//                }
//                else
//                {
//                    if (!methodInvocation.getArguments().isEmpty())
//                    {
//                        if (identifier.toString().equals("contains"))
//                        {
//                            var op = strOpmap.get("in");
//                            var left = doResolve(methodInvocation.getArguments().get(0), names, classes, fieldMap);
//                            var right = treeMaker.Apply(List.nil(), expressionMap.get(IExpression.Type.Value), List.of(ident));
//                            return treeMaker.Apply(List.nil(), expressionMap.get(IExpression.Type.Binary), List.of(left, right, op));
//                        }
//                    }
//                    else
//                    {
//                        return treeMaker.Apply(List.nil(), expressionMap.get(IExpression.Type.Value), List.of(methodInvocation));
//                    }
//                }
//            }
//            else if (fieldAccess.getExpression() instanceof JCTree.JCMethodInvocation)
//            {
//                var fieldAccessExpression = (JCTree.JCMethodInvocation) fieldAccess.getExpression();
//                switch (identifier.toString())
//                {
//                    case "contains":
//                    case "startsWith":
//                    case "endsWith":
//                    case "equals":
//                        var op = strOpmap.get(identifier.toString());
//                        var left = doResolveInvoke(fieldAccessExpression, names, classes, fieldMap);
//                        var right = doResolve(methodInvocation.getArguments().get(0), names, classes, fieldMap);
//                        return treeMaker.Apply(List.nil(), expressionMap.get(IExpression.Type.Binary), List.of(left, right, op));
//                }
//                return doResolveInvoke(fieldAccessExpression, names, classes, fieldMap);
//            }
//            else if (fieldAccess.getExpression() instanceof JCTree.JCFieldAccess)
//            {
//                //todo
//            }
//        }
//        return null;
//    }
//
//    private JCTree.JCMethodInvocation doResolveLiteral(JCTree.JCLiteral literal)
//    {
//        return treeMaker.Apply(List.nil(), expressionMap.get(IExpression.Type.Value), List.of(literal));
//    }
//
//    private JCTree.JCMethodInvocation doResolveNewClass(JCTree.JCNewClass newClass, java.util.List<String> names, java.util.List<JCTree.JCFieldAccess> classes, Map<String, Map<String, JCTree.JCIdent>> fieldMap)
//    {
//        ListBuffer<JCTree.JCExpression> listBuffer = new ListBuffer<>();
//        listBuffer.append(treeMaker.Select(newClass.getIdentifier(), this.names.fromString("class")));
//        if (newClass.getClassBody() != null && !newClass.getClassBody().getMembers().isEmpty())
//        {
//            var member = (JCTree.JCBlock) newClass.getClassBody().getMembers().get(0);
//            for (var statement : member.getStatements())
//            {
//                var expressionStatement = (JCTree.JCExpressionStatement) statement;
//                if (expressionStatement.getExpression() instanceof JCTree.JCMethodInvocation)
//                {
//                    var methodInvocation = (JCTree.JCMethodInvocation) expressionStatement.getExpression();
//                    if (methodInvocation.getMethodSelect() instanceof JCTree.JCIdent && !methodInvocation.getArguments().isEmpty())
//                    {
//                        var ident = (JCTree.JCIdent) methodInvocation.getMethodSelect();
//                        var source = treeMaker.Literal(ident.getName().toString());
//                        var value = doResolve(methodInvocation.getArguments().get(0), names, classes, fieldMap);
//                        var mapping = treeMaker.Apply(List.nil(), expressionMap.get(IExpression.Type.Mapping), List.of(source, value));
//                        listBuffer.append(mapping);
//                    }
//                }
//            }
//        }
//        return treeMaker.Apply(List.nil(), expressionMap.get(IExpression.Type.New), listBuffer.toList());
//    }
//
//    private JCTree.JCMethodInvocation doResolveBinary(JCTree.JCBinary binary, java.util.List<String> names, java.util.List<JCTree.JCFieldAccess> classes, Map<String, Map<String, JCTree.JCIdent>> fieldMap)
//    {
//        var left = doResolve(binary.getLeftOperand(), names, classes, fieldMap);
//        var right = doResolve(binary.getRightOperand(), names, classes, fieldMap);
//        var op = opMap.get(binary.getTag());
//        return treeMaker.Apply(List.nil(), expressionMap.get(IExpression.Type.Binary), List.of(left, right, op));
//    }
//
//    private JCTree.JCMethodInvocation doResolveParens(JCTree.JCParens parens, java.util.List<String> names, java.util.List<JCTree.JCFieldAccess> classes, Map<String, Map<String, JCTree.JCIdent>> fieldMap)
//    {
//        var done = doResolve(parens.getExpression(), names, classes, fieldMap);
//        return treeMaker.Apply(List.nil(), expressionMap.get(IExpression.Type.Parens), List.of(done));
//    }
}
