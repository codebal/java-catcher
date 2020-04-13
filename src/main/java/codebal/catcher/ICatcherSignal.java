package codebal.catcher;

public interface ICatcherSignal {
    Boolean cacheResourceSetter(CcData ccData);
    CcData cacheResourceGetter(Object ccData);
}
