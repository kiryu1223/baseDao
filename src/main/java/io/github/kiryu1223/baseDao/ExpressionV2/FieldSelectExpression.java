package io.github.kiryu1223.baseDao.ExpressionV2;

import java.lang.reflect.Field;

public class FieldSelectExpression implements IExpression
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
        if (sel == null)
        {
            throw new RuntimeException(this.toString() + " getValue()");
        }
        try
        {
            Field field = sel instanceof Class<?>
                    ?((Class<?>) sel).getDeclaredField(selectedField)
                    :sel.getClass().getDeclaredField(selectedField);
            field.setAccessible(true);
            return field.get(sel);
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString()
    {
        return selector + "." + selectedField;
    }
}