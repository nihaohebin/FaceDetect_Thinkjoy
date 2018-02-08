package com.tjoydoor.util.threadpool;

public class ServiceThread extends Thread {

    public ServiceThread(Runnable r) {
        super(r);

    }
}