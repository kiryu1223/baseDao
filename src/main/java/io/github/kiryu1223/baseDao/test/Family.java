package io.github.kiryu1223.baseDao.test;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.Objects;

@Entity
public class Family
{
    private Integer guardianId;
    private Integer wardId;

    @Basic
    @Column(name = "guardian_id", nullable = false)
    public Integer getGuardianId()
    {
        return guardianId;
    }

    public void setGuardianId(Integer guardianId)
    {
        this.guardianId = guardianId;
    }

    @Basic
    @Column(name = "ward_id", nullable = false)
    public Integer getWardId()
    {
        return wardId;
    }

    public void setWardId(Integer wardId)
    {
        this.wardId = wardId;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Family family = (Family) o;
        return Objects.equals(guardianId, family.guardianId) && Objects.equals(wardId, family.wardId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(guardianId, wardId);
    }
}
