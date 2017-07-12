package io.neolang.token

/**
 * @author kiva
 */
enum class TokenType {
    TYPE_IDENTIFIER,
    TYPE_STRING,
    TYPE_INTEGER,
    TYPE_BOOLEAN,

    KEYWORD_DOLLAR, /* $ */
    KEYWORD_USE     /* @ */,

    OPERATOR_BEGIN,
    OPT_ADD, /* + */
    OPT_SUB, /* - */
    OPT_NAV, /* - (负号) */
    OPT_MUL, /* * */
    OPT_DIV, /* / */
    OPT_MOD, /* % */
    OPT_XOR, /* ^ */
    OPT_AND, /* & */
    OPT_OR, /* | */
    LEFT_SHIFT, /* << */
    RIGHT_SHIFT, /* >> */
    OPERATOR_END,

    LOGICAL_OPERATOR_BEGIN,
    OPT_LAND, /* && */
    OPT_LOR, /* || */
    OPT_LE, /* <= */
    OPT_LT, /* < */
    OPT_GE, /* >= */
    OPT_GT, /* > */
    OPT_EQ, /* == */
    OPT_NEQ, /* != */
    OPT_NOT, /* ! */
    LOGICAL_OPERATOR_END,
}