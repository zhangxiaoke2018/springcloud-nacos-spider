package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.bookProject.JingdongBook;
import com.jinguduo.spider.db.repo.JingdongBookRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lc on 2020/4/2
 */
@Service
public class JingdongBookService {
    @Autowired
    private JingdongBookRepo jingdongBookRepo;

    public List<JingdongBook> findAll() {
        return jingdongBookRepo.findAll();
    }
}
