package com.jinguduo.spider.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.Alias;
import com.jinguduo.spider.data.table.AliasPolymerization;
import com.jinguduo.spider.service.AliasService;

/**
 * Created by lc on 2017/6/7.
 */
@RestController
@RequestMapping("alias")
public class AliasController {

    @Autowired
    private AliasService aliasService;

    @RequestMapping(value = "all", method = RequestMethod.GET)
    public List<AliasPolymerization> getAll() {
        return aliasService.getAll();
    }

    @RequestMapping(value = "list/classify/{classify}", method = RequestMethod.GET)
    public List<AliasPolymerization> getAllByClassify(@PathVariable("classify") String classify) {
        return aliasService.getAllByClassify(classify);
    }

    @RequestMapping(value = "list/classify/{classify}/category/{category}/type/{type}", method = RequestMethod.GET)
    public List<AliasPolymerization> getAllByClassifyAndCategoryAndType(@PathVariable("classify") String classify, @PathVariable("category") String category, @PathVariable("type") Byte type) {
        return aliasService.getAllByClassifyAndCategoryAndType(classify, category, type);
    }

    @RequestMapping(value = "list/relevanceId/{relevanceId}/type/{type}", method = RequestMethod.GET)
    public List<AliasPolymerization> getByRelevanceIdAndType(@PathVariable("relevanceId") Integer relevanceId,@PathVariable("type") Byte type) {
        return aliasService.getByRelevanceIdAndType(relevanceId,type);
    }

    @RequestMapping(value = "update",method = RequestMethod.POST)
    public Alias updateAlias(@RequestBody Alias alias){
        return aliasService.updateAlias(alias);
    }
    
    @RequestMapping(value = "/finduk",method = RequestMethod.GET)
    public Alias findAliasByUk(@RequestParam Integer linkedId,@RequestParam Byte type,@RequestParam String classify){
        return aliasService.getAliasByUk(linkedId,type,classify);
    }
    
    @GetMapping("/generate/alias")
    public void generateAlias(@RequestParam Integer showId,@RequestParam String showName,@RequestParam String category){
        aliasService.generateAlias(showId,showName,category);
    }
}
