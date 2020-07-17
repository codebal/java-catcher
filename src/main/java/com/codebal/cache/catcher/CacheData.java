package com.codebal.cache.catcher;

import java.io.Serializable;
import java.util.Date;
import com.codebal.cache.catcher.logger.CacheLogger;

public class CacheData implements Serializable {

    private static final long serialVersionUID = 1L;

    static final int KEY_MAX_LENGTH = 200;

    public enum Status {
        NEW,
        NORMAL,
        CREATING,
        NEW_CREATING
    }

    private Object data = null;
    public String key;
    public Status status;
    public Date create_dt = null; //캐시 생성 시간
    public Date update_dt = null; //캐시 변경 시간
    //public Date update_dt;  //데이터를 리프레시 하기 위한 만료 시간
    //public Date expire_dt = null;  //데이터가 삭제되는 시간.
    public Integer refresh_ms;
    public Integer expire_ms;
    //public Date hit_dt;
    //public int hit_cnt;
    public boolean asyncUpdate = true; //리프레시를 비동기로
    public boolean asyncNew = false; //true: 캐시 존재여부 상관없이 값을 리턴, false: 캐시가 존재하지 않을경우 생성될때 까지 대기한후 리턴
    private boolean creating = false;

    public Catcher.CacheCreateErrorHandle cacheCreateErrorHandle;

    public CacheData(String key, Object data, Status status, int refresh_sec, int expire_sec, Boolean asyncUpdate, Boolean asyncNew, Catcher.CacheCreateErrorHandle cacheCreateErrorHandle){
        init(key, data, status, refresh_sec*1000, expire_sec*1000, asyncUpdate, asyncNew, cacheCreateErrorHandle);
    }

    public void mergeOldCacheData(CacheData oldCacheData){
        if(oldCacheData == null)
            return;

        setData(oldCacheData.getData());
        status = oldCacheData.status;
        create_dt = oldCacheData.create_dt;
        update_dt = oldCacheData.update_dt;
    }

    public Date getNextUpdate_dt(){
        if(create_dt == null)
            return null;
        return new Date(create_dt.getTime() + refresh_ms);
    }

    public Date getExpire_dt(){
        if(create_dt == null)
            return null;
        return new Date(create_dt.getTime() + expire_ms);
    }

    public int getRefresh_sec(){
        return refresh_ms / 1000;
    }

    public int getExpire_sec(){
        return expire_ms / 1000;
    }

    void init(String key, Object data, Status status, int refresh_ms, int expire_ms, Boolean asyncUpdate, Boolean asyncNew, Catcher.CacheCreateErrorHandle cacheCreateErrorHandle){
        this.key = getLimitCacheKey(key);

        this.data = data;
        this.refresh_ms = refresh_ms;
        this.expire_ms = expire_ms;

        if(asyncUpdate != null)
            this.asyncUpdate = asyncUpdate;

        if(asyncNew != null)
            this.asyncNew = asyncNew;

        this.status = status;
        
        // if(isNew()){
        //     setDate(true, true);
        // }
        // else{
        //     setDate(false, true);
        // }

        this.cacheCreateErrorHandle = cacheCreateErrorHandle;
        if(this.cacheCreateErrorHandle == null)
            this.cacheCreateErrorHandle = Catcher.CacheCreateErrorHandle.REUSE;

        //toString();
    }

    public boolean needRefresh(){
        if(getNextUpdate_dt() == null)
            return false;
        return getNextUpdate_dt().getTime() < System.currentTimeMillis();
    }

    public boolean needForceRefresh(){
        if(getNextUpdate_dt() == null)
            return false;
        return (getNextUpdate_dt().getTime() + 10*1000) < System.currentTimeMillis();
    }

    public void initDate(boolean set_create_dt, boolean set_update_dt){
        if(set_create_dt)
            create_dt = new Date();
        if(set_update_dt)
            update_dt = new Date();
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
        return status.equals(Status.CREATING);
    }

    public void setCreating(boolean creating) {
        initDate(false, true);
        if(creating)
            if(isNew())
                this.status = Status.NEW_CREATING;
            else
                this.status = Status.CREATING;
        else
            this.status = Status.NORMAL;
        //this.creating = creating;
    }

    public boolean isNew(){
        return status.equals(Status.NEW);
    }

    public boolean isNormal(){
        return status.equals(Status.NORMAL);
    }

    public boolean isNewCreating(){
        return status.equals(Status.NEW_CREATING);
    }

    public long getExpireSecLeft(){
        if(getNextUpdate_dt() == null)
            return 0;
        return getNextUpdate_dt().getTime() - System.currentTimeMillis();
    }

    public long getDeleteSecLeft(){
        if(getExpire_dt() == null)
            return 0;
        return getExpire_dt().getTime() - System.currentTimeMillis();
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
                ", create_dt=" + create_dt +
                ", update_dt=" + getNextUpdate_dt() +
                ", expire_dt=" + getExpire_dt() +
                ", refresh_sec=" + getRefresh_sec() +
                ", expire_sec=" + getExpire_sec() +
                ", asyncUpdate=" + asyncUpdate +
                ", asyncNew=" + asyncNew +
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
