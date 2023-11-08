package com.kiryu1223.baseDao.Dao.Factory;

import com.kiryu1223.baseDao.Dao.Entity;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class EntityFactory
{
    private static final ConcurrentLinkedDeque<Entity> Pool = new ConcurrentLinkedDeque<>();

    static
    {
        for (int i = 0; i < 128; i++)
        {
            Pool.push(new Entity());
        }
    }

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
