package com.jinguduo.spider.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class RevisionController {
    
    private String pathname = "/revision";

    @RequestMapping(path = "/revision", produces = "text/plain")
    public String revision() throws IOException {
        String revision = "No Exists";
        
        ClassPathResource resource = new ClassPathResource(pathname);
        byte[] bdata = FileCopyUtils.copyToByteArray(resource.getInputStream());
        revision = new String(bdata, StandardCharsets.UTF_8);
        
        return revision;
    }
}
