package com.jinguduo.spider.service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jinguduo.spider.data.table.Category;
import com.jinguduo.spider.data.table.Platform;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.db.repo.PlatformRepo;
import com.jinguduo.spider.db.repo.ShowLogRepo;
import com.jinguduo.spider.db.repo.ShowRepo;

@Component
public class ShowLogService {

	private static final Logger log = LoggerFactory.getLogger(ShowLogService.class);

	@Autowired
	private ShowLogRepo showLogRepo;

	@Autowired
	private ShowRepo showRepo;

	@Autowired
	private PlatformRepo platformRepo;


	public ShowLog insert(ShowLog showLog) {
		assert showLog != null;
		assert showLog.getId() == null;
		// show log 数据不允许修改!
		if(showLog.getPlayCount() == null || showLog.getPlayCount() <= 0){
			log.debug("the show_log's play_count is null or zero:{}",showLog.getShowId());
			return null;
		}

		return showLogRepo.save(showLog);
	}

	public List<ShowLog> find(String code) {
		return showLogRepo.findTop24ByCodeOrderByIdDesc(code);
	}

	public List<List<Map>> find(String name, String startDate, String endDate) {

		List<List<Map>> returnList = Lists.newArrayList();

		List<Show> shows = showRepo.findByNameAndDepth(name, 1);

		try {
			Timestamp s = new Timestamp(DateUtils.parseDate(startDate + " 00:00:00", "yyyy-MM-dd HH:mm:ss").getTime());
			Timestamp e = new Timestamp(DateUtils.parseDate(endDate + " 23:59:59", "yyyy-MM-dd HH:mm:ss").getTime());
			for (Show show : shows) {

				Platform platform = platformRepo.findOne(show.getPlatformId());

				List<ShowLog> showLogs = showLogRepo.findByCodeAndCrawledAtBetween(show.getCode(), s, e);
				List<Map> maps = Lists.newArrayList();
				showLogs.forEach(showLog ->
						maps.add(ImmutableMap.of("play_count", showLog.getPlayCount(),
								"platform_name",platform.getName(),
								"crawled_at", DateFormatUtils.format(showLog.getCrawledAt(),"yyyy-MM-dd HH")))
				);
				returnList.add(maps);
			}

			Calendar starCal = Calendar.getInstance();
			starCal.setTime(new Date(s.getTime()));

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date(s.getTime()));

			long days=(e.getTime()-s.getTime())/(24*60*60*1000) + 1;

			List<Map> multiAll = Lists.newArrayList();
			for (int i = 0; i < days; i++) {
				calendar.set(Calendar.DATE,starCal.get(Calendar.DATE)+i);
				for (int j = 0; j < 24; j++) {
					String hour = String.format("%s %s",DateFormatUtils.format(calendar.getTime(), "yyyy-MM-dd"), j < 10 ? "0" + j : j);
					long hourPCTmp = 0L;
					Map map = Maps.newConcurrentMap();
					for (List<Map> maps : returnList) {//其中一个平台的数量,每个map包含播放量,抓取时间等
						Optional<Map> any = maps.stream()
								.filter(m -> hour.equals(m.get("crawled_at")))
								.findAny();
						hourPCTmp += Long.valueOf(any.orElse(ImmutableMap.of("play_count",0L)).get("play_count").toString());
					}
					map.put("platform_name","多平台");
					map.put("crawled_at",hour);
					map.put("play_count",hourPCTmp);
					multiAll.add(map);
				}
			}
			returnList.add(0,multiAll);

		} catch (ParseException e) {
			log.error(e.getMessage(), e);
		}
		return returnList;
	}

	public Map<String,Object> findExact(Integer type, String name, String start, String end) {
	    
	    Map<String,Object> res = Maps.newHashMap();
	    
        List<String> platforms = Lists.newArrayList();
        
        List<Show> shows = Lists.newArrayList();
        if(type==0){
            shows = showRepo.findByNameAndDepth(name, 1);
        }else if(type==1){
            List<Show> list = showRepo.findByCodeOrderById(name);
            if(!CollectionUtils.isEmpty(list)){
                shows.add(list.get(0));
            }
        }
        
        try {
            Timestamp s = new Timestamp(DateUtils.parseDate(start,"yyyy-MM-dd HH:mm").getTime());
            Timestamp e = new Timestamp(DateUtils.parseDate(end,"yyyy-MM-dd HH:mm").getTime());
            
            for (Show show : shows) {
                if(StringUtils.equals(show.getCategory(), Category.MEDIA_DATA.name())){
                    continue;
                }
                Platform platform = platformRepo.findOne(show.getPlatformId());
                Map<String, Object> collect = Maps.newHashMap();
                List<String> times = Lists.newArrayList();//时间
                List<Long> pcs = Lists.newArrayList();//播放量
                
                showLogRepo.findByCodeAndPlatformIdAndCrawledAtBetween(show.getCode(), show.getPlatformId(),s, e).stream().sorted((x,y) -> x.getCrawledAt().compareTo(y.getCrawledAt())).forEach(sl ->{
                    times.add(DateFormatUtils.format(sl.getCrawledAt(),"yyyy-MM-dd HH:mm"));
                    pcs.add(sl.getPlayCount());
                });
                collect.put("times", times);
                collect.put("playcounts", pcs);
                res.put(platform.getName(),collect);
                platforms.add(platform.getName());
            }
            res.put("platforms", platforms);
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        return res;
    }
}
