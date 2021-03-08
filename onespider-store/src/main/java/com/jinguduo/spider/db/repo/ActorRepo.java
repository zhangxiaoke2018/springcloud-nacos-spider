package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.Actor;

import java.util.List;

/**
 * Created by jack on 2016/12/21.
 */
public interface ActorRepo extends JpaRepository<Actor, Integer>{

    Actor findFirstByCodeAndNameOrderById(String code, String actorName);

    List<Actor> findListByLinkedId(int actorId);
}
