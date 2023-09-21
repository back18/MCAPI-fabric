package com.quan.mcapi;

public enum StatusCode
{
    OK(200),
    Forbidden(403),
    NotFound(404),
    InternalServerError(500);

    private final int value;

    private StatusCode(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }
}
