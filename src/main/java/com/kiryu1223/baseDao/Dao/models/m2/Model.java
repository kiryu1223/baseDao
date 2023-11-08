package com.kiryu1223.baseDao.Dao.models.m2;

import com.kiryu1223.baseDao.Dao.Queryer.Query;
import com.kiryu1223.baseDao.Dao.Queryer.Query2;
import com.kiryu1223.baseDao.Dao.Queryer.Query3;
import com.kiryu1223.baseDao.Dao.Queryer.Query4;

import java.lang.reflect.ParameterizedType;

public interface Model<T1, T2>
{
    default Query2<T1, T2> query()
    {
        ParameterizedType model = (ParameterizedType) this.getClass().getGenericInterfaces()[0];
        java.lang.reflect.Type[] types = model.getActualTypeArguments();
        return new Query2<T1, T2>(null, null, (Class<T1>) types[0], (Class<T2>)types[1]);
    }

    default <T3> Query3<T1, T2, T3> query(Class<T3> c3)
    {
        ParameterizedType model = (ParameterizedType) this.getClass().getGenericInterfaces()[0];
        java.lang.reflect.Type[] types = model.getActualTypeArguments();
        return new Query3<>(null, null, (Class<T1>) types[0], (Class<T2>)types[1], c3);
    }

    default <T3, T4> Query4<T1, T2, T3, T4> query(Class<T3> c3, Class<T4> c4)
    {
        ParameterizedType model = (ParameterizedType) this.getClass().getGenericInterfaces()[0];
        java.lang.reflect.Type[] types = model.getActualTypeArguments();
        return new Query4<>(null, null, (Class<T1>) types[0], (Class<T2>)types[1], c3, c4);
    }
}
