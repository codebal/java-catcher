package com.codebal.cache.catcher;

public class CacheException extends Exception {
    private CacheData cacheData;
    static public CacheException make(Exception e, CacheData cacheData){
        CacheException cacheException = (CacheException)e;
        cacheException.setCacheData(cacheData);
        return cacheException;
    }

    public CacheData getCacheData() {
        return cacheData;
    }

    public void setCacheData(CacheData cacheData) {
        this.cacheData = cacheData;
    }
}
