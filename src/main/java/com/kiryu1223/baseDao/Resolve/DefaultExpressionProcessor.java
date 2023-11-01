package com.kiryu1223.baseDao.Resolve;

import com.kiryu1223.baseDao.Dao.Queryer.Query;
import com.kiryu1223.baseDao.Dao.Queryer.Query2;
public class DefaultExpressionProcessor extends AbstractExpressionProcessor
{
    @Override
    public void registerManager()
    {
        register(Query.class);
        register(Query2.class);
    }
}
