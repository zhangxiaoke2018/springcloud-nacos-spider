package com.jinguduo.spider.web;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import com.jinguduo.spider.data.table.Category;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.service.SeedService;
import com.jinguduo.spider.service.ShowService;

import lombok.extern.apachecommons.CommonsLog;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/6/17 下午3:05
 */
@CommonsLog
@RestController
public class ShowController {

    @Autowired
    private ShowService showService;

    @Autowired
    private SeedService seedService;

    @RequestMapping(value = "shows", method = RequestMethod.GET)
    public Object getShowByTitle(@RequestParam String title) {

        List<Show> shows = showService.get(title);
        return shows;
    }

    @RequestMapping(value = "show", method = RequestMethod.POST)
    public Object addShow(@RequestBody Show show) {
        Show res = showService.insertOrUpdate(show);
        return res;
    }

    @RequestMapping(value = "/show/{oldShowId}/{newShowId}", method = RequestMethod.GET)
    public Object addParent(@PathVariable Integer oldShowId, @PathVariable Integer newShowId) {

        showService.addParent(oldShowId, newShowId);
        return null;
    }

    @RequestMapping(value = "/show/list", method = RequestMethod.GET)
    public Object page(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
        page = (page == null || page <= 0 ? 1 : page);
        size = (size == null || size <= 0 ? 10 : size);
        List<Show> shows = showService.getPage(1, page, size);
        return shows;
    }

    @RequestMapping(value = "show/{name}", method = RequestMethod.GET)
    public Object find(@PathVariable String name) {
        return showService.find(name);
    }

    @RequestMapping(value = "show/find", method = RequestMethod.GET)
    public Object findByName(@RequestParam String name) {
        return showService.find(name);
    }

    /***
     * 根据name查询所有的同级show，并且根据Id，返回该show的job
     *
     * 暂时这样写（很戳），下个周会全改版，再删除这些方法
     * @param name
     * @param id
     * @return
     */
    @RequestMapping(value = "show/job/{name}/{id}", method = RequestMethod.GET)
    public List<Show> getShowsJobs(@PathVariable String name, @PathVariable Integer id) {
        return showService.findShowsJobs(name, id);
    }

    @RequestMapping(value = "show/job", method = RequestMethod.GET)
    public List<Show> getShowJobs(@RequestParam String name) {
        return showService.findShowsJobs(name, 0);
    }

    @RequestMapping(value = "show/count", method = RequestMethod.GET)
    public Integer count(@RequestParam Integer depth) {
        return showService.count(depth);
    }

    @RequestMapping(value = "show/like_name", method = RequestMethod.GET)
    public Object likeName(@RequestParam("name") String name) {
        return showService.getByLikeName(name);
    }

    @RequestMapping(value = "show/info", method = RequestMethod.GET)
    public Object showInfo(@RequestParam("name") String name) {
        return showService.getInfo(name);
    }

    @RequestMapping(value = "show/message", method = RequestMethod.GET)
    public Object showMessage(@RequestParam(value = "showName") String name) {
        return showService.getShow(name);
    }

    @RequestMapping(value = "show/downShow", method = RequestMethod.GET)
    public Object downShow(@RequestParam(value = "showId") String showId) {
        Integer id = Integer.parseInt(showId);
        return showService.updateDeleted(id, true);
    }

    @RequestMapping(value = "show/status", method = RequestMethod.GET)
    public Object showStatus(@RequestParam(value = "linked_id") String linked_id) {
        return showService.checkedOnBillboard(Integer.parseInt(linked_id));
    }

    @RequestMapping(value = "show/upBillboard", method = RequestMethod.GET)
    public void upBillboard(@RequestParam(value = "linked_id") String linked_id) {
        showService.upBillboard(Integer.parseInt(linked_id));
    }

    @RequestMapping(value = "show/downBillboard", method = RequestMethod.GET)
    public void downBillboard(@RequestParam(value = "linked_id") String linked_id) {
        showService.downBillboard(Integer.parseInt(linked_id));
    }

    @RequestMapping(value = "show", method = RequestMethod.GET)
    public Object show(@RequestParam Integer showId) {
        return showService.get(showId);
    }

    @RequestMapping(value = "/show/code/{code}", method = RequestMethod.GET)
    public Object findByCode(@PathVariable String code) {
        return showService.findByCode(code);
    }


    @RequestMapping(value = "show/edit/updateStore/", method = RequestMethod.GET)
    public Object showUpdateReleaseDate(@RequestParam String name, @RequestParam String category, @RequestParam(required = false) String releaseDate, @RequestParam Integer linkedId) {

        try {
            showService.updateNameAndCategoryAndReleaseDateByLinkedId(name, category, linkedId, releaseDate);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "FALSE";
        }
        return "SUCCESS";
    }

    @RequestMapping(value = "show/edit/on_billboard/{showName}", method = RequestMethod.GET)
    public Object showUpdateOnBillboard(@PathVariable String showName, @RequestParam String onBillboard) {

        try {
            showService.updateShowOnBillboard(showName, Boolean.valueOf(onBillboard));
        } catch (Exception ex) {
            ex.printStackTrace();
            return "FALSE";
        }
        return "SUCCESS";
    }

    /***
     * 审核分页
     * @param rows
     * @param pagesize
     * @param status
     * @param name
     * @param category
     * @param platformId
     * @return
     */
    @RequestMapping(value = "/show/checkPage", method = RequestMethod.GET)
    public Page<Show> checkPage(@RequestParam(required = true) Integer rows, @RequestParam(required = true) Integer pagesize,
                                String status, String name, String category) {
        //默认查询深度为 1
        Integer depth = 1;
        Integer _status = null;

        //默认查待审核
        if (StringUtils.isNotBlank(status)) {
            _status = Integer.valueOf(status);
        }

        Page<Show> shows = showService.getPage(rows, pagesize, depth, _status, name, category);
        return shows;
    }

    /***
     * 审核show
     * @param id
     * @param name
     * @param status
     * @param releaseDate
     * @param biShowId
     * @param parentId
     * @return
     * @throws ParseException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @RequestMapping(value = "show/checkShow", method = RequestMethod.POST)
    public Show examineShow(@RequestParam(value = "id", required = true) Integer id, @RequestParam(value = "name") String name, Integer status,
                            String releaseDate, Integer biShowId, Integer parentId, @RequestParam String category) throws ParseException, InvocationTargetException, IllegalAccessException {

        final String patten = "yyyy-MM-dd";

        Show show = this.showService.findById(id);
        show.setName(name);
        show.setCategory(category);
        if (null != biShowId && biShowId != 0) {
            show.setLinkedId(biShowId);
        }
        show.setParentId(parentId);
        if (StringUtils.isNotBlank(releaseDate)) {
            show.setReleaseDate(new SimpleDateFormat(patten).parse(releaseDate));
        } else {
            show.setReleaseDate(null);
        }
        show.setCheckedStatus(status);
        //设置为来自dashboard
        show.setSource(1);
        if (status == 2) {
            //忽略处理，删除无用seeds
            if (StringUtils.isNotBlank(show.getCode())) {
                seedService.deleteSeedByCode(show.getCode());
            }
            //同时该show status为deleted
//            show.setDeleted(true);
        } else if(category.equals(Category.KID_ANIME.name()) || category.equals(Category.FOREIGN_KID_ANIME) || category.equals(Category.KID_ANIME_MOVIE.name()) || category.equals(Category.FOREIGN_KID_ANIME_MOVIE.name())){
            show.setOnBillboard(false);
        } else {
            show.setOnBillboard(true);
        }

        return showService.examineShow(show);
    }

    @RequestMapping(value = "/show/list/{code}", method = RequestMethod.GET)
    @ResponseBody
    public List<Show> findAllByCode(@PathVariable String code) {
        return showService.findAllByCode(code);
    }


    @GetMapping("/show/real/all")
    public Object findAllShow() {
        return showService.findRealShows();
    }

    @GetMapping("/shows/all")
    public Object findAllShows() {
        return showService.findAllShows();
    }

    @GetMapping("/show/linked_id/{linkedId}")
    public Object findCodes(@PathVariable Integer linkedId) {
        return showService.findAllByLinkedId(linkedId);
    }

    @GetMapping("/show/havejob")
    public Object haveThisPlatformJob(@RequestParam Integer showId, @RequestParam Integer platformId) {
        if (showService.haveThisPlatformJob(showId, platformId)) {
            return "YES";
        } else {
            return "NO";
        }
    }

}
