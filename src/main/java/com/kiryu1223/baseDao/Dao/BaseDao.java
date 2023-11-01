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
    private final DBUtil dbUtil;

    public BaseDao(DataSource dataSource, DataBase.Type type)
    {
        this.dbUtil = new DBUtil(dataSource);
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
        return new Query<>(dbUtil, c1);
    }

    public <T1, T2> Query2<T1, T2> query(Class<T1> c1, Class<T2> c2)
    {
        return new Query2<>(dbUtil, null, null, c1, c2);
    }

    public <T1, T2, T3> Query3<T1, T2, T3> query(Class<T1> c1, Class<T2> c2, Class<T3> c3)
    {
        return new Query3<>(dbUtil, null, null, c1, c2, c3);
    }

    public <T1, T2, T3, T4> Query4<T1, T2, T3, T4> query(Class<T1> c1, Class<T2> c2, Class<T3> c3, Class<T4> c4)
    {
        return new Query4<>(dbUtil, null, null, c1, c2, c3, c4);
    }

    public <T> Insert<T> insert(Class<T> c1)
    {
        return new Insert<>(dbUtil, c1);
    }

    public <T> Delete<T> delete(Class<T> c1)
    {
        return new Delete<>(dbUtil, c1);
    }

    public <T> Update<T> update(Class<T> c1)
    {
        return new Update<>(dbUtil, c1);
    }

    public <T> boolean save(T t)
    {
        var entity = Resolve.save(new Save<>(t));
        return dbUtil.startUpdate(entity) == 1;
    }

    public <T> boolean save(List<T> ts)
    {
        if (!ts.isEmpty())
        {
            var entityList = Resolve.batchSave(ts);
            var array = dbUtil.batchUpdate(entityList);
            var count = 0;
            for (var i : array) count += i;
            return count == ts.size();
        }
        else
        {
            return false;
        }
    }

    public Transaction startTransaction(Cud<?>... cuds)
    {
        var transaction = new Transaction(dbUtil);
        for (var c : cuds)
        {
            transaction.push(c);
        }
        return transaction;
    }

    public Transaction begin(Cud<?>... cuds)
    {
        var transaction = new Transaction(dbUtil);
        for (var c : cuds)
        {
            transaction.push(c);
        }
        return transaction;
    }

}
