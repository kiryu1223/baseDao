package io.github.kiryu1223.baseDao.core.Mapping;

public abstract class Arg
{
    private final Class<?> type;

    public Arg(Class<?> type)
    {
        this.type = type;
    }

    public Class<?> getType()
    {
        return type;
    }

//    public <T> T create(ResultSet rs, int[] index) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
//    {
//        T TObject = null;
//        if (this instanceof SqlFieldArg)
//        {
//            TObject = (T) rs.getObject(index[0]++, type);
//        }
//        else if (this instanceof SqlTableArg)
//        {
//            TObject = (T) type.getConstructor().newInstance();
//            for (Field typeField : Cache.getTypeFields(type))
//            {
//                Object value = rs.getObject(index[0]++, typeField.getType());
//                typeField.set(TObject, value);
//            }
//        }
//        else if (this instanceof NewClassArg)
//        {
//            NewClassArg newClassArg = (NewClassArg) this;
//            TObject = (T) type.getConstructor().newInstance();
//            for (Pair pair : newClassArg.getPairs())
//            {
//                Method setter = pair.getMethod();
//                List<Arg> args = pair.getArgs();
//                List<Object> values = new ArrayList<>(args.size());
//                for (Arg arg : args)
//                {
//                    values.add(arg.create(rs, index));
//                }
//                setter.invoke(TObject, values.toArray());
//            }
//        }
//        return TObject;
//    }
}
