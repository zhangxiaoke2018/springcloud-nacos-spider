package com.jinguduo.spider.data.table;


import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@Table(name = "comic_comment_text")
@Entity
public class ComicCommentText  implements Serializable {

    private static final long serialVersionUID = -521765168536938604L;

    @Id
    @GeneratedValue
    private Integer id;
    private Long commentId;
    private String userId;
    private String userName;
    private String content;
    private Integer revertCount;
    private String code;
    private Date commentCreateTime;
    private Integer platformId;
    private Long supportCount;
    private Date day;

}
