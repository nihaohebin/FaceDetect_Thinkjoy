package com.tjoydoor.util.threadpool;

import java.util.concurrent.RejectedExecutionException;

public class ServiceThreadPoolManager {

    private static ServiceThreadPoolManager instance = new ServiceThreadPoolManager();
    private ServiceThreadPool threadPool;

    private ServiceThreadPoolManager() {
        this.threadPool = new ServiceThreadPool(20, 100, 30, 1000);
    }

    public static ServiceThreadPoolManager getInstance() {
        if (instance == null) {
            instance = new ServiceThreadPoolManager();
        }
        return instance;
    }

    public void execute(Runnable r) throws RejectedExecutionException {
        this.threadPool.execute(r);
    }
}
