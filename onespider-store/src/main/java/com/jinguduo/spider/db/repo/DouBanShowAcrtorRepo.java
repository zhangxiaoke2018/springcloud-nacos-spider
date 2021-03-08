package com.jinguduo.spider.db.repo;

import org.springframework.data.repository.CrudRepository;

import com.jinguduo.spider.data.table.DouBanShowActor;

import java.util.List;
import java.util.Optional;

/**
 * Created by csonezp on 2016/8/16.
 */
public interface DouBanShowAcrtorRepo extends CrudRepository<DouBanShowActor, Integer> {
    DouBanShowActor findByShowIdAndActorId(Integer showId, Integer actorId);
    Optional<List<DouBanShowActor>> findByActorId(Integer actorId);
    Optional<List<DouBanShowActor>> findByShowId(Integer showId);
}
