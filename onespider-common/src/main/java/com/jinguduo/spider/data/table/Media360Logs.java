package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by lc on 2017/5/10.
 */

@Entity
@Table(name = "media_360_logs")
@Data
public class Media360Logs implements Serializable {

    private static final long serialVersionUID = -8916960626628294944L;
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Integer id;

    @Column(name = "code")
    private String code;

    @Column(name = "media_day")
    private Date mediaDay;

    @Column(name = "media_count")
    private Integer mediaCount;

    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @Column(name = "updated_at", updatable = false)
    private Date updatedAt;

}