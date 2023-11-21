package io.github.kiryu1223.baseDao.Resolve.info;

public class VarInfo
{
    public int pos;
    public String varName;
    public String typeName;

    public VarInfo(int pos, String varName, String typeName)
    {
        this.pos = pos;
        this.varName = varName;
        this.typeName = typeName;
    }

    @Override
    public String toString()
    {
        return "VarInfo{" +
                "pos=" + pos +
                ", varName='" + varName + '\'' +
                ", typeName='" + typeName + '\'' +
                '}';
    }
}
