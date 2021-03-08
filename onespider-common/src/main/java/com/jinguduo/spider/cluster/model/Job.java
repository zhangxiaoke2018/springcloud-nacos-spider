package com.jinguduo.spider.cluster.model;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import javax.persistence.Transient;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.constant.JobKind;
import com.jinguduo.spider.common.constant.JobSchedulerCommand;
import com.jinguduo.spider.webmagic.model.HttpRequestBody;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = {"id", "kind", "command", "frequency", "httpRequestBody"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Job implements Serializable {

	private static final long serialVersionUID = 7332983459126121979L;

	private String id;
	private Integer showId;
	private Integer platformId;
	
	private String url;
	private String method = "GET";
	private String code;
	private String parentCode;
	// 多长时间重复一次抓取，秒。无特殊情况不需要设置，在SpiderSetting中统一配置
	private Integer frequency = FrequencyConstant.BLANK;
	private JobKind kind = JobKind.Once;
	private JobSchedulerCommand command;
	
	private HttpRequestBody httpRequestBody;
	
	@Transient
	@JsonIgnore
	private transient Long crawledAt = 0L;

	@Transient
	@JsonIgnore
	private transient String host = null;  // 冗余

	public Job() {
		super();
	}

	public Job(String url) {
		this(url, "GET");
	}

	public Job(String url, String method) {
		this();
		this.setUrl(url);
		this.method = method;
	}

	public void setUrl(String url) {
		this.url = url;
		if (url != null) {
			try {
			    final URI uri = new URI(url);
                Assert.hasText(uri.getHost(), "The url maybe bad:[" + url + "]");
                Assert.hasText(uri.getScheme(), "The url maybe bad:[" + url + "]");
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
		}
	}
	
	@JsonIgnore
    public String getHost() {
	    Assert.hasText(url, "The url maybe bad");
	    if (host != null) {
            return host;
        }
        try {
            host = new URI(url).getHost();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return host;
    }
}
