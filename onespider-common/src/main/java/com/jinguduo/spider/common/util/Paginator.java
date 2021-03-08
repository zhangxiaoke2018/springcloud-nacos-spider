package com.jinguduo.spider.common.util;

import java.util.Collection;

import lombok.Data;

/**
 * 分页model
 * 
 *
 * @param <T>
 */
@Data
public class Paginator<T> {
    
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 100;

    private Collection<T> entites;
    private Integer page = 1;  // current page number
    private Integer size = 100;  // size by page
    private Integer pageCount = 1;  // total page of all records
    
    public Paginator() {
        super();
    }
    
    public Paginator(int page, int size) {
        this();
        this.page = Math.max(page, DEFAULT_PAGE);
        this.size = size < 1 ? DEFAULT_SIZE : size;
    }
    
    public Paginator(int page, int size, int recordCount) {
        this(page, size);
        this.pageCount = Math.max(1, Math.round(recordCount / size));
        this.page = page > pageCount ? pageCount : page;
    }
    
    public boolean isFristPage() {
        return page == 1;
    }
    
    public boolean isLastPage() {
        return page == pageCount;
    }
    
    public boolean hasNextPage() {
        return page < pageCount;
    }
    
    public int getPageNext() {
        return page < pageCount ? page + 1 : pageCount;
    }
    
    public boolean hasPrevPage() {
        return page > 1;
    }
    
    public int getPagePrev() {
        return page > 1 ? page -1 : page;
    }

    public int getOffset() {
        return (int)(page * size);
    }
}
