package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by lc on 2019/12/4
 */
@Data
@Table(name = "dangdang_category")
@Entity
public class DangdangCategory implements Serializable {
    @Id
    private Integer id;
    private Integer parentId;
    private String name;
}
