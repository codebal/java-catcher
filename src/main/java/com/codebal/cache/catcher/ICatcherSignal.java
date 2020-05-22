package com.codebal.cache.catcher;

public interface ICatcherSignal {
    Boolean cacheResourceSetter(CacheData cacheData);
    CacheData cacheResourceGetter(Object cacheKey);

    CacheData cacheCreateErrorHandler(CacheError cacheError);
    CacheData waitTimeoverHandler(CacheData cacheData);
}
