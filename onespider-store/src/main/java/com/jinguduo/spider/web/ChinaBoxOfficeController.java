package com.jinguduo.spider.web;


import com.jinguduo.spider.data.table.BoxOfficeLogs;
import com.jinguduo.spider.service.ChinaBoxOfficeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cbooo")
public class ChinaBoxOfficeController {


    @Autowired
    private ChinaBoxOfficeService chinaBoxOfficeService;

    @PostMapping("/boxOffice")
    public Object saveBoxOffice(@RequestBody BoxOfficeLogs boxOffice){

        return chinaBoxOfficeService.saveBoxOffice(boxOffice);
    }
}
