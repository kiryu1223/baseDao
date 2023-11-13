package com.kiryu1223.baseDao.Resolve;

import com.kiryu1223.baseDao.Dao.Queryer.Query;
import com.kiryu1223.baseDao.Dao.Queryer.Query2;
import com.kiryu1223.baseDao.Dao.Queryer.Query3;
import com.kiryu1223.baseDao.Dao.Queryer.Query4;

public class DefaultExpressionProcessor extends AbstractExpressionProcessor
{
    @Override
    public void registerManager()
    {
        register(Query.class);
        register(Query2.class);
        register(Query3.class);
        register(Query4.class);
    }
}
