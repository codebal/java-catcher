package com.codebal.cache.catcher;

import java.io.Serializable;
import java.util.Date;

public class CacheData implements Serializable {

    static final int KEY_MAX_LENGTH = 200;

    private Object data = null;
    public String key;
    public Date crt_dt;
    public Date refresh_dt;  //데이터를 리프레시 하기 위한 만료 시간
    public Date expire_dt = null;  //데이터가 삭제되는 시간.
    public int refresh_sec;
    public int expire_sec;
    public Date hit_dt;
    public int hit_cnt;
    public boolean asyncRefresh = true; //리프레시를 비동기로
    public boolean startNotNull = true; //캐시가 존재하지 않는 경우, null을 반환하지 않고 생성될때까지 대기한후 값을 반환
    private boolean creating = false;

    public CacheData(String key, Object data, int refresh_sec){
        init(key, data, new Date(System.currentTimeMillis() + refresh_sec*1000), null, null, null);
    }

    public CacheData(String key, Object data, int refresh_sec, int expire_sec, Boolean asyncRefresh, Boolean startNotNull){
        init(key, data, new Date(System.currentTimeMillis() + refresh_sec*1000), new Date(System.currentTimeMillis() + expire_sec*1000), asyncRefresh, startNotNull);
    }

    void init(String key, Object data, Date refresh_dt, Date expire_dt, Boolean asyncRefresh, Boolean startNotNull){
        this.key = getLimitCacheKey(key);

        this.data = data;
        this.refresh_dt = refresh_dt;
        this.expire_dt = expire_dt;

        this.crt_dt = new Date();
        this.hit_dt = new Date();
        this.hit_cnt = 0;

        this.refresh_sec = Long.valueOf((this.refresh_dt.getTime() - System.currentTimeMillis()) / 1000).intValue();

        //삭제시간을 안 넣으면, 기본값 만료시간의 2배
        if(this.expire_dt != null){
            this.expire_sec = Long.valueOf((this.expire_dt.getTime() - System.currentTimeMillis()) / 1000).intValue();
        }
        else{
            this.expire_sec = this.refresh_sec * 2;
            this.expire_dt = new Date( System.currentTimeMillis() + this.expire_sec * 1000 );
        }

        if(asyncRefresh != null)
            this.asyncRefresh = asyncRefresh;

        if(startNotNull != null)
            this.startNotNull = startNotNull;

        //toString();
    }

    public boolean needRefresh(){
        return this.refresh_dt.getTime() < System.currentTimeMillis();
    }

    public boolean needForceRefresh(){
        return (this.refresh_dt.getTime() + 60*1000) < System.currentTimeMillis();
    }

    public <T> T getData(){
        if(this.data == null)
            return null;

        return (T)this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public boolean isCreating() {
        return creating;
    }

    public void setCreating(boolean creating) {
        this.creating = creating;
    }

    public long getExpireSecLeft(){
        return this.refresh_dt.getTime() - System.currentTimeMillis();
    }

    public Long getDeleteSecLeft(){
        if(expire_dt == null)
            return null;

        return this.expire_dt.getTime() - System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "CacheData{" +
                "data=" + data +
                ", key='" + key + '\'' +
                ", crt_dt=" + crt_dt +
                ", refresh_dt=" + refresh_dt +
                ", expire_dt=" + expire_dt +
                ", refresh_sec=" + refresh_sec +
                ", expire_sec=" + expire_sec +
                ", hit_dt=" + hit_dt +
                ", hit_cnt=" + hit_cnt +
                ", creating=" + creating +
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
