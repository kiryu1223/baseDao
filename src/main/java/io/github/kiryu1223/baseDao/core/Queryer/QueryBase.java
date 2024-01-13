package io.github.kiryu1223.baseDao.core.Queryer;

import io.github.kiryu1223.baseDao.core.Base.Base;

import java.util.ArrayList;
import java.util.List;

public abstract class QueryBase
{
    protected final List<Base> bases = new ArrayList<>();
    protected final List<Class<?>> joins = new ArrayList<>();

    public QueryBase()
    {
    }

    public QueryBase(List<Base> bases, List<Class<?>> joins)
    {
        this.bases.addAll(bases);
        this.joins.addAll(joins);
    }

    public List<Base> getBases()
    {
        return bases;
    }
}
