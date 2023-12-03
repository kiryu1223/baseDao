package io.github.kiryu1223.baseDao;

import io.github.kiryu1223.baseDao.Dao.BaseDao;
import io.github.kiryu1223.baseDao.Dao.Deleter.Delete;
import io.github.kiryu1223.baseDao.Dao.Inserter.Insert;
import io.github.kiryu1223.baseDao.Dao.Queryer.*;
import io.github.kiryu1223.baseDao.Dao.Updater.Update;
import io.github.kiryu1223.expressionTree.AbstractExpressionProcessor;

import java.util.List;

public class BaseDaoProcessor extends AbstractExpressionProcessor
{
    @Override
    public void registerManager(List<Class<?>> classList)
    {
        classList.add(BaseDao.class);

        classList.add(Query.class);
        classList.add(Query2.class);
        classList.add(Query3.class);
        classList.add(Query4.class);

        classList.add(Insert.class);
        classList.add(Update.class);
        classList.add(Delete.class);
    }
}
