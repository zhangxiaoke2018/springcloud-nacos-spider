package com.jinguduo.spider.web;

import com.google.common.collect.ImmutableMap;
import com.jinguduo.spider.data.table.SegmentDictionary;
import com.jinguduo.spider.service.SegmentDictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 28/04/2017 14:58
 */
@RestController
@RequestMapping("/segment_dictionary")
public class SegmentDictionaryController {

    @Autowired
    private SegmentDictionaryService segmentDictionaryService;

    @GetMapping("/all")
    public Object findAll(){
        return segmentDictionaryService.findAll();
    }

    @RequestMapping("/list")
    public Object list(@RequestParam Integer page,
                       @RequestParam Integer rows,
                       @RequestParam String word){
        Page<SegmentDictionary> segmentDictionaryPage = segmentDictionaryService.find(page, rows, word);
        return ImmutableMap.of("data", segmentDictionaryPage.getContent(), "total", segmentDictionaryPage.getTotalPages());
    }

    @RequestMapping("/update")
    public Map update(@RequestParam Integer id,
                      @RequestParam String word,
                      @RequestParam String nature,
                      @RequestParam Integer frequency,
                      @RequestParam Integer hasModified){
        Map map = new HashMap();
        SegmentDictionary segmentDictionary = new SegmentDictionary(id, word, nature, frequency, hasModified, 0);
        Optional<SegmentDictionary> segmentDictionaryOptional = segmentDictionaryService.save(segmentDictionary);
        if (segmentDictionaryOptional.isPresent()){
            map.put("code", "0");
        }else {
            map.put("code", "1");
            map.put("message", "失败");
        }
        return map;
    }


    @RequestMapping("/delete")
    public Map delete(@RequestParam Integer id){
        Map map = new HashMap();
        if (segmentDictionaryService.delete(id)){
            map.put("code", 0);
        }else {
            map.put("code", 1);
            map.put("message", "失败");
        }
        return map;
    }
}
