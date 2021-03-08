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
 * @DATE 2017年8月7日 下午3:39:39
 *
 */
@Data
@Entity
@Table(name = "gdi_actor_logs" )
public class GdiActorLogs implements Serializable {
    private static final long serialVersionUID = -7349651391968412594L;
    @Id
    @GeneratedValue
    private Integer id;
    private Integer linkedId;
    private String name;
    private Date day;
    
    private Integer baiduIndex;
    private Integer weiboIndex;
    private Integer wechatArticleNum;
    
    private String otherInfo;
    private Integer gdiSum;
    
    private Long mainPlayCount;
    private Long subPlayCount;
    private Long varietyPlayCount;
    private String showIds;
}
