package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by lc on 2019/12/4
 */
@Data
@Table(name = "dangdang_category_relation")
@Entity
public class DangdangCategoryRelation implements Serializable {
    @Id
    @GeneratedValue
    private Integer id;
    private String bookCode;
    private Integer categoryId;
}
