package com.codebal.cache.catcher;

public interface ICatcherSignal {
    Boolean cacheResourceSetter(CacheData cacheData);
    CacheData cacheResourceGetter(Object ccData);

    CacheData cacheCreateErrorHandler(CacheException cacheException);
    CacheData waitTimeoverHandler(CacheData cacheData);
}
