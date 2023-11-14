package com.kiryu1223.baseDao.ExpressionV2;

import com.kiryu1223.baseDao.Dao.Cache;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MethodCallExpression implements IExpression
{
    private final IExpression selector;
    private final String selectedMethod;
    private final List<IExpression> params;

    public MethodCallExpression(IExpression selector, String selectedMethod, List<IExpression> params)
    {
        this.selector = selector;
        this.selectedMethod = selectedMethod;
        this.params = params;
    }

    public IExpression getSelector()
    {
        return selector;
    }

    public String getSelectedMethod()
    {
        return selectedMethod;
    }


    public List<IExpression> getParams()
    {
        return params;
    }

    public ReferenceExpression getSource()
    {
        if (selector instanceof ReferenceExpression)
        {
            return (ReferenceExpression) selector;
        }
        else if (selector instanceof FieldSelectExpression)
        {
            FieldSelectExpression fieldSelect = (FieldSelectExpression) selector;
            return fieldSelect.getSource();
        }
        else if (selector instanceof MethodCallExpression)
        {
            MethodCallExpression methodCall = (MethodCallExpression) selector;
            return methodCall.getSource();
        }
        throw new RuntimeException(this.toString());
    }

    public Object getValue()
    {
        Object sel = null;
        if (selector instanceof ReferenceExpression)
        {
            sel = ((ReferenceExpression) selector).getReference();
        }
        else if (selector instanceof FieldSelectExpression)
        {
            FieldSelectExpression fieldSelect = (FieldSelectExpression) selector;
            sel = fieldSelect.getValue();
        }
        else if (selector instanceof MethodCallExpression)
        {
            MethodCallExpression methodCall = (MethodCallExpression) selector;
            sel = methodCall.getValue();
        }
        List<Class<?>> types = new ArrayList<>();
        for (IExpression param : params)
        {
            if (param instanceof ValueExpression<?>)
            {
                ValueExpression<?> val = (ValueExpression<?>) param;
                types.add(val.getValue().getClass());
            }
        }
        if (sel == null)
        {
            throw new RuntimeException(this.toString() + " getValue()");
        }
        try
        {
            Method method = sel.getClass().getMethod(selectedMethod, types.toArray(new Class<?>[]{}));
            method.setAccessible(true);
            return method.invoke(sel, params.toArray());
        }
        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (IExpression param : params)
        {
            sb.append(param).append(",");
        }
        if (sb.length() > 0)
        {
            sb.deleteCharAt(sb.length() - 1);
        }
        return selector + "." + selectedMethod + "(" + sb + ")";
    }
}
