package io.github.kiryu1223.baseDao.Resolve.info;

import java.util.ArrayList;
import java.util.List;

public class MethodInfo
{
    private final String methodName;
    private final String returnType;
    private final List<ParamInfo> paramInfos = new ArrayList<>();

    public MethodInfo(String methodName, String returnType)
    {
        this.methodName = methodName;
        this.returnType = returnType;
    }

    public String getMethodName()
    {
        return methodName;
    }

    public String getReturnType()
    {
        return returnType;
    }

    public List<ParamInfo> getParamInfos()
    {
        return paramInfos;
    }

    @Override
    public String toString()
    {
        return "MethodInfo{" +
                "methodName='" + methodName + '\'' +
                ", returnType='" + returnType + '\'' +
                ", paramInfos=" + paramInfos +
                '}';
    }
}
