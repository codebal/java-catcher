package org.codebal.cache;

public class CcThread extends Thread {
    CcRunner ccRunner;

    public CcThread(CcRunner ccRunner, String name) {
        super(ccRunner, name);
        this.ccRunner = ccRunner;
    }

    public CcRunner getCcRunner() {
        return ccRunner;
    }

    public void setCcRunner(CcRunner ccRunner) {
        this.ccRunner = ccRunner;
    }
}
