package io.github.kiryu1223.baseDao.Resolve.info;

public class ImportInfo
{
    private final String importName;
    private  final boolean isStatic;

    public ImportInfo(String importName, boolean isStatic)
    {
        this.importName = importName;
        this.isStatic = isStatic;
    }

    public String getImportName()
    {
        return importName;
    }

    public boolean isStatic()
    {
        return isStatic;
    }

    @Override
    public String toString()
    {
        return "ImportInfo{" +
                "importName='" + importName + '\'' +
                ", isStatic=" + isStatic +
                '}';
    }
}
