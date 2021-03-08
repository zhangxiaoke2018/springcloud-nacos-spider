package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.WechatArticleAccessory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @DATE 2018/10/12 3:57 PM
 */
@Repository
public interface WechatArticleAccessoryRepo extends JpaRepository<WechatArticleAccessory, Long> {
}
