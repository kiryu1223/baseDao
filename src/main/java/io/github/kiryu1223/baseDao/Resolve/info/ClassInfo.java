package io.github.kiryu1223.baseDao.Resolve.info;

import java.util.ArrayList;
import java.util.List;

public class ClassInfo
{
    private final String packageName;
    private final String fullName;
    private final List<ImportInfo> importInfos = new ArrayList<>();
    private final List<MethodInfo> methodInfos = new ArrayList<>();

    public ClassInfo(String packageName, String fullName)
    {
        this.packageName = packageName;
        this.fullName = fullName;
    }

    public String getPackageName()
    {
        return packageName;
    }

    public String getFullName()
    {
        return fullName;
    }

    public List<MethodInfo> getMethodInfos()
    {
        return methodInfos;
    }

    public List<ImportInfo> getImportInfos()
    {
        return importInfos;
    }

    public boolean containsImport(String imp)
    {
        for (ImportInfo importInfo : importInfos)
        {
            if (importInfo.getImportName().equals(imp))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "ClassInfo{" +
                "packageName='" + packageName + '\'' +
                ", fullName='" + fullName + '\'' +
                ", importInfos=" + importInfos +
                ", methodInfos=" + methodInfos +
                '}';
    }
}
