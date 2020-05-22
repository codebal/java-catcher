package com.codebal.cache.catcher;

public class CacheError {
    private CacheData cacheData;
    private Exception exception;

    static CacheError make(Exception exception, CacheData cacheData){
        return new CacheError(exception, cacheData);
    }

    public CacheError(Exception exception, CacheData cacheData){
        this.exception = exception;
        this.cacheData = cacheData;
    }

    public CacheData getCacheData() {
        return cacheData;
    }

    public void setCacheData(CacheData cacheData) {
        this.cacheData = cacheData;
    }
}
