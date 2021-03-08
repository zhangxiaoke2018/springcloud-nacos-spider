package com.jinguduo.spider.store;

import java.io.IOException;
import java.io.Serializable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.Data;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class CsvFileWriterTests {

    @Autowired
    private CsvFileWriter csvFileWriter;
    
    @Test
    public void testWrite() throws IOException {
        for (int i = 0; i < 400; i++) {
            csvFileWriter.write(new Model(i));
        }
    }
    
    @Data
    public static class Model implements Serializable {
        private static final long serialVersionUID = -1804988607973540568L;
        
        private Integer id = 1;
        private String content = "sd'lk\"jfls'djfds\r\n\rn12314234'*&^%$中文\"";
        //private Map<String, String> map = ImmutableMap.<String, String> of("key", "中文");
        
        public Model() {
            
        }
        
        public Model(Integer i) {
            id = i;
        }
    }
}
