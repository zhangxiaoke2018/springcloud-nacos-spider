package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.CartoonBulletin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by lc on 2020/4/9
 */
@Repository
public interface CartoonBulletinRepo extends JpaRepository<CartoonBulletin, Integer> {
    CartoonBulletin findFirstByUrl(String url);
}
