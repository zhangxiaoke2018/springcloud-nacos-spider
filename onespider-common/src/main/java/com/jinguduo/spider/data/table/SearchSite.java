package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "media_search_site")
@Data
public class SearchSite {

    @Id
    @GeneratedValue
    private Long id;
    private String searchUrlPrefix;
    private String siteName;
    private Integer enable;//0:启用(默认)，1：不启用
    private Timestamp createTime = new Timestamp(System.currentTimeMillis());
    private Timestamp modifyTime = new Timestamp(System.currentTimeMillis());

    public SearchSite() {
    }

    public SearchSite(String searchUrlPrefix, String siteName) {
        this.searchUrlPrefix = searchUrlPrefix;
        this.siteName = siteName;
    }
}
