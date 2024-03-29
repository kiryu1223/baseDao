package io.github.kiryu1223.baseDao.core;

import io.github.kiryu1223.baseDao.core.Cud.Cud;
import io.github.kiryu1223.baseDao.core.Deleter.Delete;
import io.github.kiryu1223.baseDao.core.Inserter.Insert;
import io.github.kiryu1223.baseDao.core.Queryer.Query;
import io.github.kiryu1223.baseDao.core.Queryer.Query2;
import io.github.kiryu1223.baseDao.core.Queryer.Query3;
import io.github.kiryu1223.baseDao.core.Queryer.Query4;
import io.github.kiryu1223.baseDao.core.Updater.Update;
import io.github.kiryu1223.baseDao.DataBase.DataBase;

import javax.sql.DataSource;
import java.util.List;

public class BaseDao
{
    public BaseDao(DataSource dataSource, DataBase.Type type)
    {
        DBUtil.setDataSource(dataSource);
        //Resolver.setDb(type.getDb());
    }

    public <T> Query<T> query(Class<T> c1)
    {
        return new Query<>(c1);
    }

    public <T1, T2> Query2<T1, T2> query(Class<T1> c1, Class<T2> c2)
    {
        return new Query2<>(c1, c2);
    }

    public <T1, T2, T3> Query3<T1, T2, T3> query(Class<T1> c1, Class<T2> c2, Class<T3> c3)
    {
        return new Query3<>(c1, c2, c3);
    }

    public <T1, T2, T3, T4> Query4<T1, T2, T3, T4> query(Class<T1> c1, Class<T2> c2, Class<T3> c3, Class<T4> c4)
    {
        return new Query4<>(c1, c2, c3, c4);
    }

    public <T> Insert<T> insert(Class<T> c1)
    {
        return new Insert<>(c1);
    }

    public <T> Delete<T> delete(Class<T> c1)
    {
        return new Delete<>(c1);
    }

    public <T> Update<T> update(Class<T> c1)
    {
        return new Update<>(c1);
    }

    public <T> boolean save(T t)
    {
        Entity entity = Resolver.save(new Save<>(t));
        return DBUtil.startUpdate(entity) == 1;
    }

    public <T> boolean save(List<T> ts)
    {
        if (!ts.isEmpty())
        {
            List<Entity> entityList = Resolver.batchSave(ts);
            List<Integer> array = DBUtil.batchUpdate(entityList);
            int count = 0;
            for (int i : array) count += i;
            return count == ts.size();
        }
        else
        {
            return false;
        }
    }

    public Transaction startTransaction(Cud<?>... cuds)
    {
        Transaction transaction = new Transaction();
        for (Cud<?> c : cuds)
        {
            transaction.push(c);
        }
        return transaction;
    }

    public Transaction begin(Cud<?>... cuds)
    {
        Transaction transaction = new Transaction();
        for (Cud<?> c : cuds)
        {
            transaction.push(c);
        }
        return transaction;
    }

}
