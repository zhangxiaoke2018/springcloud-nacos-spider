package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.WechatSearchText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * @DATE 2018/8/8 14:18
 */
@Repository
public interface WechatSearchTextRepo extends JpaRepository<WechatSearchText, Long> {

    WechatSearchText findByAuthorAndTitle(String author, String title);
    WechatSearchText findByAuthorAndArticleTime(String author, Date articleTime);


}
