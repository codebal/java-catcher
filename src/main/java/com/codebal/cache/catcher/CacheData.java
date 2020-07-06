package com.codebal.cache.catcher;

import java.io.Serializable;
import java.util.Date;

public class CacheData implements Serializable {

    private static final long serialVersionUID = 1L;

    static final int KEY_MAX_LENGTH = 200;

    public enum Status {
        NEW,
        NORMAL,
        CREATING
    }

    private Object data = null;
    public String key;
    public Status status;
    public Date crt_dt;
    //public Date refresh_dt;  //데이터를 리프레시 하기 위한 만료 시간
    //public Date expire_dt = null;  //데이터가 삭제되는 시간.
    public Integer refresh_ms;
    public Integer expire_ms;
    //public Date hit_dt;
    //public int hit_cnt;
    public boolean asyncRefresh = true; //리프레시를 비동기로
    public boolean nonBlocking = false; //true: 캐시 존재여부 상관없이 값을 리턴, false: 캐시가 존재하지 않을경우 생성될때 까지 대기한후 리턴
    private boolean creating = false;

    private Catcher.CacheCreateErrorHandle cacheCreateErrorHandle;

    public CacheData(String key, Object data, Status status, int refresh_sec, int expire_sec, Boolean asyncRefresh, Boolean nonBlocking){
        init(key, data, status, refresh_sec*1000, expire_sec*1000, asyncRefresh, nonBlocking);
    }

    public Date getRefresh_dt(){
        return new Date(crt_dt.getTime() + refresh_ms);
    }

    public Date getExpire_dt(){
        return new Date(crt_dt.getTime() + expire_ms);
    }

    public int getRefresh_sec(){
        return refresh_ms / 1000;
    }

    public int getExpire_sec(){
        return expire_ms / 1000;
    }

    void init(String key, Object data, Status status, int refresh_ms, int expire_ms, Boolean asyncRefresh, Boolean nonBlocking){
        this.key = getLimitCacheKey(key);

        this.data = data;
        this.refresh_ms = refresh_ms;
        this.expire_ms = expire_ms;

        this.crt_dt = new Date();

        if(asyncRefresh != null)
            this.asyncRefresh = asyncRefresh;

        if(nonBlocking != null)
            this.nonBlocking = nonBlocking;

        this.status = status;

        this.cacheCreateErrorHandle = Catcher.CacheCreateErrorHandle.REUSE;

        //toString();
    }

    public boolean needRefresh(){
        return getRefresh_dt().getTime() < System.currentTimeMillis();
    }

    public boolean needForceRefresh(){
        return (getRefresh_dt().getTime() + 10*1000) < System.currentTimeMillis();
    }

    public <T> T getData(){
        if(this.data == null)
            return null;

        return (T)this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void refreshData(Object data){
        crt_dt = new Date();
        setData(data);
    }

    public boolean isCreating() {
        return creating;
    }

    public void setCreating(boolean creating) {
        this.creating = creating;
    }

    public long getExpireSecLeft(){
        return this.getRefresh_dt().getTime() - System.currentTimeMillis();
    }

    public Long getDeleteSecLeft(){
        return this.getExpire_dt().getTime() - System.currentTimeMillis();
    }

    public Catcher.CacheCreateErrorHandle getCacheCreateErrorHandle() {
        return cacheCreateErrorHandle;
    }

    public void setCacheCreateErrorHandle(Catcher.CacheCreateErrorHandle cacheCreateErrorHandle) {
        this.cacheCreateErrorHandle = cacheCreateErrorHandle;
    }

    @Override
    public String toString() {
        return "CacheData{" +
                "data=" + data +
                ", key='" + key + '\'' +
                ", status=" + status +
                ", crt_dt=" + crt_dt +
                ", refresh_dt=" + getRefresh_dt() +
                ", expire_dt=" + getExpire_dt() +
                ", refresh_sec=" + getRefresh_sec() +
                ", expire_sec=" + getExpire_sec() +
                ", asyncRefresh=" + asyncRefresh +
                ", nonBlocking=" + nonBlocking +
                ", creating=" + creating +
                ", cacheCreateErrorHandle=" + cacheCreateErrorHandle +
                '}';
    }

    static public String getLimitCacheKey(String key){
        if(key.length() > KEY_MAX_LENGTH){
            return key.substring(0, KEY_MAX_LENGTH-1);
        }
        else{
            return key;
        }
    }


}
