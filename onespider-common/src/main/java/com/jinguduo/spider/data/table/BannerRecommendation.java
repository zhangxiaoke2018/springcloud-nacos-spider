package com.jinguduo.spider.data.table;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.format.annotation.DateTimeFormat;

import com.jinguduo.spider.common.constant.CommonEnum.BannerType;

import lombok.Data;

@Entity
@Table(name = "banner_recommendation")
@Data
public class BannerRecommendation {
	
    @Id
    @GeneratedValue
    private Integer id;
    private String code;
    private Integer platformId;
    private Integer bannerType;
    @CreatedDate
    @DateTimeFormat
    private Date crawledAt = new Timestamp(System.currentTimeMillis());
    public BannerRecommendation() {
    	
    }

    public BannerRecommendation(String code,Integer platformId,BannerType bannerType) {
    	this.code = code;
    	this.platformId = platformId;
    	this.bannerType = bannerType.getCode();
    }
    
    public BannerRecommendation(String code,Integer platformId,String bannerType) {
    	this.code = code;
    	this.platformId = platformId;
    	this.bannerType = BannerType.valueOf(bannerType).getCode();
    }
}
