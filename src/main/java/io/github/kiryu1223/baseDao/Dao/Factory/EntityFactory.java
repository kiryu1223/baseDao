package io.github.kiryu1223.baseDao.Dao.Factory;

import io.github.kiryu1223.baseDao.Dao.Entity;

import java.util.concurrent.ConcurrentLinkedDeque;

public class EntityFactory
{
    private static final ConcurrentLinkedDeque<Entity> Pool = new ConcurrentLinkedDeque<>();

    public static Entity get()
    {
        Entity res = Pool.poll();
        if (res == null)
        {
            res = new Entity();
        }
        return res;
    }

    public static void back(Entity entity)
    {
        entity.clear();
        if (Pool.size() > 256) return;
        Pool.push(entity);
    }
}
