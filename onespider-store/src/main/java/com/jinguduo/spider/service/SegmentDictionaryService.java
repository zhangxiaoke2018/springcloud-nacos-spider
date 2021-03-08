package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.SegmentDictionary;
import com.jinguduo.spider.db.repo.SegmentDictionaryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 28/04/2017 14:57
 */
@Service
public class SegmentDictionaryService {

    @Autowired
    private SegmentDictionaryRepo segmentDictionaryRepo;

    public List<SegmentDictionary> findAll(){
        return segmentDictionaryRepo.findAll().stream().filter(s -> s.getDisable() == 0).collect(Collectors.toList());
    }

    public Page<SegmentDictionary> find(Integer pageSize, Integer rows, String word){

        PageRequest pageRequest = new PageRequest(pageSize -1, rows);
        Specification specification = ((root, criteriaQuery, criteriaBuilder) -> {
            Predicate workPredicate = criteriaBuilder.like(root.get("word").as(String.class), "%"+word+"%");
            Predicate disablePredicate = criteriaBuilder.equal(root.get("disable").as(Integer.class), 0);
            Predicate nullPredicate = criteriaBuilder.isNull(root.get("disable"));
            return criteriaQuery.where(workPredicate, criteriaBuilder.or(disablePredicate, nullPredicate)).getRestriction();
        });
        Page<SegmentDictionary> segmentDictionaryPage= segmentDictionaryRepo.findAll(specification, pageRequest);
        return segmentDictionaryPage;
    }

    public Optional<SegmentDictionary> save(SegmentDictionary segmentDictionary){
        return Optional.ofNullable(segmentDictionaryRepo.save(segmentDictionary));
    }

    public boolean delete(Integer id){
        SegmentDictionary segmentDictionary = segmentDictionaryRepo.findOne(id);
        segmentDictionary.setDisable(1);
        SegmentDictionary sd = segmentDictionaryRepo.save(segmentDictionary);
        return sd.getDisable() == 1;
    }

}
