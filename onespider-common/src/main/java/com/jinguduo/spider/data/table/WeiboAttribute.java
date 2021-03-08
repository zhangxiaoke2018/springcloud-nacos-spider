package com.jinguduo.spider.data.table;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 2017-04-19 18:28:35
 */
@Data
@Entity
@Table
public class WeiboAttribute implements Serializable {

    private static final long serialVersionUID = -2611105204736579654L;

    @Id
    @GeneratedValue
    private Integer id;

    @NotBlank
    private String keyword;

    @NotBlank
    private String value;

    private Double man;

    private Double woman;

    private Integer zeroToTwelve;

    private Integer twelveToEighteen;

    private Integer nineteenToTwentyFour;

    private Integer twentyFiveToThirtyFour;

    private Integer thirtyFiveToFifty;

    private Integer otherAge;

    private Integer tripTag;

    private Integer foodTag;

    private Integer humorTag;

    private Integer recreationTag;

    private Integer starTag;


    //白羊座
    private Double aries;

    //金牛座
    private Double taurus;

    //双子座
    private Double gemini;

    //巨蟹座
    private Double cancer;

    //狮子座
    private Double leo;

    //处女座
    private Double virgo;

    //天秤座
    private Double libra;

    //天蝎座
    private Double scorpio;

    //射手座
    private Double sagittarius;

    //摩羯座
    private Double capricornus;

    //水瓶座
    private Double aquarius;

    //双鱼座
    private Double pisces;

    private String code;


    public void setValueAndFill(String value) {
        this.value = value;

        JSONObject data = JSONObject.parseObject(value);

        JSONObject sex = data.getJSONObject("sex").getJSONObject("key2");
        this.man = sex.getDouble("man");
        this.woman = sex.getDouble("woman");

        JSONObject age = data.getJSONObject("age").getJSONObject("key2").getJSONObject("0");

        this.zeroToTwelve = age.getInteger("0-12");
        this.twelveToEighteen = age.getInteger("12-18");
        this.nineteenToTwentyFour = age.getInteger("19-24");
        this.twentyFiveToThirtyFour = age.getInteger("25-34");
        this.thirtyFiveToFifty = age.getInteger("35-50");
        this.otherAge = age.getInteger("other");

        JSONObject tag = data.getJSONObject("tag").getJSONObject("key2").getJSONObject("0");

        this.tripTag = tag.getInteger("旅游");
        this.foodTag = tag.getInteger("美食");
        this.humorTag = tag.getInteger("搞笑幽默");
        this.recreationTag = tag.getInteger("娱乐");
        this.starTag = tag.getInteger("名人明星");

        JSONObject star = data.getJSONObject("star").getJSONObject("key2").getJSONObject("0");
        this.aries = star.getDouble("白羊座");
        this.taurus = star.getDouble("金牛座");
        this.gemini = star.getDouble("双子座");
        this.cancer = star.getDouble("巨蟹座");
        this.leo = star.getDouble("狮子座");
        this.virgo = star.getDouble("处女座");
        this.libra = star.getDouble("天秤座");
        this.scorpio = star.getDouble("天蝎座");
        this.sagittarius = star.getDouble("射手座");
        this.capricornus = star.getDouble("摩羯座");
        this.aquarius = star.getDouble("水瓶座");
        this.pisces = star.getDouble("双鱼座");

    }
}
