package com.jinguduo.spider.data.table;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/8/30
 * Time:11:24
 */
public enum ComicSex {
    unKnowSex("未知",(byte)0),
    boy("男",(byte)1),
    girl("女",(byte)2),
    all("不区分男女",(byte)3);

    private String desc ;
    private Byte sqlEnum ;

    private ComicSex( String desc , Byte sqlEnum ){
        this.desc = desc ;
        this.sqlEnum = sqlEnum ;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Byte getSqlEnum() {
        return sqlEnum;
    }

    public void setSqlEnum(Byte sqlEnum) {
        this.sqlEnum = sqlEnum;
    }
}
