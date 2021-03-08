package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/***
 * 豆瓣(全部)短评数
 */
@Data
@Entity
@Table( name = "douban_comments_text")
public class DoubanCommentsText implements Serializable {

    private static final long serialVersionUID = 5198192972461199894L;

    @Id
    @GeneratedValue
    private Integer id;

    @Column
    private String code;

    @Column
    private String type;// F:想看 F:在看 P:看过

    @Column(unique = true)
    private Long commentId;

    @Column
    private String nickName;

    @Column
    private Integer star;

    @Column
    private Timestamp createTime;

    @Column
    private String content;

    @Column
    private Integer up;

    @Column(nullable = false, updatable = false)
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    @Column(nullable = false, updatable = false, insertable = false)
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());

    public DoubanCommentsText() {
    }
}
