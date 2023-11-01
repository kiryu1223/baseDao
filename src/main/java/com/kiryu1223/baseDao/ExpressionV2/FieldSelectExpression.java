package com.kiryu1223.baseDao.ExpressionV2;

import com.kiryu1223.baseDao.Dao.Cache;

import java.lang.reflect.InvocationTargetException;

public class FieldSelectExpression implements IExpression, IHasSource
{
    private final IExpression selector;
    private final String selectedField;

    public FieldSelectExpression(IExpression expression, String selectedField)
    {
        this.selector = expression;
        this.selectedField = selectedField;
    }

    public IExpression getSelector()
    {
        return selector;
    }

    public String getSelectedField()
    {
        return selectedField;
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
            var fields = Cache.getTypeFields(a.getClass());
            for (var field : fields)
            {
                if (field.getName().equals(selectedField))
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
        else if (selector instanceof FieldSelectExpression)
        {
            var fieldSelect = (FieldSelectExpression) selector;
            var a = fieldSelect.getValue();
            var fields = Cache.getTypeFields(a.getClass());
            for (var field : fields)
            {
                if (field.getName().equals(fieldSelect.selectedField))
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
