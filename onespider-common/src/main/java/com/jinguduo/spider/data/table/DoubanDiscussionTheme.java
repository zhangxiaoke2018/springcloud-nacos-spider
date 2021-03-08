package com.jinguduo.spider.data.table;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 讨论区，暂时不开发
 */
@Builder
@Data
public class DoubanDiscussionTheme implements Serializable {

    private static final long serialVersionUID = -3226191786835009372L;

    @Id
    @GeneratedValue
    private Integer id;

    @NotBlank
    private String code;
    //标题
    private String title;
    //期刊
    private String serials;
    //讨论区数量
    private Integer discussionCount;


}
