package com.jinguduo.spider.common.code;

import com.alibaba.fastjson.JSONObject;

import lombok.extern.apachecommons.CommonsLog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @USER xiaoyun
 * @DATE 2016/10/9 11:46
 */
@CommonsLog
public class FiveSixMovieFetchCode implements FetchCode {
    @Override
    public String get(String url) {
        String code = "";
        Document document = null;
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        String script = document.getElementsByTag("script").get(1).toString();
        try{
            script = script.substring(script.indexOf("sohuVideoInfo"));
            JSONObject data = JSONObject.parseObject(script.substring(script.indexOf("{"), script.indexOf("}") + 1));
            String vid = data.getString("vid");
            return vid;
        }catch (StringIndexOutOfBoundsException ex){
            //"http://www.56.com/u59/v_MTM1ODcyNDAw.html"
            code = url.substring(url.lastIndexOf("/")+3,url.lastIndexOf(".html"));
        }
        return code;
    }
}
