package com.jinguduo.spider.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jinguduo.spider.common.constant.StatusEnum;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.*;
import com.jinguduo.spider.db.repo.PlatformRepo;
import com.jinguduo.spider.db.repo.SeedRepo;
import com.jinguduo.spider.db.repo.ShowLogRepo;
import com.jinguduo.spider.db.repo.ShowRepo;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.jinguduo.spider.common.util.DateHelper.lastDayWholePointDate;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 16/6/17 下午3:05
 */
@Service
@CommonsLog
@SuppressWarnings("all")
public class ShowService {

    @Resource
    private ShowRepo showRepo;

    @Resource
    private ShowLogRepo showLogRepo;

    @Resource
    private PlatformRepo platformRepo;

    @Autowired
    private SeedRepo seedRepo;

    public Show get(Integer showId) {
        return showRepo.findOne(showId);
    }

    public List<Show> get(String title) {

        List<Show> shows = showRepo.findByDepth(1);

        List<Show> collect = shows.stream()
                .filter(s -> title.contains(s.getName().substring(0, s.getName().length() >= 2 ? 2 : s.getName().length())))
                .map(s -> {
                    s.setName(s.getName() + " " + platformRepo.findOne(s.getPlatformId()).getName());
                    return s;
                })
                .collect(Collectors.toList());

        return collect;
    }

    public List<Show> getPage(Integer depth, Integer page, Integer size) {
        List<Show> shows = showRepo.findByDepth(depth, new PageRequest(page - 1, size));
        return shows;
    }

    public Integer count(Integer depth) {
        return showRepo.findByDepth(depth).size();
    }

    public List<Show> getByLikeName(String name) {
        return showRepo.findByNameLike(name);
    }

    public void addParent(Integer oldShowId, Integer newShowId) {

        Assert.isTrue(oldShowId != 0);
        Assert.isTrue(newShowId != 0);

        Show oldShow = showRepo.findOne(oldShowId);
        Show newShow = showRepo.findOne(newShowId);

        newShow.setCategory(oldShow.getCategory());
        newShow.setReleaseDate(oldShow.getReleaseDate());
        if (oldShow.getParentId() != 0 && oldShow.getParentId() != null) {//存在父级
            newShow.setParentId(oldShow.getParentId());
            showRepo.save(newShow);
        } else {//不存在父级,创建父级,并更新两个show的parentId
            Show parentShow = new Show();
            parentShow.setName(oldShow.getName());
            parentShow.setDepth(0);
            parentShow.setPlatformId(0);
            Show saveRe = showRepo.save(parentShow);

            oldShow.setParentId(saveRe.getId());
            newShow.setParentId(saveRe.getId());
            showRepo.save(oldShow);
            showRepo.save(newShow);
        }


    }

    /***
     * 保存show逻辑
     * <p>
     *   https://tower.im/projects/945d74142676474e8e42d5f8dfddc089/docs/6334eb5bea184f108e01c89d181033ff/
     *
     * @param show
     * @return
     */
    public Show insertOrUpdate(Show show) {

        if (show == null || StringUtils.isBlank(show.getName()) || StringUtils.isBlank(show.getCode())) {
            return null;
        }


        show.setParentId(parentId(show));

        Show r = null;
        String code = show.getCode();

        //根据show生成seed
        if (StringUtils.isNotBlank(show.getUrl()) && show.getDepth() == 1) {

            Seed s = seedRepo.findByUrl(show.getUrl());
            if (s == null) {
                s = seedRepo.findByCode(code);
            }
            if (s == null) {
                Seed seed = new Seed();
                seed.setUrl(show.getUrl());
                seed.setCode(show.getCode());
                seed.setPlatformId(show.getPlatformId());
                if (show.isCheckIgnored()) {
                    //若忽略处理时还未有seed 则存一个标记删除的seed 之后再在爬虫自动发现时不再更新seed
                    seed.setStatus(StatusEnum.STATUS_DEL.getValue());
                } else {
                    seed.setStatus(StatusEnum.STATUS_OK.getValue());
                }
                seedRepo.save(seed);
            } else if (s.getStatus() == StatusEnum.STATUS_DEL.getValue()) {
                if (!show.isFromAutoFind() && !show.isCheckIgnored()) {
                    //忽略过的剧的seed 不再更新为正常状态
                    s.setUrl(show.getUrl());
                    s.setCode(show.getCode());
                    s.setPlatformId(show.getPlatformId());
                    s.setStatus(StatusEnum.STATUS_OK.getValue());
                    seedRepo.save(s);
                }
            }
        }

        //MEDIA_DATA 单独处理
        if (Category.MEDIA_DATA.name().equals(show.getCategory())) {
            Show s = showRepo.findByCode(code);
            if (s == null) {
                show.setParentCode("");
                return showRepo.save(show);
            } else if (s != null && s.getLinkedId() != show.getLinkedId() && s.getPlatformId() == 12 && show.getPlatformId() == 12) {//豆瓣类任务特殊处理
                //这是多个剧公用一个豆瓣的情况
                show.setParentCode("");
                return showRepo.save(show);
            } else if (s != null && s.getLinkedId() != show.getLinkedId() && s.getPlatformId() != show.getPlatformId()) {
                //这是无相关的2个剧因为code一样的情况
                show.setParentCode("");
                return showRepo.save(show);
            } else {
                return s;
            }
        }

        if (show.getDepth() == 1) {
            //深度为1或深度为0 parentCode 暂设为空
            show.setParentCode("");
            if (show.getId() != null) { // must only from dashboard !!!
                updateSiblingAndParent(show); //更新show的相关记录（父级及兄弟节点）
                // 该平台有对应show，可能是删除状态的show,这是将删除状态的show更新
                if (show.getLinkedId() != null && show.getPlatformId() != null) {
                    List<Show> ldp = showRepo.findByLinkedIdAndDepthAndPlatformId(show.getLinkedId(), show.getDepth(), show.getPlatformId()).stream()
                            .filter(f -> f.getId() != show.getId()).collect(Collectors.toList());
                    //该剧该平台的show
                    if (ldp != null && ldp.size() > 0) {
                        Optional<Show> notDel = ldp.stream().filter(f -> (!f.getDeleted())).findAny();
                        if (notDel.isPresent()) {
                            //存在为删除的show则把重复的show及seed删除
                            show.setCheckedStatus(2);
                            show.setOnBillboard(false);
                            show.setDeleted(true);
                            r = this.showRepo.save(show);
                            Seed seed = seedRepo.findByCode(show.getCode());
                            if (seed != null && seed.getStatus() != StatusEnum.STATUS_DEL.getValue()) {
                                seed.setStatus(StatusEnum.STATUS_DEL.getValue());
                                seedRepo.save(seed);
                            }
                        } else {
                            //不存在未删除的show则更新已删除的show
                            Show existShow = ldp.get(0);
                            DbEntityHelper.copy(show, existShow, new String[]{"id"});
                            r = this.showRepo.save(existShow);
                        }
                    } else {
                        r = this.showRepo.save(show);
                    }
                } else {
                    r = this.showRepo.save(show);
                }
            } else { // from spider or dashboard

                Show old = this.showRepo.findFirstByUrlAndDepthOrderById(show.getUrl(), show.getDepth());
                if (old == null) {
                    old = this.showRepo.findFirstByCodeAndDepthOrderById(code, show.getDepth());
                }
                if (old != null) {//存在&&检查未通过 s or d
                    if (show.isFromDashboard() || old.isCheckInit() || old.getDeleted()) {
                        // 修改entity, 如果来自dashboard或者 show还未审核
                        DbEntityHelper.copy(show, old, new String[]{"id", "code"});
                        updateSiblingAndParent(old);
                        r = this.showRepo.save(old);
                    }
                } else {
                    updateSiblingAndParent(show); // 更新show的相关记录（父级及兄弟节点）
                    // create
                    // 该平台有对应show，可能是删除状态的show,这是将删除状态的show更新
                    if (show.getLinkedId() != null && show.getPlatformId() != null) {
                        List<Show> ldp = showRepo.findByLinkedIdAndDepthAndPlatformId(show.getLinkedId(), show.getDepth(), show.getPlatformId());
                        //该剧该平台的show
                        if (ldp != null && ldp.size() > 0) {
                            Optional<Show> notDel = ldp.stream().filter(f -> (!f.getDeleted())).findAny();
                            //不存在未删除的show则更新已删除的show    存在为删除的show则更新该show
                            Show existShow = notDel.isPresent() ? notDel.get() : ldp.get(0);
                            DbEntityHelper.copy(show, existShow, new String[]{"id"});
                            r = this.showRepo.save(existShow);
                        } else {
                            r = this.showRepo.save(show);
                        }
                    } else {
                        r = this.showRepo.save(show);
                    }
                }
                // skip saving when show.id == null && from Spider
            }
        } else if (show.getDepth() == 2) {

            if (show.getName().contains("预告") || show.getName().contains("宣传片") || show.getName().contains("片花") || show.getName().contains("剧透")) {
                return null;
            }
            Show old = null;
            if (null == show.getPlatformId()) {
                old = this.showRepo.findFirstByCodeAndDepthOrderById(code, show.getDepth());
            } else {
                old = this.showRepo.findFirstByCodeAndPlatformIdAndDepthOrderById(code, show.getPlatformId(), show.getDepth());
            }


            if (null != old) {
                show.setId(old.getId());
                show.setParentId(old.getParentId());
                //分集修正
                if ((null == show.getEpisode() || 0 == show.getEpisode())) {
                        show.setEpisode(old.getEpisode());
                }


            }

            Show parentShow = null;
            if (show.getParentId() != null && show.getParentId() != 0) {
                parentShow = this.showRepo.findOne(show.getParentId());
            } else {
                //土豆等平台网大存在深度２的，自动发现的spider不能给定parentId，特殊处理
                parentShow = this.showRepo.findFirstByNameAndDepthAndPlatformIdAndDeletedAndCheckedStatusOrderById(show.getName(), 1, show.getPlatformId(), false, 1);
            }
            if (parentShow == null && StringUtils.isNotBlank(show.getParentCode())) {
                if (null == show.getPlatformId()) {
                    parentShow = this.showRepo.findFirstByCodeAndDepthAndDeletedAndCheckedStatusOrderById(show.getParentCode(), 1, false, 1);
                } else {
                    parentShow = this.showRepo.findFirstByCodeAndPlatformIdAndDepthAndDeletedAndCheckedStatusOrderById(show.getParentCode(), show.getPlatformId(), 1, false, 1);
                }

            }

            if (parentShow != null) {
                show.setLinkedId(parentShow.getLinkedId());
                show.setCategory(parentShow.getCategory());
                if (show.getReleaseDate() == null) {
                    show.setReleaseDate(parentShow.getReleaseDate());
                }
                show.setParentId(parentShow.getId());
                show.setPlatformId(parentShow.getPlatformId());
                show.setParentCode(parentShow.getCode());
            }
            try {
                if (show.getPlatformId() == null) {
                    return null;
                }
                r = this.showRepo.save(show);
            } catch (Exception ex) {
                log.error(show);
                log.error(ex.getMessage());
            }
        }
        return r;
    }

    /**
     * 更新show的相关记录（父级及兄弟节点），<b>只能用在insetOrUpdate(Show)方法内</b>
     *
     * @param show
     */
    private void updateSiblingAndParent(Show show) {
        // from dashboard
        if (show.getLinkedId() != null && show.getLinkedId() != 0) { // 防御检查
            final int id = (show.getId() == null) ? 0 : show.getId();
            Show sibling = this.showRepo.findFirstByLinkedIdAndDepthAndIdNotAndCategoryNotOrderByParentIdDescIdAsc(show.getLinkedId(), show.getDepth(), id, "MEDIA_DATA");
            if (sibling != null) {
                if (sibling.getParentId() == 0 || sibling.getParentId() == null) {
                    //create parent's show
                    Show parent = new Show();
                    DbEntityHelper.copy(sibling, parent, new String[]{"id", "depth", "platformId"});
                    parent.setDepth(0);
                    parent.setPlatformId(0);
                    parent.setParentCode("");
                    parent.setUrl("");
                    parent.setCode(DigestUtils.md5Hex(show.getName() + show.getLinkedId()));
                    parent = showRepo.save(parent);
                    // update sibling
                    sibling.setParentId(parent.getId());
                    showRepo.save(sibling);
                }
                show.setOnBillboard(sibling.getOnBillboard());
                show.setParentId(sibling.getParentId());
                show.setParentCode("");
            }
        }
    }

    /**
     * parent_id为null时将其设为0
     *
     * @param show
     * @return
     */
    private int parentId(Show show) {
        return show.getParentId() == null ? 0 : show.getParentId();
    }

    /**
     * 根据show的名称查询得到所有show
     *
     * @param name
     * @return
     */
    public Object find(String name) {

        List<Show> shows = showRepo.findByNameAndDepth(name, 1);
        List<JSONObject> collect = shows.stream().map(s -> JSONObject.parseObject(JSON.toJSONString(s))).collect(Collectors.toList());

        collect.forEach(c -> c.put("platform_name", platformRepo.findOne(c.getInteger("platformId")).getName()));

        return collect;
    }

    public List<Map> getInfo(String name) {

        List<Map> returnMaps = Lists.newArrayList();

        List<Show> shows = Lists.newArrayList();
        List<Show> parentShow = showRepo.findByNameAndDepth(name, 0);
        if (parentShow == null || parentShow.size() == 0) {//单平台--不排除没有归为一类多平台的剧
            shows = showRepo.findByNameAndDepth(name, 1);
        } else {//多平台
            shows = showRepo.findByParentId(parentShow.get(0).getId());
        }
        if (shows.size() == 0) return null;
        for (Show show : shows) {
            Platform platform = platformRepo.findOne(show.getPlatformId());

            ShowLog tShowLog = showLogRepo.findTop1ByCodeAndCrawledAtBetweenOrderByCrawledAtDesc(
                    show.getCode(),
                    new Timestamp(lastDayWholePointDate(new Date(), 1).getTime()),
                    new Timestamp(lastDayWholePointDate(new Date(), 0).getTime()));

            ShowLog yShowLog = showLogRepo.findTop1ByCodeAndCrawledAtBetweenOrderByCrawledAtDesc(
                    show.getCode(),
                    new Timestamp(lastDayWholePointDate(new Date(), 2).getTime()),
                    new Timestamp(lastDayWholePointDate(new Date(), 1).getTime()));

            Map map = Maps.newHashMap();
            map.put("name", show.getName());
            map.put("code", show.getCode());
            if (show.getReleaseDate() != null) {
                map.put("release_date", DateFormatUtils.format(show.getReleaseDate(), "yyyy-MM-dd"));
            } else {
                map.put("release_date", "");
            }
            map.put("category", Category.getDesc(Category.valueOf(show.getCategory())));
            map.put("platform_name", platform.getName());
            map.put("t_show_log", tShowLog == null ? 0 : tShowLog.getPlayCount() + " | " + (tShowLog == null ? "..." : DateFormatUtils.format(tShowLog.getCrawledAt(), "yyyy-MM-dd HH:mm:ss")));
            map.put("y_show_log", (yShowLog == null ? 0 : yShowLog.getPlayCount()) + " | " + (yShowLog == null ? "..." : DateFormatUtils.format(yShowLog.getCrawledAt(), "yyyy-MM-dd HH:mm:ss")));
            map.put("t_play_count", tShowLog == null ? 0 : tShowLog.getPlayCount());
            map.put("y_play_count", yShowLog == null ? 0 : yShowLog.getPlayCount());
            map.put("daily", tShowLog == null ? 0 : tShowLog.getPlayCount() - (yShowLog == null ? 0 : yShowLog.getPlayCount()));

            returnMaps.add(map);
        }
        return returnMaps;
    }

    public void updateNameAndCategoryAndReleaseDateByLinkedId(String name, String category, Integer linkedId, String releaseDate) throws ParseException {
        List<Show> shows = showRepo.findByLinkedId(linkedId);
        for (Show show : shows) {
            if (!show.getCategory().equals(Category.MEDIA_DATA.name())) {
                if (StringUtils.isNotBlank(releaseDate)) {
                    show.setReleaseDate(DateUtils.parseDate(releaseDate, "yyyy-MM-dd"));
                }
                show.setCategory(category);
                showRepo.save(show);
            }
        }
    }

    public void updateShowOnBillboard(String showName, Boolean bol) {
        List<Show> shows = showRepo.findByName(showName);
        for (Show show : shows) {
            show.setOnBillboard(bol);
            showRepo.save(show);
        }


    }

    /***
     * 分页查询
     * @param rows
     * @param pagesize
     * @param depth
     * @param status
     * @param name
     * @param category
     * @param platformId
     * @return
     */
    public Page<Show> getPage(Integer rows, Integer pagesize, Integer depth, Integer status, String name, String category) {

        Page<Show> shows = null;

        //默认Id大于多少
        final Integer late = 200000;

        //分页计算 JPA不用计算分页，多此一举
        /*int start = 1;
        int pageSize = 15;
        if(rows != 0 && pagesize != 0){
            start = rows*(pagesize-1);
            pageSize = rows;
        }*/
        PageRequest page = new PageRequest(pagesize - 1, rows, new Sort((status != null && status == 0) ? Sort.Direction.ASC : Sort.Direction.DESC, new String[]{"id"}));

        Specification<Show> spec = new Specification<Show>() {
            public Predicate toPredicate(Root<Show> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                //条件查询
                List<Predicate> ps = Lists.newArrayList();

                ps.add(cb.equal(root.get("depth").as(Integer.class), depth));
                ps.add(cb.equal(root.get("category").as(String.class), category));
                ps.add(cb.ge(root.get("id").as(Integer.class), late));

                if (null != status) {
                    ps.add(cb.equal(root.get("checkedStatus").as(Integer.class), status));
                }

                if (StringUtils.isNotBlank(name)) {
                    ps.add(cb.like(root.get("name").as(String.class), "%" + name + "%"));
                }

                Predicate[] pp = ps.toArray(new Predicate[ps.size()]);

                return query.where(pp).getRestriction();
            }
        };

        shows = showRepo.findAll(spec, page);

        return shows;
    }

    public Show findById(Integer id) {
        return this.showRepo.findOne(id);
    }

    public List<Show> findByLinkedId(Integer linkedId) {
        return showRepo.findByLinkedIdAndDepth(linkedId, 1);
    }

    public List<Show> findAllByLinkedId(Integer linkedId) {
        return showRepo.findByLinkedId(linkedId);
    }


    /***
     * 检查show
     * @param show
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Show examineShow(Show show) {
        return this.insertOrUpdate(show);
    }

    public Object getShow(String name) {
        List<Show> shows = showRepo.findByName(name);
        List sj = Lists.newArrayList();
        for (int i = 0; i < shows.size(); i++) {
            Show show = shows.get(i);
            Map map = Maps.newHashMap();
            if (show.getDepth() != 1)
                continue;
            else {
                String code = shows.get(i).getCode();
                map.put("id", show.getId());
                map.put("name", name);
                map.put("code", code);
                map.put("parent_id", show.getParentId());
                map.put("depth", show.getDepth());
                map.put("deleted", show.getDeleted());
                map.put("platform_id", show.getPlatformId());
                map.put("category", show.getCategory());
                map.put("linked_id", show.getLinkedId());
                map.put("episode", show.getEpisode());
                map.put("on_billboard", show.getOnBillboard());
                map.put("checked_status", show.getCheckedStatus());
                sj.add(map);
            }
        }
        return sj;
    }

    public Object updateDeleted(Integer id, boolean deleted) {
        Show show = showRepo.findOne(id);
        show.setDeleted(deleted);
        return showRepo.save(show);
    }

    public Show findByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        return showRepo.findByCode(code);
    }

    //该条件下 show 唯一
    public Show findByCodeAndDepthAndPlatform(String code, Integer depth, Integer platformId) {
        if (StringUtils.isBlank(code) || depth == null || platformId == null) {
            return null;
        }
        return showRepo.findByCodeAndDepthAndPlatformId(code, depth, platformId);
    }

    public List<Show> findAllByCode(String code) {
        List<Show> showList = new ArrayList<>();
        Show show = showRepo.findByCode(code);
        if (show == null) {
            return Lists.newArrayList();
        }
        if (show.getDepth() == 2) {
            Show pShow = showRepo.findOne(show.getParentId());
            if (pShow.getParentId() != 0) {
                List<Show> pShowList = showRepo.findByParentId(pShow.getParentId());
                for (Show pShow2 : pShowList) {
                    List<Show> sShowList = showRepo.findByParentId(pShow2.getId());
                    showList.addAll(sShowList);
                }
            } else {
                List<Show> sShowList = showRepo.findByParentId(show.getParentId());
                showList.addAll(sShowList);
            }

        } else if (show.getDepth() == 1 && show.getParentId() != 0) {
            List<Show> sShowList = showRepo.findByParentId(show.getParentId());
            showList.addAll(sShowList);
        } else {
//            showList.add(show);
        }
        showList.sort((show1, show2) -> {
            if (show1.getEpisode() == null || show2.getEpisode() == null) {
                return -1;
            }
            return show1.getEpisode().compareTo(show2.getEpisode());
        });
        showList.add(0, show);
        return showList;
    }

    public List<Show> findByPlatformId(Integer platformId) {

        return showRepo.findByPlatformId(platformId);
    }


    public boolean checkedOnBillboard(Integer linked_id) {
        List<Show> shows = showRepo.findByLinkedIdAndDepthAndCategoryNotIn(linked_id, 1, Lists.newArrayList(Category.MEDIA_DATA.name()));
        if (CollectionUtils.isEmpty(shows)) {
            return false;
        }
        for (Show show : shows) {
            if (show.getOnBillboard().equals(Boolean.FALSE))
                return false;
        }
        return true;
    }

    public void upBillboard(Integer linked_id) {
        List<Show> shows = showRepo.findByLinkedIdAndDepthAndCategoryNotIn(linked_id, 1, Lists.newArrayList(Category.MEDIA_DATA.name()));
        for (Show show : shows) {
            show.setOnBillboard(true);
            showRepo.save(show);
        }
    }

    public void downBillboard(Integer linked_id) {
        List<Show> shows = showRepo.findByLinkedIdAndDepthAndCategoryNotIn(linked_id, 1, Lists.newArrayList(Category.MEDIA_DATA.name()));
        for (Show show : shows) {
            show.setOnBillboard(false);
            showRepo.save(show);
        }
    }


    public List<Show> findRealShows() {
        return showRepo.findByCategoryNotAndDepthNot("MEDIA_DATA", 0);
    }

    /***
     * 根据name查询所有的同级show，并且根据Id，返回该show的job
     * @param name
     * @param id
     * @return
     */
    public List<Show> findShowsJobs(String name, Integer id) {

        List<Show> shows = showRepo.findByNameAndDepthAndCategoryNot(name, 1, "MEDIA_DATA");

        shows.stream().forEach(s -> getSJ(s, id));

        return shows;
    }

    private void getSJ(Show s, Integer id) {
        s.setPlatformCn(platformRepo.findOne(s.getPlatformId()).getName());
        try {
            Seed seed = this.seedRepo.findByCodeAndStatus(s.getCode(), StatusEnum.STATUS_OK.getValue());
            if (null != seed) {
                s.setUrl(seed.getUrl());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public boolean haveThisPlatformJob(Integer showId, Integer platformId) {
        List<Show> list = showRepo.findByLinkedIdAndPlatformIdAndDepthAndDeleted(showId, platformId, 1, false);
        if (list != null && list.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public Object findAllShows() {
        return showRepo.findByDeletedAndDepthNot(false, 0);
    }

}
