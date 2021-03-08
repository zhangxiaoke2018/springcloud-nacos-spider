package com.jinguduo.spider.common.msg;

import java.io.IOException;
import java.lang.reflect.Type;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

public class GzipJsonHttpMessageConverter extends AbstractGenericHttpMessageConverter<Object> {

    public GzipJsonHttpMessageConverter() {
        // TODO Auto-generated constructor stub
    }
    
    
    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void writeInternal(Object t, Type type, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        // TODO Auto-generated method stub
        return null;
    }

}
