package io.github.kiryu1223.baseDao.core.Base;

import io.github.kiryu1223.baseDao.core.JoinType;

public class Join extends Base
{
    private final JoinType joinType;
    private final Class<?> joinClass;
    private final int index;

    public Join(Class<?> joinClass, JoinType joinType, int index)
    {
        this.joinClass = joinClass;
        this.joinType = joinType;
        this.index = index;
    }

    public JoinType getJoinType()
    {
        return joinType;
    }

    public Class<?> getJoinClass()
    {
        return joinClass;
    }

    public int getIndex()
    {
        return index;
    }
}
