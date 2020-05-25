package com.codebal.cache.catcher;

public interface ICatcherSignal {
    Boolean cacheResourceSetter(CacheData cacheData);
    CacheData cacheResourceGetter(Object cacheKey);

    CacheData cacheCreateCustomErrorHandler(CacheError cacheError);
    CacheData waitTimeoverHandler(CacheData cacheData);
}
