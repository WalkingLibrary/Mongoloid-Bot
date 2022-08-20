package com.jumbodinosaurs.mongoloidbot.tasks.exceptions;

public class UserQueryException extends Exception
{
    public UserQueryException()
    {
    }
    
    public UserQueryException(String message)
    {
        super(message);
    }
    
    public UserQueryException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    public UserQueryException(Throwable cause)
    {
        super(cause);
    }
    
    public UserQueryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
