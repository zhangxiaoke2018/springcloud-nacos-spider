package com.jinguduo.spider.data.table;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 19/04/2017 18:25
 */
@Data
@Entity
@Table
public class WeiboProvinceCompare implements Serializable {

    private static final long serialVersionUID = -2638605204736579654L;

    @Id
    @GeneratedValue
    private Integer id;

    @NotBlank
    private String keyword;

    @NotBlank
    private String value;

    private String type;

    private Integer beijing;

    private Integer guangdong;

    private Integer jiangsu;

    private Integer shandong;

    private Integer shanghai;

    private Integer zhejiang;

    private Integer henan;

    private Integer sichuan;

    private Integer hebei;

    private Integer liaoning;

    private Integer shaanxi;

    private Integer hubei;

    private Integer anhui;

    private Integer hunan;

    private Integer fujian;

    private Integer heilongjiang;

    private Integer shanxi;

    private Integer chongqing;

    private Integer yunnan;

    private Integer jiangxi;

    private Integer tianjin;

    private Integer jilin;

    private Integer guangxi;

    private Integer neimongol;

    private Integer gansu;

    private Integer guizhou;

    private Integer xinjiang;

    private Integer hongkong;

    private Integer hainan;

    private Integer ningxia;

    private Integer taiwan;

    private Integer xizang;

    private Integer qinghai;

    private Integer macau;

    private String code;

    private Long total = 0L;

    public void fill(String type) throws IllegalArgumentException, IllegalAccessException{

        this.type = type;

        JSONObject jsonData = JSONObject.parseObject(this.value);
        JSONObject proJson = jsonData.getJSONObject(type);

        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            JSONObject o = proJson.getJSONObject(fieldName);
            if(o != null){
                int num = 0;
                if(type.equals("user")){
                    num = o.getInteger("num");
                }else {
                    num = o.getIntValue("ct");
                }
                this.total += num;
                field.set(this, num);
            }
        }
    }
}
