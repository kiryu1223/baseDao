package io.github.kiryu1223.baseDao.DataBase;

public abstract class DataBase
{
    public enum Type
    {
        Mysql(new Mysql()),
        Oracle(new Oracle()),
        ;
        private final DataBase db;

        Type(DataBase db)
        {
            this.db = db;
        }

        public DataBase getDb()
        {
            return db;
        }
    }
}
