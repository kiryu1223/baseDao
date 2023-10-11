package com.kiryu1223.baseDao.Dao.Base;

public class Limit extends Base
{
    private int offset = -1;
    private int rows = -1;

    public Limit(int offset, int rows)
    {
        this.offset = offset;
        this.rows = rows;
    }
    public Limit(int rows)
    {
        this.rows = rows;
    }
    public int getOffset()
    {
        return offset;
    }

    public int getRows()
    {
        return rows;
    }
}
