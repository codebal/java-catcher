package com.codebal.cache.catcher;

public interface ICatcherSignal {
    Boolean cacheResourceSetter(CacheData cacheData);
    CacheData cacheResourceGetter(Object ccData);
}
