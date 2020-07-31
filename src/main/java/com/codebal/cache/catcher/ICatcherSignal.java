package com.codebal.cache.catcher;

public interface ICatcherSignal {
    Boolean cacheResourceSetter(CacheData cacheData);
    CacheData cacheResourceGetter(Object cacheKey);

    CacheData customErrorHandler(CacheError cacheError);
}
