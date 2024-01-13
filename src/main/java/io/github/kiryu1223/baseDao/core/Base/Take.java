package io.github.kiryu1223.baseDao.core.Base;

public class Take extends Base
{
    private final int count;

    public Take(int count)
    {
        this.count = count;
    }

    public int getCount()
    {
        return count;
    }
}
