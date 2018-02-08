package com.tjoydoor;

import com.luoxudong.app.threadpool.ThreadPoolHelp;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {


    @Test
    public void  threadTest() throws InterruptedException {

        while (true){

            Thread.sleep(200);

            System.out.println("每五百毫秒开启一个苹果线 = "+Thread.currentThread().getName());

            ThreadPoolHelp.Builder.cached().builder().execute(new Runnable() {
                @Override
                public void run() {

                    try {
                        Thread.sleep(1000);
                        System.out.println("一秒钟生产一个苹果 = "+Thread.currentThread().getName());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

}