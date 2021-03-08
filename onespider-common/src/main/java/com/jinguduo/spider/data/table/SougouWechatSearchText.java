package com.jinguduo.spider.data.table;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/6/26
 * Time:16:59
 * ！！！！！！！！！！！！！！切忌修改！！！！！！！！！！！！！！
 */
@JsonPropertyOrder({
        "title", "author", "summary", "compositor", "articleTime","url","ts","content"
})
@Data
public class SougouWechatSearchText implements Serializable {
    private static final long serialVersionUID = 5374382948571524218L;
    private String title;
    //简介
    private String summary;
    private String url;
    private String author;
    private Date articleTime;
    //排序
    private int compositor;
    //html
    private String content;
    //createTime
    private Long ts = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000;
}
