package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by lc on 2019/3/12
 */
@Data
@Table(name = "comic_episode_info")
@Entity
public class ComicEpisodeInfo implements Serializable {
    @Id
    @GeneratedValue
    private Integer id;
    private String code;
    private Date day;
    private Integer platformId;
    private String name;
    private Integer episode;
    private Integer vipStatus;
    private Date comicCreatedTime;
    private Integer likeCount;
    private String chapterId;
    private Integer comment;
}
