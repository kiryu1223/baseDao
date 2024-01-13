package io.github.kiryu1223.baseDao.core;

public class Save<T>
{
    private final T t;

    public Save(T t)
    {
        this.t = t;
    }

    public T getTarget()
    {
        return t;
    }
}
