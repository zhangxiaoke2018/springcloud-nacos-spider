package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.MaoyanActor;

public interface MaoyanActorRepo extends JpaRepository<MaoyanActor, Integer> {

    MaoyanActor findByCode(String code);

}