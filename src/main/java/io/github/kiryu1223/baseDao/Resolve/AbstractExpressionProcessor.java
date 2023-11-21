package io.github.kiryu1223.baseDao.Resolve;

import io.github.kiryu1223.baseDao.Resolve.info.*;
import io.github.kiryu1223.baseDao.ExpressionV2.NewExpression;
import io.github.kiryu1223.baseDao.Dao.GetSetHelper;
import io.github.kiryu1223.baseDao.ExpressionV2.IExpression;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public abstract class AbstractExpressionProcessor extends AbstractProcessor
{
    private JavacTrees javacTrees;
    private Context context;
    private TreeMaker treeMaker;
    private Names names;
    private final Map<IExpression.Type, JCTree.JCFieldAccess> expressionMap = new HashMap<>();
    private final Map<JCTree.Tag, JCTree.JCFieldAccess> opMap = new HashMap<>();
    private final List<Class<?>> classList = new ArrayList<>();
    Types types;
    Elements elements;
    private final List<ClassInfo> classInfos = new ArrayList<>();

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

    public abstract void registerManager(List<Class<?>> classList);

    private void register()
    {
        for (Class<?> clazz : classList)
        {
            ClassInfo classInfo = new ClassInfo(clazz.getPackage().getName(), clazz.getCanonicalName());
            for (Method method : clazz.getMethods())
            {
                MethodInfo methodInfo = new MethodInfo(method.getName(), method.getReturnType().getCanonicalName());
                for (Parameter parameter : method.getParameters())
                {
                    ParamInfo paramInfo = null;
                    Class<?> parameterType = parameter.getType();
                    if (isFunctionInterFace(parameterType))
                    {
                        paramInfo = new ParamInfo(parameterType.getCanonicalName(), getFunctionParamCount(parameterType));
                    }
                    else
                    {
                        paramInfo = new ParamInfo(parameterType.getCanonicalName(), 0);
                    }
                    if (parameter.isAnnotationPresent(Expression.class))
                    {
                        AnnoInfo annoInfo = new AnnoInfo(Expression.class.getCanonicalName(), parameter.getAnnotation(Expression.class).value());
                        paramInfo.getAnnoInfo().add(annoInfo);
                    }
                    methodInfo.getParamInfos().add(paramInfo);
                }
                classInfo.getMethodInfos().add(methodInfo);
            }
            classInfos.add(classInfo);
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
        registerManager(classList);
        register();
        GetSetHelper.setNames(names);
        String expV2 = "io.github.kiryu1223.baseDao.ExpressionV2";

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
        expressionMap.put(IExpression.Type.Block, treeMaker.Select(iExpression, names.fromString("block")));
        expressionMap.put(IExpression.Type.Assign, treeMaker.Select(iExpression, names.fromString("assign")));

        System.out.println("staticExpressionTree 启动!");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        if (!roundEnv.processingOver())
        {
            Set<? extends Element> roots = roundEnv.getRootElements();
            String ExpressionAll = "io.github.kiryu1223.baseDao.Resolve.*";
            String ExpressionAnno = "io.github.kiryu1223.baseDao.Resolve.Expression";

            //获取全部类
            List<ClassInfo> classInfoList = new ArrayList<>(roots.size());
            for (Element root : roots)
            {
                TreePath path = javacTrees.getPath(root);
                ExpressionTree packageName = path.getCompilationUnit().getPackageName();
                ClassInfo classInfo = new ClassInfo(packageName.toString(), root.toString());
                List<? extends ImportTree> importTrees = path.getCompilationUnit().getImports();
                for (ImportTree importTree : importTrees)
                {
                    JCTree.JCImport jcImport = (JCTree.JCImport) importTree;
                    classInfo.getImportInfos().add(
                            new ImportInfo(
                                    jcImport.getQualifiedIdentifier().toString(),
                                    jcImport.isStatic()
                            )
                    );
                }
                classInfoList.add(classInfo);
            }
            //头插使其顺序正确
            classInfos.addAll(0, classInfoList);

            //给全部类获取方法
            int index = 0;
            for (Element root : roots)
            {
                ClassInfo currentClassInfo = classInfos.get(index++);
                TreePath path = javacTrees.getPath(root);
                for (JCTree member : javacTrees.getTree((TypeElement) root).getMembers())
                {
                    if (member instanceof JCTree.JCMethodDecl)
                    {
                        JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) member;
                        if (methodDecl.getReturnType() == null || methodDecl.getModifiers().getFlags().contains(Modifier.PRIVATE))
                            continue;
                        JCTree returnType = methodDecl.getReturnType();
                        String returnStr = returnType.toString();
                        if (returnType instanceof JCTree.JCTypeApply)
                        {
                            returnStr = ((JCTree.JCTypeApply) returnType).getType().toString();
                        }
                        returnStr = getFullName(returnStr, currentClassInfo.getImportInfos());
                        MethodInfo methodInfo = new MethodInfo(methodDecl.getName().toString(), returnStr);
                        for (JCTree.JCVariableDecl parameter : methodDecl.getParameters())
                        {
                            JCTree parameterType = parameter.getType();
                            String parameterStr = parameterType.toString();
                            if (parameterType instanceof JCTree.JCTypeApply)
                            {
                                JCTree.JCTypeApply typeApply = (JCTree.JCTypeApply) parameterType;
                                parameterStr = typeApply.getType().toString();
                            }
                            parameterStr = getFullName(parameterStr, currentClassInfo.getImportInfos());
                            ParamInfo paramInfo = new ParamInfo(parameterStr, getParamCount(parameterStr));
                            com.sun.tools.javac.util.List<JCTree.JCAnnotation> annotationList = parameter.getModifiers().getAnnotations();
                            for (JCTree.JCAnnotation annotation : annotationList)
                            {
                                if (annotation.getAnnotationType() instanceof JCTree.JCIdent
                                        && ((JCTree.JCIdent) annotation.getAnnotationType()).getName().toString().equals("Expression")
                                        && (currentClassInfo.containsImport(ExpressionAnno) || currentClassInfo.containsImport(ExpressionAll)))
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
                                    paramInfo.getAnnoInfo().add(new AnnoInfo(ExpressionAnno, cc));
                                    break;
                                }
                            }
                            methodInfo.getParamInfos().add(paramInfo);
                        }
                        currentClassInfo.getMethodInfos().add(methodInfo);
                    }
                }
            }

            //表达式树流程
            for (Element root : roots)
            {
                if (!root.getKind().equals(ElementKind.CLASS)) continue;
                TreePath path = javacTrees.getPath(root);
                List<ImportInfo> importInfos = new ArrayList<>(path.getCompilationUnit().getImports().size());
                for (ImportTree importTree : path.getCompilationUnit().getImports())
                {
                    JCTree.JCImport jcImport = (JCTree.JCImport) importTree;
                    importInfos.add(new ImportInfo(jcImport.getQualifiedIdentifier().toString(), jcImport.isStatic()));
                }
                JCTree.JCClassDecl jcClassDecl = javacTrees.getTree((TypeElement) root);
                treeMaker.pos = jcClassDecl.getPreferredPosition();
                List<VarInfo> varInfos = new ArrayList<>();
                for (JCTree member : jcClassDecl.getMembers())
                {
                    if (member instanceof JCTree.JCVariableDecl)
                    {
                        JCTree.JCVariableDecl variable = (JCTree.JCVariableDecl) member;
                        String typeName= getFullName(variable.getType().toString(),importInfos);
                        varInfos.add(new VarInfo(0,variable.getName().toString(),typeName));
                    }
                }
                jcClassDecl.accept(new ExpressionTranslator(
                        varInfos, classInfos,
                        importInfos,
                        opMap,
                        expressionMap,
                        treeMaker,
                        names));
            }
        }
        return false;
    }

    private String getFullName(String returnStr, List<ImportInfo> imports)
    {
        if (returnStr.contains("."))
        {
            String[] sped = returnStr.split("\\.");
            for (ImportInfo importInfo : imports)
            {
                String imp = importInfo.getImportName();
                String[] split = imp.split("\\.");
                if (split[split.length - 1].equals(sped[0]))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(imp);
                    for (int i = 1; i < sped.length; i++)
                    {
                        sb.append(".").append(sped[i]);
                    }
                    returnStr = sb.toString();
                }
                else if (split[split.length - 1].equals("*"))
                {
                    String sub = imp.substring(0, imp.length() - 2);
                    boolean sus = false;
                    for (ClassInfo classInfo : classInfos)
                    {
                        if (classInfo.getPackageName().equals(sub))
                        {
                            returnStr = classInfo.getPackageName() + "." + returnStr;
                            sus = true;
                            break;
                        }
                    }
                    if (sus) break;
                }
            }
        }
        else
        {
            for (ImportInfo importInfo : imports)
            {
                String imp = importInfo.getImportName();
                String[] split = imp.split("\\.");
                if (split[split.length - 1].equals(returnStr))
                {
                    returnStr = imp;
                    break;
                }
                else if (split[split.length - 1].equals("*"))
                {
                    String sub = imp.substring(0, imp.length() - 2);
                    boolean sus = false;
                    for (ClassInfo classInfo : classInfos)
                    {
                        if (classInfo.getPackageName().equals(sub))
                        {
                            returnStr = classInfo.getPackageName() + "." + returnStr;
                            sus = true;
                            break;
                        }
                    }
                    if (sus) break;
                }
            }
        }
        return returnStr;
    }

    private int getParamCount(String typeName)
    {
        int count = 0;
        for (ClassInfo classInfo : classInfos)
        {
            if (classInfo.getFullName().equals(typeName))
            {
                count = classInfo.getMethodInfos().get(0).getParamInfos().size();
            }
        }
        return count;
    }

    private boolean isFunctionInterFace(Class<?> type)
    {
        if (!type.isInterface()) return false;
        int count = 0;
        for (Method method : type.getMethods())
        {
            if (method.isDefault()) continue;
            switch (method.getModifiers())
            {
                case java.lang.reflect.Modifier.STRICT:
                case java.lang.reflect.Modifier.PUBLIC + java.lang.reflect.Modifier.STRICT:
                    continue;
                default:
                    count++;
                    break;
            }
        }
        return count == 1;
    }

    private int getFunctionParamCount(Class<?> type)
    {
        int count = 0;
        for (Method method : type.getMethods())
        {
            if (method.isDefault()) continue;
            switch (method.getModifiers())
            {
                case java.lang.reflect.Modifier.STRICT:
                case java.lang.reflect.Modifier.PUBLIC + java.lang.reflect.Modifier.STRICT:
                    continue;
                default:
                    count = method.getParameterCount();
                    break;
            }
        }
        return count;
    }
}
