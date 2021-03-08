package com.jinguduo.spider.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.StoreMainApplication;
import com.jinguduo.spider.data.table.Show;

import lombok.extern.apachecommons.CommonsLog;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@CommonsLog
public class ShowServiceTests {

    @Autowired
    private ShowService showService;

    @Test
    public void testContent(){
        Assert.notNull(showService);}

//    @Before
    public void setUp(){
        Show show = new Show();
        show.setName("sb1");
        show.setCode("aHR0cDovL3dlaWJvLmNvbS91LzU3MjU0OTk5Mjc/cmVmZXJfZmxhZz0xMDAxMDMwMTAxXw==");
        show.setPlatformId(1);
        show.setCategory("MEDIA_DATA");
        Show show2 = new Show();
        show2.setName("sb2");
        show2.setCode("aHR0cDovL3dlaWJvLmNvbS91LzU3MjU0OTk5Mjc/cmVmZXJfZmxhZz0xMDAxMDMwMTAxXw==");
        show2.setPlatformId(2);
        show2.setCategory("MEDIA_DATA");
        showService.insertOrUpdate(show);
        showService.insertOrUpdate(show2);
    }

    /**
     * 保存 null
     */
    @Test
    public void saveNull(){
        Show show = showService.insertOrUpdate(null);
        Assert.isNull(show);
    }



    @Test
    public void depth1ShowIdNotNull(){
        Show show = new Show();
        show.setId(1);
        show.setDepth(1);
        show.setName("sb1");
        show.setCode("aHR0cDovL3dlaWJvLmNvbS91LzU3MjU0OTk5Mjc/cmVmZXJfZmxhZz0xMDAxMDMwMTAxXw==");
        show.setPlatformId(1);
        show.setLinkedId(111);
        show.setCategory("NETWORK_MOVIE");

        showService.insertOrUpdate(show);

        Show newShow = new Show();
        newShow.setId(1);
        newShow.setDepth(1);
        newShow.setName("sb2");
        newShow.setCode("aHR0cDovL3dlaWJvLmNvbS91LzU3MjU0OTk5Mjc/cmVmZXJfZmxhZz0xMDAxMDMwMTAxXw==");
        newShow.setPlatformId(1);
        newShow.setLinkedId(111);
        newShow.setCategory("NETWORK_MOVIE");

        showService.insertOrUpdate(newShow);

        Show finalShow = showService.findById(1);

        Assert.isTrue("sb2".equals(finalShow.getName()));

    }

    @Test
    public void testNoMerge(){
        Show o = new Show();
        o.setName("不可思议学园");
        o.setCode("28k8q9smjo2qlxz");
        o.setOnBillboard(Boolean.TRUE);
        o.setCheckedStatus(1);
        o.setCategory("NETWORK_MOVIE");
        o.setDepth(1);
        o.setPlatformId(1);
        o.setLinkedId(111);
        //保存一个已通过审核的剧
        Show oshow = showService.insertOrUpdate(o);
        log.info("已存在的show: " + oshow);

        Show n = new Show();
        n.setName("不可思议学园");
        n.setCode("569058000");
        n.setOnBillboard(Boolean.TRUE);
        n.setCheckedStatus(1);
        n.setCategory("NETWORK_MOVIE");
        n.setDepth(1);
        n.setPlatformId(2);
        n.setLinkedId(111);
        n.setSource(1);


        Show nshow = showService.insertOrUpdate(n);
        log.info("新填入show: " + nshow);

    }









}
