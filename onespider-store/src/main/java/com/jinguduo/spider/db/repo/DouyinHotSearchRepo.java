package com.jinguduo.spider.db.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.table.DouyinHotSearch;

@Component
public interface DouyinHotSearchRepo extends CrudRepository<DouyinHotSearch, Integer> {

	DouyinHotSearch findOneByOrdinalAndActiveTime(Integer ordinal, String activeTime);

}
