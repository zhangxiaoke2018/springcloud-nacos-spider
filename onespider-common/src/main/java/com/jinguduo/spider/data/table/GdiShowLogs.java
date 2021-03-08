package com.jinguduo.spider.data.table;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * 
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @author liuxinglong
 * @DATE 2017年7月11日 下午5:12:24
 *
 */
@Data
@Entity
@Table(name = "gdi_show_logs" )
public class GdiShowLogs implements Serializable {
    private static final long serialVersionUID = 5530132182011273586L;
    @Id
    @GeneratedValue
    private Integer id;
    private Integer linkedId;
    private String name;
    private String category;
    private String PlatformMergeCn;
    private Date day;
    
    private Long playCountIncrease;
    private Long vipPlayCount;
    private Integer baiduSearchIndex;
    private Integer weiboDataIndex;
    private Float doubanGradeIndex;
    private Integer doubanComment;
    private Integer wechatArticleNum;
    private Integer commentCountIncrease;
    private Integer barrageIncrease;
    private Integer gdiSum;
}
