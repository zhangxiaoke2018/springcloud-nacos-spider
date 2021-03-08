package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.bookProject.ChildrenBook;
import com.jinguduo.spider.data.table.bookProject.DoubanBook;
import com.jinguduo.spider.db.repo.ChildrenBookRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by lc on 2019/12/4
 */
@Service
public class ChildrenBookService {
    @Autowired
    private ChildrenBookRepo repo;

    public ChildrenBook saveOrUpdate(ChildrenBook cb) {
        if (null == cb.getPlatformId() || StringUtils.isEmpty(cb.getCode())) return cb;

        ChildrenBook old = repo.findByPlatformIdAndCode(cb.getPlatformId(), cb.getCode());
        if (null == old) {
            this.setSimpleName(cb);
            return repo.save(cb);
        }
        if (StringUtils.isNotEmpty(cb.getName())) {
            //如果名称更新，才执行获取simpleName操作，防止手动修正名称后恢复
            if (!StringUtils.equals(cb.getName(), old.getName()) || null == old.getSimpleName()) {
                this.setSimpleName(cb);
                old.setSimpleName(cb.getSimpleName());
            }
            old.setName(cb.getName());
        }
        if (StringUtils.isNotEmpty(cb.getIsbn())) {
            old.setIsbn(cb.getIsbn());
        }
        if (StringUtils.isNotEmpty(cb.getHeaderImg())) {
            old.setHeaderImg(cb.getHeaderImg());
        }
        if (StringUtils.isNotEmpty(cb.getIntro())) {
            old.setIntro(cb.getIntro());
        }
        if (StringUtils.isNotEmpty(cb.getAuthor())) {
            old.setAuthor(cb.getAuthor());
        }
        if (StringUtils.isNotEmpty(cb.getPublisher())) {
            old.setPublisher(cb.getPublisher());
        }
        if (null != cb.getPublishTime()) {
            Date saveTime = null == old.getPublishTime() || cb.getPublishTime().compareTo(old.getPublishTime()) == -1 ? cb.getPublishTime() : old.getPublishTime();
            old.setPublishTime(saveTime);
        }
        return repo.save(old);
    }

    /**
     * 不能写在pojo中。防止每次select 会执行此方法
     */
    private void setSimpleName(ChildrenBook cb) {
        if (StringUtils.isEmpty(cb.getName())) return;
        String sName = cb.getName().trim();
        //去除 "（" 中文括号
        int subEnd = StringUtils.indexOf(sName, "（");
        if (-1 != subEnd) {
            sName = StringUtils.substring(sName, 0, subEnd);
        }
        //去除 "：" 中文冒号
        int subEnd2 = StringUtils.indexOf(sName, "：");
        if (-1 != subEnd2) {
            sName = StringUtils.substring(sName, 0, subEnd2);
        }
        //去除 "—" 全角破折号
        int subEnd3 = StringUtils.indexOf(sName, "—");
        if (-1 != subEnd3) {
            sName = StringUtils.substring(sName, 0, subEnd3);
        }
        //去除 " " 空格
        int subEnd4 = StringUtils.indexOf(sName, " ");
        if (subEnd4 > 1) {
            sName = StringUtils.substring(sName, 0, subEnd4);
        }

        cb.setSimpleName(sName);
    }


    public List<Object[]> findAllNewIsbn() {

        List<Object[]> res = repo.findIsbnNotExistDouban();
        return res;
    }

    public List<ChildrenBook> findAll() {
        return repo.findAll();
    }

    //标记该童书不再进行douban_search 任务
    public void tagDoubanQueryStatus(DoubanBook doubanBook) {
        if (null == doubanBook || StringUtils.isEmpty(doubanBook.getIsbn())) return;

        List<ChildrenBook> list = repo.findByIsbn(doubanBook.getIsbn());
        if (null == list || list.isEmpty()) return;

        for (ChildrenBook book : list) {
            book.setDoubanQueryStatus((byte) 1);
            repo.save(book);
        }


    }

    public List<Object[]> findRankLess150BookInfo() {
        List<Object[]> list = repo.findRankLess150BookInfo();
        return list;
    }
}
