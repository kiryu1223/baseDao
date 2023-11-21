package io.github.kiryu1223.baseDao.Dao.models.m4;

import io.github.kiryu1223.baseDao.Dao.Queryer.Query4;

import java.lang.reflect.ParameterizedType;

public interface Model<T1, T2, T3, T4>
{
    default Query4<T1, T2, T3, T4> query()
    {
        ParameterizedType model = (ParameterizedType) this.getClass().getGenericInterfaces()[0];
        java.lang.reflect.Type[] types = model.getActualTypeArguments();
        return new Query4<>(null, null, (Class<T1>) types[0], (Class<T2>) types[1], (Class<T3>) types[2], (Class<T4>) types[3]);
    }
}
