package com.jinguduo.spider.common.exception;

public class BadHttpResponseException extends QuickException {

	private static final long serialVersionUID = -9204657326045393439L;

	public BadHttpResponseException() {
        super();
    }
    
    public BadHttpResponseException(String message) {
        super(message);
    }
    
    public BadHttpResponseException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public BadHttpResponseException(Throwable cause) {
        super(cause);
    }
}
