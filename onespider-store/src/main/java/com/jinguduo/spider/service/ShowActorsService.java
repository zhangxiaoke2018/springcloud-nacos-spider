package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.Category;
import com.jinguduo.spider.data.table.OpinionWords;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.ShowActors;
import com.jinguduo.spider.db.repo.OpinionWordsRepo;
import com.jinguduo.spider.db.repo.ShowActorsRepo;
import com.jinguduo.spider.db.repo.ShowRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 03/07/2017 17:49
 */
@Slf4j
@Service
public class ShowActorsService {

    @Autowired
    private ShowActorsRepo showActorsRepo;

    @Autowired
    private ShowRepo showRepo;

    @Autowired
    private OpinionWordsRepo opinionWordsRepo;


    public void insertOrUpdate(ShowActors showActors){

        ShowActors sa = showActorsRepo.findByCodeAndCelebrityCode(showActors.getCode(), showActors.getCelebrityCode());

        if(sa == null){
            showActorsRepo.save(showActors);
            log.info("insert into carl show_actors name : "+showActors.getActorNameCn());
        }else {
            BeanUtils.copyProperties(showActors, sa, "id");
            showActorsRepo.save(sa);
        }


    }

    public List<OpinionWords> findShowActor2OpinionWords(Integer linkedId){

        Show show = showRepo.findByLinkedIdAndPlatformId(linkedId, 12);

        // 只处理 电视剧 && 网剧  新增 综艺
        Show showCategory = showRepo.findByLinkedIdAndCategoryNot(linkedId, "MEDIA_DATA").stream().findFirst().get();

        List<OpinionWords> opinionWords = Lists.newArrayList();

        Boolean isVariety = Boolean.FALSE;
        if (showCategory.getCategory().equals(Category.NETWORK_VARIETY.name()) ||showCategory.getCategory().equals(Category.TV_VARIETY.name()) ){
            isVariety = Boolean.TRUE;
        }

        if(show != null){
            List<ShowActors> showActors = showActorsRepo.findByCode(show.getCode());
            for (ShowActors showActor : showActors) {


                OpinionWords words = new OpinionWords();
                words.setLinkedId(linkedId);
                words.setKeyword(show.getName());
                if (isVariety){
                    words.setCategory("嘉宾");
                }else {
                    words.setCategory("主演");
                }
                words.setSubject(showActor.getActorNameCn().trim());
                words.setRelatedKeyword(showActor.getActorNameCn().trim());
                words.setEpi(0);
                opinionWords.add(words);

                // 如果是综艺，不进行角色处理
                if (isVariety){
                    continue;
                }

                // 角色不为空则再添加一个
                if(StringUtils.isNotBlank(showActor.getRole())){
                    OpinionWords words2 = new OpinionWords();
                    words2.setLinkedId(linkedId);
                    words2.setKeyword(show.getName());
                    words2.setCategory("角色");
                    words2.setSubject(showActor.getRole().trim());
                    words2.setRelatedKeyword(showActor.getRole().trim());
                    words2.setEpi(0);
                    opinionWords.add(words2);

                }
            }
        }

        List<OpinionWords> opinionWordsList = opinionWordsRepo.findByLinkedId(linkedId);

        if(opinionWordsList != null && opinionWordsList.size() > 0){
            // 存在则不进行插入，直接返回
            return opinionWords;
        }


        if(
                !"NETWORK_DRAMA".equals(showCategory.getCategory()) &&
                !"TV_DRAMA".equals(showCategory.getCategory()) &&
                !"NETWORK_VARIETY".equals(showCategory.getCategory()) &&
                !"TV_VARIETY".equals(showCategory.getCategory())
                ) {
            return opinionWords;
        }

        // 插入
        for (OpinionWords opinionWord : opinionWords) {

            try {
                opinionWordsRepo.save(opinionWord);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        return opinionWords;
    }








}
