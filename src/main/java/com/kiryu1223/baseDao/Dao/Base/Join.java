package com.kiryu1223.baseDao.Dao.Base;

import com.kiryu1223.baseDao.Dao.JoinType;

public class Join extends Base
{
    private final JoinType joinType;
    private final Class<?> joinClass;

    public Join(Class<?> joinClass, JoinType joinType)
    {
        this.joinClass = joinClass;
        this.joinType = joinType;
    }

    public JoinType getJoinType()
    {
        return joinType;
    }

    public Class<?> getJoinClass()
    {
        return joinClass;
    }
}
