package io.github.kiryu1223.baseDao.Error;

public class NoWayException extends RuntimeException
{
    public NoWayException()
    {
        super("Please clean and reCompile (请重新编译项目)");
    }
}
