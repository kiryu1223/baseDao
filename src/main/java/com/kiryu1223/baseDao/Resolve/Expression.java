package com.kiryu1223.baseDao.Resolve;

import com.kiryu1223.baseDao.ExpressionV2.IExpression;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Expression
{
    Class<? extends IExpression> value() default IExpression.class;
}
