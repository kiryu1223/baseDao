package io.github.kiryu1223.baseDao.Resolve.info;

import io.github.kiryu1223.baseDao.ExpressionV2.IExpression;

import java.util.ArrayList;
import java.util.List;

public class ParamInfo
{
    private final String ParamType;
    private final int lambdaParamCount;
    private final List<AnnoInfo> annoInfo = new ArrayList<>();

    public ParamInfo(String paramType, int typeCount)
    {
        ParamType = paramType;
        this.lambdaParamCount = typeCount;
    }

    public String getParamType()
    {
        return ParamType;
    }

    public List<AnnoInfo> getAnnoInfo()
    {
        return annoInfo;
    }

    public int getLambdaParamCount()
    {
        return lambdaParamCount;
    }

    public boolean isExpressionParam()
    {
        return !annoInfo.isEmpty();
    }
    public Class<? extends IExpression> getExpressionType()
    {
        return annoInfo.get(0).getExpressionType();
    }
    @Override
    public String toString()
    {
        return "ParamInfo{" +
                "ParamType='" + ParamType + '\'' +
                ", lambdaParamCount=" + lambdaParamCount +
                ", annoInfo=" + annoInfo +
                '}';
    }
}
