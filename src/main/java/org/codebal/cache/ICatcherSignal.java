package org.codebal.cache;

public interface ICatcherSignal {
    Boolean cacheResourceSetter(CcData ccData);
    CcData cacheResourceGetter(Object ccData);
}
