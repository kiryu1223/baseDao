package com.kiryu1223.baseDao.Dao.models.m3;

import com.kiryu1223.baseDao.Dao.Queryer.Query2;
import com.kiryu1223.baseDao.Dao.Queryer.Query3;
import com.kiryu1223.baseDao.Dao.Queryer.Query4;

import java.lang.reflect.ParameterizedType;

public interface Model<T1, T2, T3>
{
    default Query3<T1, T2, T3> query()
    {
        ParameterizedType model = (ParameterizedType) this.getClass().getGenericInterfaces()[0];
        java.lang.reflect.Type[] types = model.getActualTypeArguments();
        return new Query3<>(null, null, (Class<T1>) types[0], (Class<T2>) types[1], (Class<T3>) types[2]);
    }

    default <T4> Query4<T1, T2, T3, T4> query(Class<T4> c4)
    {
        ParameterizedType model = (ParameterizedType) this.getClass().getGenericInterfaces()[0];
        java.lang.reflect.Type[] types = model.getActualTypeArguments();
        return new Query4<>(null, null, (Class<T1>) types[0], (Class<T2>) types[1], (Class<T3>) types[2], c4);
    }
}
