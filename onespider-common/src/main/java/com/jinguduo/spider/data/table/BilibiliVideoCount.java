package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/8/7
 * Time:11:18
 */
@Data
@Entity
@Table
public class BilibiliVideoCount {
    @Id
    @GeneratedValue
    private Integer id;

    private String code;

    private Byte type;

    private Integer count;

    @Column(name = "create_time", updatable = false)
    private Date create_time;

    @Column(name = "update_time", updatable = false)
    private Date update_time;

    /**
     * type本来很少，没用枚举。现在多了。。
     *
     *类型：0.所有，1音乐，2舞蹈，3生活，4鬼畜，5娱乐，6电视剧，7电影，8动画，9番剧相关
     *      10国创，11游戏，12科技，13时尚，14广告
     * */


}
