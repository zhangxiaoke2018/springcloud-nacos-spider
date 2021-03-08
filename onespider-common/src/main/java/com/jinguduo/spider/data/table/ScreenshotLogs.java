package com.jinguduo.spider.data.table;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 19/05/2017 17:18
 */
@Entity
@Table
@Data
public class ScreenshotLogs implements Serializable {

    private static final long serialVersionUID = 8581701214157288229L;

    @Id
    @GeneratedValue
    private Integer id;

    private String code;

    @NotBlank
    private String uuid;

    @Lob
    @Basic(fetch= FetchType.LAZY)
    @Column(name = "content" ,columnDefinition = "TEXT")
    private String content;

}
