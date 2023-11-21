package io.github.kiryu1223.baseDao.ExpressionV2;

public enum Operator
{
    /**
     * Unary
     */
    NOT,
    /**
     * Binary
     */
    EQ,
    NE,
    GE,
    LE,
    GT,
    LT,
    And,
    Or,
    PLUS,
    MINUS,
    MUL,
    DIV,
    MOD,
    IN,
    ;

    @Override
    public String toString()
    {
        switch (this)
        {
            case NOT:
                return "!";
            case EQ:
                return "=";
            case NE:
                return "!=";
            case GE:
                return ">=";
            case LE:
                return "<=";
            case GT:
                return ">";
            case LT:
                return "<";
            case And:
                return "&&";
            case Or:
                return "||";
            case PLUS:
                return "+";
            case MINUS:
                return "-";
            case MUL:
                return "*";
            case DIV:
                return "/";
            case MOD:
                return "%";
            default:
                throw new RuntimeException(this.name());
        }
    }
}
