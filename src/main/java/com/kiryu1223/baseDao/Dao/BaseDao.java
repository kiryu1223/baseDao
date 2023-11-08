package com.kiryu1223.baseDao.Dao;

import com.kiryu1223.baseDao.Dao.Cud.Cud;
import com.kiryu1223.baseDao.Dao.Inserter.Save;
import com.kiryu1223.baseDao.DataBase.DataBase;
import com.kiryu1223.baseDao.DataBase.Mysql;
import com.kiryu1223.baseDao.DataBase.Oracle;
import com.kiryu1223.baseDao.Dao.Deleter.Delete;
import com.kiryu1223.baseDao.Dao.Inserter.Insert;
import com.kiryu1223.baseDao.Dao.Queryer.Query;
import com.kiryu1223.baseDao.Dao.Queryer.Query2;
import com.kiryu1223.baseDao.Dao.Queryer.Query3;
import com.kiryu1223.baseDao.Dao.Queryer.Query4;
import com.kiryu1223.baseDao.Dao.Updater.Update;

import javax.sql.DataSource;
import java.util.List;

public class BaseDao
{
    public BaseDao(DataSource dataSource, DataBase.Type type)
    {
        DBUtil.setDataSource(dataSource);
        switch (type)
        {
            case Mysql:
                Resolve.setDb(new Mysql());
                break;
            case Oracle:
                Resolve.setDb(new Oracle());
                break;
        }
    }

    public <T> Query<T> query(Class<T> c1)
    {
        return new Query<>(c1);
    }

    public <T1, T2> Query2<T1, T2> query(Class<T1> c1, Class<T2> c2)
    {
        return new Query2<>(null, null, c1, c2);
    }

    public <T1, T2, T3> Query3<T1, T2, T3> query(Class<T1> c1, Class<T2> c2, Class<T3> c3)
    {
        return new Query3<>(null, null, c1, c2, c3);
    }

    public <T1, T2, T3, T4> Query4<T1, T2, T3, T4> query(Class<T1> c1, Class<T2> c2, Class<T3> c3, Class<T4> c4)
    {
        return new Query4<>(null, null, c1, c2, c3, c4);
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
        Entity entity = Resolve.save(new Save<>(t));
        return DBUtil.startUpdate(entity) == 1;
    }

    public <T> boolean save(List<T> ts)
    {
        if (!ts.isEmpty())
        {
            List<Entity> entityList = Resolve.batchSave(ts);
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
