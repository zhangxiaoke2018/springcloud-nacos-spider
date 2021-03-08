package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.WechatNewText;
import com.jinguduo.spider.data.table.WechatSearchText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @DATE 2018/8/8 14:18
 */
@Repository
public interface WechatNewTextRepo extends JpaRepository<WechatNewText, Long> {

    WechatNewText findByAuthorAndTitle(String author, String title);
    WechatNewText findByAuthorAndArticleTime(String author, Date articleTime);

    @Query(value = "select * from wechat_new_texts where crawled_at > DATE_SUB(now(), INTERVAL 2 HOUR) order by crawled_at desc; ",nativeQuery = true)
    List<WechatNewText> findAllOrderByCrawledAt();
}
