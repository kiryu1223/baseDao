package io.github.kiryu1223.baseDao.core.Mapping;


import java.util.ArrayList;
import java.util.List;

public class NewClassArg extends Arg
{
    private final List<Pair> pairs=new ArrayList<>();

    public NewClassArg(Class<?> type)
    {
        super(type);
    }

    public List<Pair> getPairs()
    {
        return pairs;
    }
}
