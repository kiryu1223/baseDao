package com.kiryu1223.baseDao.Dao.Base;

import com.kiryu1223.baseDao.ExpressionV2.MappingExpression;
import com.kiryu1223.baseDao.ExpressionV2.MappingsExpression;

import java.util.List;

public class SetData extends Base
{
    private final MappingsExpression mappingExpressions;

    public SetData(MappingsExpression mappingExpressions)
    {
        this.mappingExpressions = mappingExpressions;
    }

    public MappingsExpression getMappingExpressions()
    {
        return mappingExpressions;
    }
}
