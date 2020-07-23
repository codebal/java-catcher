package com.codebal.cache.catcher;

import java.io.Serializable;
import java.util.Date;

public class CacheData implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Status {
        NEW,
        NORMAL,
        CREATING,
        NEW_CREATING
    }

    private Object data = null;
    public String key;
    public Status status;
    public Date create_dt = null; //캐시 생성 완료 시간
    public Date upInfo_dt = null; //캐시 정보 변경 시간
    //public Date update_dt;  //데이터를 리프레시 하기 위한 만료 시간
    //public Date expire_dt = null;  //데이터가 삭제되는 시간.
    public Integer update_ms;
    public Integer expire_ms;
    //public Date hit_dt;
    //public int hit_cnt;
    public boolean asyncNew = false; //최초 캐시 생성을 비동기로
    public boolean asyncUpdate = true; //캐시 갱신을 비동기로
    //private boolean creating = false;

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
        upInfo_dt = oldCacheData.upInfo_dt;
    }

    public Date getNextUpdate_dt(){
        if(create_dt == null)
            return null;
        return new Date(create_dt.getTime() + update_ms);
    }

    public Date getExpire_dt(){
        if(create_dt == null)
            return null;
        return new Date(create_dt.getTime() + expire_ms);
    }

    public int getRefresh_sec(){
        return update_ms / 1000;
    }

    public int getExpire_sec(){
        return expire_ms / 1000;
    }

    void init(String key, Object data, Status status, int update_ms, int expire_ms, Boolean asyncUpdate, Boolean asyncNew, Catcher.CacheCreateErrorHandle cacheCreateErrorHandle){
        this.key = Catcher.getLimitCacheKey(key);

        this.data = data;
        this.update_ms = update_ms;
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
            upInfo_dt = new Date();
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
        //System.out.println("setCreating start : " + this.status);
        if(creating)
            if(isNew() || isNewCreating()) //NEW_CREATING 중에 캐시 생성을 또 시도하려는 경우가 발생하드라.
                this.status = Status.NEW_CREATING;
            else
                this.status = Status.CREATING;
        else
            this.status = Status.NORMAL;
        //System.out.println("setCreating end : " + this.status);
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

    public boolean isBusyNow(){
        return isCreating() || isNewCreating();
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
                ", upInfo_dt=" + upInfo_dt +
                ", nextUpdate_dt=" + getNextUpdate_dt() +
                ", expire_dt=" + getExpire_dt() +
                ", refresh_sec=" + getRefresh_sec() +
                ", expire_sec=" + getExpire_sec() +
                ", asyncUpdate=" + asyncUpdate +
                ", asyncNew=" + asyncNew +
                ", cacheCreateErrorHandle=" + cacheCreateErrorHandle +
                '}';
    }


}
