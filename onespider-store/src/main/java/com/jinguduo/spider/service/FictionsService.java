package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.Fiction;
import com.jinguduo.spider.data.table.FictionCodeRelation;
import com.jinguduo.spider.db.repo.FictionCodeRelationRepo;
import com.jinguduo.spider.db.repo.FictionsRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FictionsService {

    @Autowired
    private FictionsRepo fictionsRepo;

    @Autowired
    private FictionCodeRelationRepo fictionCodeRelationRepo;

    public Fiction insert(Fiction fiction) {
        if (fiction.getName() == null || fiction.getAuthor() == null
                || fiction.getCode() == null || fiction.getPlatformId() == null)
            return null;
        Integer platformId = fiction.getPlatformId();
        String code = fiction.getCode();

        fiction.setAuthor(trimSpace(fiction.getAuthor()));
        fiction.setName(trimSpace(fiction.getName()));

        Fiction oldFiction = fictionsRepo.findByNameAndAuthor(fiction.getName(), fiction.getAuthor());

        if (oldFiction != null) {
            fiction = update(oldFiction, fiction);
        }

        if (fiction != null) {
            Fiction saved = fictionsRepo.save(fiction);
            updateFictionCodeRelation(saved.getId(), platformId, code);
        }

        return fiction;
    }

    private String trimSpace(String src) {
        if (src == null || src.length() == 0)
            return "";

        return src.trim().replaceAll("\\s+", " ");
    }

    private void updateFictionCodeRelation(Integer fictionId, Integer platformId, String code) {
        FictionCodeRelation exist = fictionCodeRelationRepo.findByCodeAndPlatformId(code, platformId);
        if (exist != null && exist.getFictionId().equals(fictionId))
            return;

        FictionCodeRelation relation = new FictionCodeRelation();
        if (exist != null) {
            relation.setId(exist.getId());
        }
        relation.setCode(code);
        relation.setPlatformId(platformId);
        relation.setFictionId(fictionId);
        fictionCodeRelationRepo.save(relation);
    }

    private Fiction update(Fiction oldFiction, Fiction newFiction) {
        newFiction.setId(oldFiction.getId());

        if (newFiction.getIsFinish() == null) {
            newFiction.setIsFinish(oldFiction.getIsFinish());
        }

        if (newFiction.getChannel() == null) {
            newFiction.setChannel(oldFiction.getChannel());
        } else if (newFiction.getChannel() != oldFiction.getChannel()
                && (newFiction.getPlatformId() == 49
                || newFiction.getPlatformId() == 50
                || newFiction.getPlatformId() == 42)
                || newFiction.getPlatformId() == 57) {
            //如果从不可靠平台（掌阅、QQ阅读）爬来的频道信息和原有不一致，保持原有
            newFiction.setChannel(oldFiction.getChannel());
        }


        if (newFiction.getTotalLength() == null) {
            newFiction.setTotalLength(oldFiction.getTotalLength());
        }

        if (StringUtils.isEmpty(newFiction.getCover())) {
            newFiction.setCover(oldFiction.getCover());
        }

        if (StringUtils.isEmpty(newFiction.getIntro())) {
            newFiction.setIntro(oldFiction.getIntro());
        }

        return newFiction;
    }

}
