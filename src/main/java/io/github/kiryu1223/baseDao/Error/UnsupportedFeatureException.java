package io.github.kiryu1223.baseDao.Error;

public class UnsupportedFeatureException extends RuntimeException
{
    public UnsupportedFeatureException(Object mess)
    {
        super("未支持的功能" + "\n" + mess);
    }
}
