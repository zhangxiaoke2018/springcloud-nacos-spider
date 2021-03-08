package com.jinguduo.spider.common.proxy;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TargetHost {

    private String url;
    private String text;
}
