package com.codebal.cache.catcher;

public class CacheThread extends Thread {
    CacheRunner cacheRunner;

    public CacheThread(CacheRunner cacheRunner, String name) {
        super(cacheRunner, name);
        this.cacheRunner = cacheRunner;
    }

    public CacheRunner getCacheRunner() {
        return cacheRunner;
    }

    public void setCacheRunner(CacheRunner cacheRunner) {
        this.cacheRunner = cacheRunner;
    }
}
