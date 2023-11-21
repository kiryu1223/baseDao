package io.github.kiryu1223.baseDao.Dao.models.m1;

import io.github.kiryu1223.baseDao.Dao.Queryer.Query;
import io.github.kiryu1223.baseDao.Dao.Queryer.Query2;
import io.github.kiryu1223.baseDao.Dao.Queryer.Query3;
import io.github.kiryu1223.baseDao.Dao.Queryer.Query4;

import java.lang.reflect.ParameterizedType;

public interface Model<T1>
{

    default Query<T1> query()
    {
        ParameterizedType model = (ParameterizedType) this.getClass().getGenericInterfaces()[0];
        java.lang.reflect.Type[] types = model.getActualTypeArguments();
        return new Query<T1>((Class<T1>) types[0]);
    }

    default <T2> Query2<T1, T2> query(Class<T2> c2)
    {
        ParameterizedType model = (ParameterizedType) this.getClass().getGenericInterfaces()[0];
        java.lang.reflect.Type[] types = model.getActualTypeArguments();
        return new Query2<T1, T2>(null, null, (Class<T1>) types[0], c2);
    }

    default <T2, T3> Query3<T1, T2, T3> query(Class<T2> c2, Class<T3> c3)
    {
        ParameterizedType model = (ParameterizedType) this.getClass().getGenericInterfaces()[0];
        java.lang.reflect.Type[] types = model.getActualTypeArguments();
        return new Query3<>(null, null, (Class<T1>) types[0], c2, c3);
    }

    default <T2, T3, T4> Query4<T1, T2, T3, T4> query(Class<T2> c2, Class<T3> c3, Class<T4> c4)
    {
        ParameterizedType model = (ParameterizedType) this.getClass().getGenericInterfaces()[0];
        java.lang.reflect.Type[] types = model.getActualTypeArguments();
        return new Query4<>(null, null, (Class<T1>) types[0], c2, c3, c4);
    }
}
