package com.kiryu1223.baseDao.ExpressionV2;

import com.kiryu1223.baseDao.Dao.Cache;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class MethodCallExpression implements IExpression, IHasSource
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

    @Override
    public ReferenceExpression getSource()
    {
        if (selector instanceof ReferenceExpression)
        {
            return (ReferenceExpression) selector;
        }
        else if (selector instanceof FieldSelectExpression)
        {
            var fieldSelect = (FieldSelectExpression) selector;
            return fieldSelect.getSource();
        }
        else if (selector instanceof MethodCallExpression)
        {
            var methodCall = (MethodCallExpression) selector;
            return methodCall.getSource();
        }
        throw new RuntimeException(this.toString());
    }

    public Object getValue()
    {
        if (selector instanceof ReferenceExpression)
        {
            var a = ((ReferenceExpression) selector).getReference();
            for (var method : a.getClass().getMethods())
            {
                method.setAccessible(true);
                if (method.getName().equals(selectedMethod))
                {
                    try
                    {
                        return method.invoke(a, params);
                    }
                    catch (IllegalAccessException | InvocationTargetException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        else if (selector instanceof FieldSelectExpression)
        {
            var fieldSelect = (FieldSelectExpression) selector;
            var a = fieldSelect.getValue();
            var fields = Cache.getTypeFields(a.getClass());
            for (var field : fields)
            {
                if (field.getName().equals(fieldSelect.getSelectedField()))
                {
                    try
                    {
                        return field.get(a);
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        else if (selector instanceof MethodCallExpression)
        {
            var methodCall = (MethodCallExpression) selector;
            var a = methodCall.getValue();
            for (var method : a.getClass().getMethods())
            {
                method.setAccessible(true);
                if (method.getName().equals(methodCall.getSelectedMethod()))
                {
                    try
                    {
                        return method.invoke(a, methodCall.getParams());
                    }
                    catch (IllegalAccessException | InvocationTargetException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        throw new RuntimeException(this.toString());
    }
}
