package com.jinguduo.spider.service;

import com.google.common.collect.Lists;
import com.jinguduo.spider.data.table.DouBanActor;
import com.jinguduo.spider.data.table.DouBanShow;
import com.jinguduo.spider.data.table.DouBanShowActor;
import com.jinguduo.spider.db.repo.DouBanActorRepo;
import com.jinguduo.spider.db.repo.DouBanShowAcrtorRepo;
import com.jinguduo.spider.db.repo.DouBanShowRepo;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by csonezp on 2016/8/16.
 */
@CommonsLog
@Service
public class DouBanService {
    @Autowired
    DouBanActorRepo douBanActorRepo;
    @Autowired
    DouBanShowRepo showRepo;
    @Autowired
    DouBanShowAcrtorRepo showActorRepo;


    public void addShow(DouBanShow douBanShow) {

        if(douBanShow == null || StringUtils.isBlank(douBanShow.getName())){
            return;
        }

        DouBanShow oldShow = showRepo.findByCode(douBanShow.getCode());
        if (oldShow != null) {
            //如果已经存在，则更新
            douBanShow.setId(oldShow.getId());
        }
        showRepo.save(douBanShow);
        
        Integer showId = douBanShow.getId();
        List<DouBanActor> databaseActors = Lists.newArrayList();
        Optional<List<DouBanShowActor>> showActors = showActorRepo.findByShowId(showId);
        if (showActors.isPresent()) {
            List<DouBanShowActor> databaseShowActors = showActors.get();
            //读取数据库中该剧所有的演员
            databaseShowActors.forEach(douBanShowActor -> {
                Optional<DouBanActor> actorOpt = douBanActorRepo.findById(douBanShowActor.getActorId());
                if(actorOpt.isPresent()) {
                    databaseActors.add(actorOpt.get());
                }
            });
        }

        List<DouBanActor> actors = douBanShow.getActors();
        actors.forEach(newActor -> {
            Integer actorId;
            DouBanActor oldActor = null;

            if (StringUtils.isNotBlank(newActor.getUrl())) {
                oldActor = douBanActorRepo.findByUrl(newActor.getUrl());
            } else {
                //在根据这个名字查出的列表中选取第一个不带url的
                List<DouBanActor> oldActors = douBanActorRepo.findAllByName(newActor.getName());
                oldActors = oldActors.stream().filter(douBanActor -> StringUtils.isEmpty(douBanActor.getUrl())).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(oldActors)){
                    oldActor = oldActors.get(0);
                }

            }


            if (oldActor != null) {

                //如果演员已经存在，则无需插入演员，只要添加关联即可
                BeanUtils.copyProperties(oldActor,newActor);
                newActor.setId(oldActor.getId());
                newActor.setName(oldActor.getName());
            } else {
                //如果抓取到的演员url不为空，此时会有一个特殊情况，即该演员一开始没有url，后来突然有了rul,这是oldActor会为空
                //则此时需要先对有url的演员进行一下检测，如果根据此演员的名字可以在数据库中查到，那就是这种情况
                databaseActors.forEach(douBanActor -> {
                    if (StringUtils.equalsIgnoreCase(douBanActor.getName(), newActor.getName())) {
                        //如果查到关联表里已经有该演员
                        newActor.setId(douBanActor.getId());
//                        newActor.setUrl(douBanActor.getUrl());
                    }
                });
            }
            try {
                //保存/更新actor信息
                douBanActorRepo.save(newActor);
                actorId = newActor.getId();

                DouBanShowActor showActor = showActorRepo.findByShowIdAndActorId(showId, actorId);
                if (showActor == null) {
                    //如果没有关联，则添加新关联
                    showActor = new DouBanShowActor();
                    showActor.setActorId(actorId);
                    showActor.setShowId(showId);
                    showActorRepo.save(showActor);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }


        });
    }

    public void addOrUpdateActor(DouBanActor douBanActor) {
        if (StringUtils.isNotBlank(douBanActor.getUrl())) {
            DouBanActor oldActor = douBanActorRepo.findByUrl(douBanActor.getUrl());
            //如果有老的才更新
            if (oldActor != null) {

                BeanUtils.copyProperties(douBanActor, oldActor, "id");

                try {
                    douBanActorRepo.save(oldActor);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

    }

    public Object getActorPlaydShow(Integer actorId) {
        DouBanActor actor = douBanActorRepo.findById(actorId).get();
        List<DouBanShowActor> showActors = showActorRepo.findByActorId(actor.getId()).get();
        List<ShowDto> shows = Lists.newArrayList();
        showActors.forEach(sa -> {
            Integer showId = sa.getShowId();
            DouBanShow show = showRepo.findById(showId);
            if (show != null) {
                ShowDto dto = new ShowDto();
                BeanUtils.copyProperties(show, dto);
                shows.add(dto);
            }
        });
        return shows;
    }

    public Object guessActorName(String name) {
        List<DouBanActor> actors = douBanActorRepo.findByNameLike(name).get();
        return actors.stream().map(a -> new ActorNameDto(a.getId(), a.getName())).collect(Collectors.toList());
    }

    public Object getShows(int page, int size) {
        PageRequest pageRequest = new PageRequest(page, size);
        return showRepo.findAll(pageRequest);
    }

    public Object getActors(int page, int size) {
        PageRequest pageRequest = new PageRequest(page, size);
        return douBanActorRepo.findAll(pageRequest);
    }


    public class ActorNameDto implements Serializable {
        String name;
        Integer id;

        public ActorNameDto(Integer id, String name) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }
    }

    public class ShowDto {
        Integer id;
        String name;
        String url;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
