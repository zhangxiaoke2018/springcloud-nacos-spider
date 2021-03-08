package com.jinguduo.spider.data.table;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by lc on 2017/6/7.
 */
@Data
public class AliasPolymerization implements Serializable {
    private static final long serialVersionUID = 1325408772866077660L;
    private Integer relevanceId;
    private Byte type;
    private List<Alias> aliases;
    private List<RelationKeywords> relationKeywords;
}
