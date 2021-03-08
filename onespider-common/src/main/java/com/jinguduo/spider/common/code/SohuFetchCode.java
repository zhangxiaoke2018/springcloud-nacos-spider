package com.jinguduo.spider.common.code;

import com.jinguduo.spider.common.util.HttpHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @USER xiaoyun
 * @DATE 2016/10/9 11:41
 */
// FIXME: 不要在这里发起一次http请求
public class SohuFetchCode implements FetchCode {
    @Override
    public String get(String url) {
        if (url.contains("s2020")||url.contains("s2019")||url.contains("s2018")||url.contains("s2017")||url.contains("s2016") || url.contains("s2015") || url.contains("s2014") || url.contains("s2013")) {
            Document document = Jsoup.parse(HttpHelper.get(url, "UTF-8"));
            String script = document.html().replace(" ", "");

            Pattern pattern = Pattern.compile("playlistId=\"(\\d+)\"");
            Matcher matcher = pattern.matcher(script);

            Pattern patternB = Pattern.compile("PLAYLIST_ID=\"(\\d+)\"");
            Matcher matcherB = patternB.matcher(script);

            if (matcher.find()){
                return matcher.group(1);
            }else if(matcherB.find()){
                return matcherB.group(1);
            }

        } else if(url.contains("my.tv.sohu.com")) {//自媒体(网综) http://my.tv.sohu.com/pl/9294859/index.shtml
            Matcher matcher = Pattern.compile("http://my\\.tv\\.sohu\\.com/pl/(.*?)/index\\.shtml").matcher(url);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } else {
            Document document = Jsoup.parse(HttpHelper.get(url, "UTF-8"));
            Elements elements = document.getElementsByClass("vBox-warn");
            if (elements != null && elements.size() != 0) {//网综
                String attr = elements.get(0).attr("data-plid");
                return attr;
            } else {//网络大电影
                String script = document.html().replace(" ", "");
                Pattern pattern = Pattern.compile("playlistId=\"(\\d+)\"");
                Matcher matcher = pattern.matcher(script);
                if (matcher.find())
                    return matcher.group(1);
            }
        }
        return "";
    }

//    public static void main(String[] args) {
//        SohuFetchCode sohuFetchCode = new SohuFetchCode();
//        System.out.println("---------------"+sohuFetchCode.get("http://tv.sohu.com/s2014/zgbgbtx/"));
//    }
}
