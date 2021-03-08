package com.jinguduo.spider.data.table;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/6/27
 * Time:14:57
 * ！！！！！！！！！！！！！！切忌修改！！！！！！！！！！！！！！
 */
@JsonPropertyOrder({
        "id", "title", "url", "ts", "content"
})
@Data
public class SougouWechatArticleText implements Serializable {
    private static final long serialVersionUID = -2047511713629154861L;
    private String id;
    private String url;
    private String title;
    private String content;
    private Long ts = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()/1000;

}
