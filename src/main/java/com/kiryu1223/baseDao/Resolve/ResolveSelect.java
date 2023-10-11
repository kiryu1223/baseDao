package com.kiryu1223.baseDao.Resolve;

import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

public class ResolveSelect
{
    public ResolveSelect(TreeMaker treeMaker, Names names)
    {
        this.treeMaker = treeMaker;
        this.names = names;
    }

    private final TreeMaker treeMaker;
    private final Names names;
}
