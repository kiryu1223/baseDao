package com.kiryu1223.baseDao.Resolve;

import com.kiryu1223.baseDao.ExpressionV2.NewExpression;
import com.kiryu1223.baseDao.JProperty.GetSetHelper;
import com.kiryu1223.baseDao.ExpressionV2.DbFuncType;
import com.kiryu1223.baseDao.ExpressionV2.IExpression;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Name;

import com.sun.source.tree.*;
import com.sun.source.util.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
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
    private final Map<Name, List<Info>> methodExpressionTypeMap = new HashMap<>();
    Map<Name, Map<Name, JCTree.JCVariableDecl>> classPrivateFieldsMap = new HashMap<>();
    Types types;
    Elements elements;

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
        boolean initialized = isInitialized();
        boolean stripModulePrefixes = initialized && processingEnv.getSourceVersion().compareTo(SourceVersion.RELEASE_8) <= 0;
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
        return SourceVersion.RELEASE_8;
    }

    public abstract void registerManager();

    protected void register(Class<?> clazz)
    {
        for (java.lang.reflect.Method method : clazz.getMethods())
        {
            if (method.getParameterCount() != 1) continue;
            Info info = null;
            Class<?> type = method.getParameters()[0].getType();
            if (method.getParameters()[0].isAnnotationPresent(Expression.class)
                    && type.isInterface() && type.getMethods().length == 1)
            {
                Expression expression = method.getParameters()[0].getAnnotation(Expression.class);
                int count = type.getMethods()[0].getParameterCount();
                info = new Info(expression.value(), count);
            }
            if (info != null)
            {
                Name name = names.fromString(method.getName());
                if (methodExpressionTypeMap.containsKey(name))
                {
                    methodExpressionTypeMap.get(name).add(info);
                }
                else
                {
                    List<Info> o = new ArrayList<>();
                    o.add(info);
                    methodExpressionTypeMap.put(name, o);
                }
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
        types = processingEnv.getTypeUtils();
        elements = processingEnv.getElementUtils();
        registerManager();
        GetSetHelper.setNames(names);
        String expV2 = "com.kiryu1223.baseDao.ExpressionV2";

        JCTree.JCFieldAccess operator = treeMaker.Select(treeMaker.Ident(names.fromString(expV2)), names.fromString("Operator"));
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

        JCTree.JCFieldAccess iExpression = treeMaker.Select(treeMaker.Ident(names.fromString(expV2)), names.fromString("IExpression"));
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

        System.out.println("staticExpressionTree 启动!");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        if (!roundEnv.processingOver())
        {
            //依托答辩↓
            Name expName = names.fromString("Expression");
            for (Element element : roundEnv.getElementsAnnotatedWith(Expression.class))
            {
                Element enclosingElement = element.getEnclosingElement();
                JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) javacTrees.getTree(enclosingElement);
                if (methodDecl.getParameters().size() != 1) continue;
                JCTree.JCVariableDecl param = methodDecl.getParameters().get(0);
                if (!(param.getType() instanceof JCTree.JCTypeApply)) continue;
                int count = ((JCTree.JCTypeApply) param.getType()).getTypeArguments().size();
                if (count == 0) continue;
                Info info = null;
                for (JCTree.JCAnnotation annotation : param.getModifiers().getAnnotations())
                {
                    if (annotation.getAnnotationType() instanceof JCTree.JCIdent
                            && ((JCTree.JCIdent) annotation.getAnnotationType()).getName().equals(expName))
                    {
                        Class<? extends IExpression> cc = IExpression.class;
                        if (!annotation.getArguments().isEmpty())
                        {
                            for (JCTree.JCExpression argument : annotation.getArguments())
                            {
                                JCTree.JCAssign assign = (JCTree.JCAssign) argument;
                                if (assign.getVariable().toString().equals("value"))
                                {
                                    switch (assign.getExpression().toString())
                                    {
                                        case "NewExpression.class":
                                            cc = NewExpression.class;
                                            break;
                                    }
                                }
                            }
                        }
                        info = new Info(cc, count);
                        break;
                    }
                }
                if (info == null) continue;
                if (methodExpressionTypeMap.containsKey(methodDecl.getName()))
                {
                    List<Info> list = methodExpressionTypeMap.get(methodDecl.getName());
                    list.add(info);
                }
                else
                {
                    List<Info> o = new ArrayList<>();
                    o.add(info);
                    methodExpressionTypeMap.put(methodDecl.getName(), o);
                }
            }

            //表达式树流程
            for (Element root : roundEnv.getRootElements())
            {
                if (!root.getKind().equals(ElementKind.CLASS)) continue;
                JCTree.JCClassDecl jcClassDecl = javacTrees.getTree((TypeElement) root);
                treeMaker.pos = jcClassDecl.getPreferredPosition();
                jcClassDecl.accept(new ExpressionTranslator(
                        methodExpressionTypeMap,
                        opMap,
                        expressionMap,
                        treeMaker,
                        names));
            }
//            //获取全部类私有属性流程
//            for (var rootElement : rootData)
//            {
//                var jcTree = javacTrees.getTree(rootElement);
//                treeMaker.pos = jcTree.getPreferredPosition();
//                Map<Name, JCTree.JCVariableDecl> privateFields = new HashMap<>();
//                jcTree.accept(new TreeTranslator()
//                {
//                    @Override
//                    public void visitClassDef(JCTree.JCClassDecl tree)
//                    {
//                        for (var member : tree.getMembers())
//                        {
//                            if (member.getKind() == Tree.Kind.VARIABLE)
//                            {
//                                var variableDecl = (JCTree.JCVariableDecl) member;
//                                if (!variableDecl.getModifiers().getFlags().contains(Modifier.PUBLIC))
//                                {
//                                    privateFields.put(variableDecl.getName(), variableDecl);
//                                }
//                            }
//                        }
//                        super.visitClassDef(tree);
//                    }
//                });
//                if (!privateFields.isEmpty())
//                {
//                    classPrivateFieldsMap.put(((JCTree.JCClassDecl) jcTree).getSimpleName(), privateFields);
//                }
//            }
//
//            //替换流程
//            for (var rootElement : rootData)
//            {
//                var jcTree = javacTrees.getTree((TypeElement) rootElement);
//                treeMaker.pos = jcTree.getPreferredPosition();
//                var currentName = jcTree.getSimpleName();
//                Set<Integer> poses = new HashSet<>();
//                jcTree.accept(new TreeTranslator()
//                {
//                    @Override
//                    public void visitMethodDef(JCTree.JCMethodDecl methodDecl)
//                    {
//                        var pos = methodDecl.getPreferredPosition();
//                        if (poses.contains(pos))
//                        {
//                            super.visitMethodDef(methodDecl);
//                            return;
//                        }
//
//                        Map<Name, Name> nameMap = new HashMap<>();
//                        methodDecl.accept(new TreeTranslator()
//                        {
//                            @Override
//                            public void visitVarDef(JCTree.JCVariableDecl varDef)
//                            {
//                                if (varDef.getType() != null)
//                                {
//                                    var type = varDef.getType();
//                                    if (type instanceof JCTree.JCIdent)
//                                    {
//                                        var name = ((JCTree.JCIdent) type).getName();
//                                        if (classPrivateFieldsMap.containsKey(name))
//                                        {
//                                            nameMap.put(varDef.getName(), name);
//                                        }
//                                    }
//                                    else if (type instanceof JCTree.JCTypeApply)
//                                    {
//                                        var typeApply = (JCTree.JCTypeApply) type;
//                                        var name = ((JCTree.JCIdent) typeApply.getType()).getName();
//                                        if (classPrivateFieldsMap.containsKey(name))
//                                        {
//                                            nameMap.put(varDef.getName(), ((JCTree.JCIdent) typeApply.getType()).getName());
//                                        }
//                                    }
//                                }
//                                else
//                                {
//                                    var initializer = varDef.getInitializer();
//                                    if (initializer != null)
//                                    {
//                                        var name = GetSetHelper.getType(varDef.getInitializer(), nameMap);
//                                        if (name != null && classPrivateFieldsMap.containsKey(name))
//                                        {
//                                            nameMap.put(varDef.getName(), name);
//                                        }
//                                    }
//                                }
//                                super.visitVarDef(varDef);
//                            }
//                        });
//
//                        methodDecl.accept(new GetterSetterTranslator(
//                                classPrivateFieldsMap,
//                                nameMap,
//                                treeMaker,
//                                names,
//                                currentName
//                        ));
//
//                        super.visitMethodDef(methodDecl);
//
//                        poses.add(pos);
//                    }
//                });
//            }
        }
        return false;
    }

    private void FindDown(Element element)
    {
        for (Element enclosedElement : element.getEnclosedElements())
        {
            if (enclosedElement instanceof Symbol.MethodSymbol)
            {
                Symbol.MethodSymbol methodSymbol = ((Symbol.MethodSymbol) enclosedElement);
                JCTree.JCMethodDecl methodDecl = javacTrees.getTree(methodSymbol);
                System.out.println(methodDecl.getBody());
            }
            else
            {
                FindDown(enclosedElement);
            }
        }
    }
}
