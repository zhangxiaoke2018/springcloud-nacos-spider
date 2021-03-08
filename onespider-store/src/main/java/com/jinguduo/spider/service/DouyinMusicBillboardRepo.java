package com.jinguduo.spider.service;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.table.DouyinMusicBillboard;

@Component
public interface DouyinMusicBillboardRepo extends CrudRepository<DouyinMusicBillboard, Integer> {

	DouyinMusicBillboard findOneByOrdinalAndActiveTime(Integer ordinal, String activeTime);

}
