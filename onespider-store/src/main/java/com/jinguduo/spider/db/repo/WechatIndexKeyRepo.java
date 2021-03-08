package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.WechatIndexKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @DATE 2018/10/11 11:10 AM
 */
@Repository
public interface WechatIndexKeyRepo extends JpaRepository<WechatIndexKey, Integer> {

    WechatIndexKey findByOpenId(String openId);

}
