package com.kiryu1223.baseDao.Dao;

import com.kiryu1223.baseDao.ExpressionV2.IExpression;
import com.kiryu1223.baseDao.ExpressionV2.NewExpression;

public interface ExpressionFunc
{
    interface E1<Unused,T1>
    {
        IExpression invoke(Unused unused,T1 t1);
    }
    interface E2<Unused,T1,T2>
    {
        IExpression invoke(Unused unused,T1 t1,T2 t2);
    }
    interface E3<Unused,T1,T2,T3>
    {
        IExpression invoke(Unused unused,T1 t1,T2 t2,T3 t3);
    }
    interface E4<Unused,T1,T2,T3,T4>
    {
        IExpression invoke(Unused unused,T1 t1,T2 t2,T3 t3,T4 t4);
    }

    interface NR1<Unused,T1,R>
    {
        NewExpression<R> invoke(Unused unused,T1 t1);
    }
    interface NR2<Unused,T1,T2,R>
    {
        NewExpression<R> invoke(Unused unused,T1 t1,T2 t2);
    }
    interface NR3<Unused,T1,T2,T3,R>
    {
        NewExpression<R> invoke(Unused unused,T1 t1,T2 t2,T3 t3);
    }
    interface NR4<Unused,T1,T2,T3,T4,R>
    {
        NewExpression<R> invoke(Unused unused,T1 t1,T2 t2,T3 t3,T4 t4);
    }
}
