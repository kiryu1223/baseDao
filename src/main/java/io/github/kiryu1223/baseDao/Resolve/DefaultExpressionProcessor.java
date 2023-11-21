package io.github.kiryu1223.baseDao.Resolve;

import io.github.kiryu1223.baseDao.Dao.BaseDao;
import io.github.kiryu1223.baseDao.Dao.Deleter.Delete;
import io.github.kiryu1223.baseDao.Dao.Inserter.Insert;
import io.github.kiryu1223.baseDao.Dao.Queryer.Query;
import io.github.kiryu1223.baseDao.Dao.Queryer.Query2;
import io.github.kiryu1223.baseDao.Dao.Queryer.Query3;
import io.github.kiryu1223.baseDao.Dao.Queryer.Query4;
import io.github.kiryu1223.baseDao.Dao.Updater.Update;

import java.util.List;

public class DefaultExpressionProcessor extends AbstractExpressionProcessor
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
