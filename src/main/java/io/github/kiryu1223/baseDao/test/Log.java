package io.github.kiryu1223.baseDao.test;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
public class Log
{
    private Integer id;
    private Integer level;
    private String message;
    private Integer performerId;
    private String performerName;
    private Timestamp time;

    public Log()
    {
    }

    public Log(Integer id)
    {
        this.id = id;
    }

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    @Basic
    @Column(name = "level", nullable = false)
    public Integer getLevel()
    {
        return level;
    }

    public void setLevel(Integer level)
    {
        this.level = level;
    }

    @Basic
    @Column(name = "message", nullable = true, length = 255)
    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    @Basic
    @Column(name = "performer_id", nullable = false)
    public Integer getPerformerId()
    {
        return performerId;
    }

    public void setPerformerId(Integer performerId)
    {
        this.performerId = performerId;
    }

    @Basic
    @Column(name = "performer_name", nullable = false, length = 255)
    public String getPerformerName()
    {
        return performerName;
    }

    public void setPerformerName(String performerName)
    {
        this.performerName = performerName;
    }

    @Basic
    @Column(name = "time", nullable = false)
    public Timestamp getTime()
    {
        return time;
    }

    public void setTime(Timestamp time)
    {
        this.time = time;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Log log = (Log) o;
        return Objects.equals(id, log.id) && Objects.equals(level, log.level) && Objects.equals(message, log.message) && Objects.equals(performerId, log.performerId) && Objects.equals(performerName, log.performerName) && Objects.equals(time, log.time);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, level, message, performerId, performerName, time);
    }
}
