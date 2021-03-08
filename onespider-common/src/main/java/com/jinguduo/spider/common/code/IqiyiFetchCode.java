package com.jinguduo.spider.common.code;

import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.common.util.HttpHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @USER xiaoyun
 * @DATE 2016/10/9 11:36
 */
//FIXME: 不要在这里发起一次http请求
public class IqiyiFetchCode implements FetchCode {
    @Override
    public  String get(String url) {

        String code = "";

        String resStr = HttpHelper.get(url, "UTF-8");
        if (resStr.contains("Q.PageInfo.playPageInfo =")) {

            if(url.contains("w_")){
                String con = resStr.substring(resStr.indexOf("Q.PageInfo.playPageInfo ="),resStr.indexOf("Q.PageInfo.playPageInfo =")+200).replace(" ","");
                Pattern pattern = Pattern.compile("albumId:(\\d*)");
                Matcher matcher = pattern.matcher(con);
                if(matcher.find())
                    code = matcher.group(1);
            }else {
                int startIndex = resStr.lastIndexOf("Q.PageInfo.playPageInfo =");
                int endIndex = resStr.lastIndexOf("rewardAllowed");

                if(endIndex > startIndex) {
                    resStr = resStr.substring(startIndex, endIndex);
                    resStr = resStr.substring(resStr.indexOf("{"));
                    resStr = resStr.substring(0, resStr.lastIndexOf(",")) + "}";
                    JSONObject jsonResultObject = JSONObject.parseObject(resStr);

                    code = jsonResultObject.getString("albumId");
                    if("0".equals(code)){ // 此页面为电影
                        Pattern patternMovie = Pattern.compile("tvId\":(\\d*)");
                        Matcher matcherMovie = patternMovie.matcher(resStr);
                        if(matcherMovie.find())
                            code = matcherMovie.group(1);
                    }
//                    猜测是页面改版了， sourceId没了
//                    if ("0".equals(jsonResultObject.getString("sourceId"))) {
//                        code = jsonResultObject.getString("albumId");
//                    } else {
//                        code = jsonResultObject.getString("sourceId");
//                    }
                } else { // 网剧网大等结构不同  --》 现在貌似没用，网大电视剧网剧电影都一样了
                    String con = resStr.substring(startIndex,startIndex+200).replace(" ","");
                    // 如果是网剧，albumId不为0
                    Pattern patternDrama = Pattern.compile("albumId\":(\\d*)");
                    Matcher matcherDrama = patternDrama.matcher(con);
                    if(matcherDrama.find())
                        code = matcherDrama.group(1);
                    if("0".equals(code)){ // 此页面为电影
                        Pattern patternMovie = Pattern.compile("tvId\":(\\d*)");
                        Matcher matcherMovie = patternMovie.matcher(con);
                        if(matcherMovie.find())
                            code = matcherMovie.group(1);
                    }

                }

            }

        }
        return code;
    }
}
