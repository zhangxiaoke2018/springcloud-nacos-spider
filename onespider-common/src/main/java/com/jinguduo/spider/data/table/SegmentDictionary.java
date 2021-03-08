package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 28/04/2017 14:55
 */
@Data
@Entity
@Table
public class SegmentDictionary implements Serializable {

    private static final long serialVersionUID = 6374243176073039317L;

    @Id
    @GeneratedValue
    private Integer id;

    private String word;

    private String nature;

    private Integer frequency;

    private Integer hasModified;

    private Integer disable;

    public SegmentDictionary() {
    }

    public SegmentDictionary(Integer id, String word, String nature, Integer frequency, Integer hasModified, Integer disable) {
        this.id = id;
        this.word = word;
        this.nature = nature;
        this.frequency = frequency;
        this.hasModified = hasModified;
        this.disable = disable;
    }

}
