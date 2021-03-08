package com.jinguduo.spider.db.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.jinguduo.spider.data.table.DouBanActor;

import java.util.List;
import java.util.Optional;

/**
 * Created by csonezp on 2016/8/15.
 */
public interface DouBanActorRepo extends PagingAndSortingRepository<DouBanActor, Integer> {

    Optional<DouBanActor> findById(Integer id);

    List<DouBanActor> findAllByName(String name);


    DouBanActor findByUrl(String url);

    Optional<List<DouBanActor>> findByNameLike(String name);
}
