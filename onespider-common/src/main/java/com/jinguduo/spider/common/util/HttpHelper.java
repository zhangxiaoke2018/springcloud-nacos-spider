package com.jinguduo.spider.common.util;

import org.apache.http.Consts;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import lombok.extern.apachecommons.CommonsLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/6/23 下午4:04
 */
@CommonsLog
public class HttpHelper {

    public static String get(String url,String charset){

        HttpGet request = new HttpGet(url);
        StringBuffer sb = new StringBuffer();

        BufferedReader in = null;
        try {
            CloseableHttpResponse httpResponse = HttpClients.createMinimal().execute(request);
            in = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), charset));
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return sb.toString();
    }

    public static String post(String url,String json){

        HttpPost request = new HttpPost(url);
        StringEntity entity = new StringEntity(json, Consts.UTF_8);
        request.setEntity(entity);
        StringBuffer sb = new StringBuffer();

        BufferedReader in = null;
        try {
            CloseableHttpResponse httpResponse = HttpClients.createMinimal().execute(request);
            in = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF8"));
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return sb.toString();
    }
}
