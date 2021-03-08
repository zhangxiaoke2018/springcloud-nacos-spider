package com.jinguduo.spider.spider.sohu;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;



//@RunWith(SpringRunner.class)
//@ActiveProfiles("test")
//@SpringBootTest
//@Slf4j
public class SohuMyMediaPlayCountSpiderTests {
    @Autowired
    private SohuMyMediaSpider sohuMyMediaSpider;

    private static final String TOTAL_PALYCOUNT_URL = "http://vstat.v.blog.sohu.com/dostat.do?method=getVideoPlayCount&v=89479706%7C89310732%7C89007324%7C89153568%7C88874810%7C88735938%7C88593167%7C88451918%7C88316204%7C88173089%7C88030094%7C87883303%7C87739163%7C87600371%7C87471648%7C87359979%7C&n=bvid";

    @Value("classpath:html/SohuMyMediaPlayCount.html")
    private Resource resource;

    private DelayRequest delayRequest;

    @Before
    public void setup()  {
        Job job = new Job();
        job.setPlatformId(1);
        job.setShowId(1);
        job.setUrl(TOTAL_PALYCOUNT_URL);
        job.setFrequency(100);
        job.setMethod("GET");

        // request
        delayRequest = new DelayRequest(job);
    }

//不需要的测试
/*    @Test
    public void testProcessOk() {
        // page
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(IoResourceHelper.readResourceContent("/html/SohuMyMediaPlayCount.html"));

        sohuMyMediaSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems);
        List<ToutiaoNewLogs> allToutiao =  resultItems.get("ShowLogs");
        for (ToutiaoNewLogs tt : allToutiao) {
            System.out.println(tt.getTitle());
        }
        Assert.notNull(allToutiao);
    }*/
}
