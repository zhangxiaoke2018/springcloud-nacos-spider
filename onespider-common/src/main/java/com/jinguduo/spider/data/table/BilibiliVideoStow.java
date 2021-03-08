package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 27/07/2017 10:51
 */
@Data
@Entity
@Table
public class BilibiliVideoStow implements Serializable {

    @Id
    @GeneratedValue
    private Integer id;

    private String code;

    private String dataId;

    private String title;

    private String url;

    private Integer playCount;

    private Integer danmakuCount;

    private Date postDate;

    private String postUser;

    public BilibiliVideoStow(){}

    public BilibiliVideoStow(String title, String url){
        this.title = title;
        if(url.startsWith("//")){
            url = "http:" + url;
        }
        this.url = url;
    }
    public void setStrPlayCount(String playCount){

        if(playCount.contains("万")){
            Double aDouble = Double.valueOf(playCount.replace("万", ""));
            this.playCount = (int)(aDouble * 10000);
        }else {
            this.playCount = (int)(Double.valueOf(playCount) * 10000);
        }

    }

}
