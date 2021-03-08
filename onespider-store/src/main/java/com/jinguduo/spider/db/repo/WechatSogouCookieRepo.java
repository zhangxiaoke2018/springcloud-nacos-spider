package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.WechatSogouCookie;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

/**
 * Created by lc on 2019/4/30
 */
@Component
public interface WechatSogouCookieRepo extends CrudRepository<WechatSogouCookie, Integer> {

}
