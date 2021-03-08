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
 * @DATE 31/07/2017 14:20
 */
@Data
@Table(name = "comic_u17")
@Entity
public class ComicU17 implements Serializable {

    private static final long serialVersionUID = 4311326935103741495L;

    @Id
    @GeneratedValue
    private Integer id;

    private String code;

    private Long totalClick;

    private Integer monthlyTicket;

    private Integer totalLike;

    private Integer commentCount;

    private Date day;

    public void setStrTotalClick(String totalClick){

        if(totalClick.contains("万")){
            this.totalClick = (long)(Double.valueOf(totalClick.replace("万","")) * 10000);
        }else if(totalClick.contains("亿")){
            this.totalClick = (long)(Double.valueOf(totalClick.replace("亿","")) * 100000000);
        }else {
            this.totalClick = Double.valueOf(totalClick).longValue();
        }


    }

}
