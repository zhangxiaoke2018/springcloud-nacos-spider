package com.jinguduo.spider.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jinguduo.spider.data.table.Actor;
import com.jinguduo.spider.db.repo.ActorRepo;

/**
 * Created by jack on 2016/12/20.
 */

@Service
public class ActorService {

    @Autowired
    ActorRepo actorRepo;

    public void save(Actor actor){
        if (null == actor){
            return;
        }
        actorRepo.save(actor);
        return;
    }

    public boolean exist(String code, String actorName){
        Actor actor = actorRepo.findFirstByCodeAndNameOrderById(code, actorName);
        return actor != null;
    }

    public List<Actor> getActorListByActorId(int actorId){
        return actorRepo.findListByLinkedId(actorId);
    }

}
