package com.jinguduo.spider.db.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jinguduo.spider.data.table.Show;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 16/6/17 下午3:06
 */
public interface ShowRepo extends JpaRepository<Show,Integer> {

    List<Show> findByDepth(Integer depth);

    @Query("from Show where depth = :depth")
    List<Show> findByDepth(@Param("depth") Integer depth , Pageable pageable);

    List<Show> findByNameLike(String name);

	Show findFirstByCodeAndDepthOrderById(String code, Integer depth);
	Show findFirstByCodeAndPlatformIdAndDepthOrderById(String code,Integer platformId,Integer depth);
	Show findFirstByCodeAndDepthAndDeletedAndCheckedStatusOrderById(String code, Integer depth, Boolean deleted,Integer checkStatus);
    Show findFirstByUrlAndDepthOrderById(String url, Integer depth);

    List<Show> findByNameAndDepth(String name,Integer depth);
    
    List<Show> findByNameAndDepthAndCategoryNot(String name,Integer depth,String category);

    List<Show> findByParentId(Integer parentId);

    List<Show> findByLinkedId(Integer linkedId);

    List<Show> findByLinkedIdAndCategoryNot(Integer linkedId,String category);

    List<Show> findByLinkedIdAndDepthAndCategoryNotIn(Integer linkedId, Integer depth, List categorys);

    List<Show> findByName(String name);

    Show findByCode(String code);
    
    List<Show> findByCodeOrderById(String code);
    
    Show findByCodeAndDepthAndPlatformId(String code,Integer depth,Integer platformId);
    
    List<Show> findByPlatformId(Integer platformId);

    Show findFirstByNameAndDepthAndPlatformIdOrderById(String name, int i, Integer platformId);
    
    Show findFirstByNameAndDepthAndPlatformIdAndDeletedAndCheckedStatusOrderById(String name, int i, Integer platformId, Boolean deleted,Integer checkedStatus);

    Show findFirstByLinkedIdAndDepthAndIdNotOrderByParentIdDescIdAsc(Integer linkedId, Integer depth, Integer id);

    Show findFirstByLinkedIdAndDepthAndIdNotAndCategoryNotOrderByParentIdDescIdAsc(Integer linkedId, Integer depth, Integer id,String category);

    Page<Show> findAll(Specification<Show> spec, Pageable pageable);

    List<Show> findByLinkedIdAndDepth(Integer linkedId,Integer depth);

    List<Show> findByLinkedIdAndPlatformIdAndDepthAndDeleted(Integer linkedId,Integer platformId,Integer depth,boolean deleted);

    List<Show> findById(Integer id);

    List<Show> findByCategoryNotAndDepthNot(String category, Integer depth);

    List<Show> findByDeletedAndDepthNot(Boolean deleted, Integer depth);

    List<Show> findByLinkedIdAndDepthAndPlatformId(Integer linkedId, Integer depth, Integer platformId);

    List<Show> findByCategoryAndDepthAndPlatformId(String category, Integer depth, Integer platformId);

    Show findByLinkedIdAndPlatformId(Integer linkedId, Integer platformId);

    Show findFirstByCodeAndPlatformIdAndDepthAndDeletedAndCheckedStatusOrderById(String parentCode, Integer platformId, int i, boolean b, int i1);

    Show findFirstByParentIdAndAndDepth(Integer parentId,Integer Depth);
}
