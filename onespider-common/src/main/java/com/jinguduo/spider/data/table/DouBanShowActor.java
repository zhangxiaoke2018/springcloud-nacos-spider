package com.jinguduo.spider.data.table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

/**
 * Created by csonezp on 2016/8/16.
 */
@Entity
@Table(name = "douban_show_actor", uniqueConstraints = {@UniqueConstraint(columnNames = {"actor_id", "show_id"})})
@Getter
@Setter
@ToString
public class DouBanShowActor {
    @Id
    @GeneratedValue
    private Integer id;
    @Column(name = "show_id")
    private Integer showId;
    @Column(name = "actor_id")
    private Integer actorId;
}
