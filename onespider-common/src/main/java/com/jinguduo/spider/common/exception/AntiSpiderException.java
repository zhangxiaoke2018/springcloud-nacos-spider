package com.jinguduo.spider.common.exception;

public class AntiSpiderException extends QuickException {

    private static final long serialVersionUID = -7231922976039260435L;

    public AntiSpiderException() {
        super();
    }
    
    public AntiSpiderException(String message) {
        super(message);
    }
    
    public AntiSpiderException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public AntiSpiderException(Throwable cause) {
        super(cause);
    }
}
