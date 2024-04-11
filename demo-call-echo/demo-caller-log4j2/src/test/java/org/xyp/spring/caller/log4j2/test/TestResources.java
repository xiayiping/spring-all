package org.xyp.spring.caller.log4j2.test;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.xyp.spiring.caller.log4j2.DemoController;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class TestResources {

    @Autowired
    DemoController controller;

    @Autowired
    RedissonClient redissonClient;

    @Test
    void testResource() throws Exception {
        Locker locker = new RedisLocker(redissonClient.getFairLock("xypLock1"), 3000, 1000);
//    Locker locker = new LocalLocker();
        try (var ignored = new MyResource(locker)) {
            System.out.println("do something");
            Assertions.assertThat(ignored).isNotNull();
        }
    }

    @Test
    void testInitialized() {
        Assertions.assertThat(redissonClient).isNotNull();
    }

    static AtomicInteger count = new AtomicInteger(100000);

    @Test
    void testLockForeach() throws Exception  {

        Random r = new Random();
        val maxT = 6;
        val latch = new CountDownLatch(maxT);
        Locker locker = new RedisLocker(redissonClient.getFairLock("xypLock1"), 3000, 1000);
//    Locker locker = new LocalLocker();

        for (int i = 0; i < maxT; i++) {
            new Thread(() -> {

                while (count.get() > 0) {
                    try (val ignored = new MyResource(locker)) {
                        var rem = count.decrementAndGet();
                        if (rem % 3000 == 0) {
                            System.out.println("remained: " + rem);
                        }
                        if (r.nextInt(1000) <= 10) {
                            throw new RuntimeException("fake exp " + System.currentTimeMillis() + " " + Thread.currentThread().getName());
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    } finally {
                    }
                }
                latch.countDown();

            }).start();
        }
        latch.await();
    }

    @Test
    void testLock1() throws Exception {
        Locker locker = new RedisLocker(redissonClient.getFairLock("xypLock4"), 300000, 1000000);
        try (var ignored = new MyResource(locker)) {
            System.out.println("lock got, before sleep, enter something to continue: ");
            val a = System.in.read();
            System.out.println("lock got, after sleep " + a);
        }
    }

    @Test
    void testLock2() throws Exception {
        Locker locker = new RedisLocker(redissonClient.getFairLock("xypLock4"), 300000, 1000000);
        try (var ignored = new MyResource(locker)) {
            System.out.println("lock 2 got, before sleep, enter something to continue: ");
            val a = System.in.read();
            System.out.println("lock 2 got, after sleep " + a);
        }
    }


    @Test
    void testLockFork() throws Exception {
        val start = System.currentTimeMillis();
        Locker locker = new RedisLocker(redissonClient.getFairLock("xypLock4"), 3000, 1000);
//        Locker locker = new RedisLocker(redissonClient.getLock("xypLock3"));
//    Locker locker = new LocalLocker();


//        val result = ForkJoinPool.commonPool()
//            .submit(new LockRec(locker, count.get()))
//            .get();
//        System.out.println(result);

//        Observation.Context context;
//        context.setName();
        int threadCt = 2;
        Random r = new Random();
        val latches = new CountDownLatch(threadCt);
        for (int i = 0; i < threadCt; i++) {
            new Thread(() -> {
                var rem = count.decrementAndGet();
                try {
                    while (rem >= 0) {
                        try (var ignored = new MyResource(locker)) {
                            Thread.sleep(200000);
//                            if (rem % 3000 == 0) {
                                System.out.println("remained: " + rem + " " + Thread.currentThread().getName());
//                            }
                            if (r.nextInt(1000) <= 10) {
                                throw new RuntimeException("fake exp " + rem);
                            }
                        } catch (Throwable t) {
                            System.out.println(t.getMessage());
                        } finally {
                            rem = count.decrementAndGet();
                        }
                    }
                } finally {
                    latches.countDown();
                }
            }).start();
        }
        latches.await();


        System.out.println(System.currentTimeMillis() - start);
    }


    static class LockRec extends RecursiveTask<Integer> {
        private final Locker lock;
        private final int remained;
        Random r = new Random();

        LockRec(Locker lock, int remained) {
            this.lock = lock;
            this.remained = remained;
        }

        @Override
        protected Integer compute() {
            if (remained <= 1) {
                try (val ignored = new MyResource(lock)) {
                    var rem = count.decrementAndGet();
                    if (rem % 3000 == 0) {
                        System.out.println("remained: " + rem);
                    }
                    if (r.nextInt(1000) <= 10) {
                        throw new RuntimeException("fake exp " + rem);
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                } finally {
                    return 1;
                }
            } else {
                val left = remained / 2;
                val right = remained / 2 + remained % 2;

                return ForkJoinTask.invokeAll(List.of(
                        new LockRec(lock, left),
                        new LockRec(lock, right)))
                    .stream().map(ForkJoinTask::join)
                    .mapToInt(t -> t).sum();
            }
        }
    }

    static class MyResource implements AutoCloseable {

        private final Locker locker;

        public MyResource(Locker locker) {
            this.locker = locker;
            locker.lock();
        }

        @Override
        public void close() throws Exception {
            locker.release();
        }
    }

    interface Locker {
        void lock();

        void release();
    }

    static class LocalLocker implements Locker {
        private ReentrantLock underlying = new ReentrantLock(true);

        @Override
        public void lock() {
            underlying.lock();
        }

        @Override
        public void release() {
            underlying.unlock();
        }
    }

    static class RedisLocker implements Locker {

        private final RLock rLock;
        private long wait;
        private long lease;
        RedisLocker(RLock rLock, long wait, long lease) {
            this.rLock = rLock;
            this.wait = wait;
            this.lease = lease;
        }

        private boolean locked = false;

        @Override
        public void lock() {
            try {
                locked = rLock.tryLock(wait, lease, TimeUnit.SECONDS);
//                locked = rLock.lock(1L, TimeUnit.SECONDS);
                if (!locked) {
                    throw new RuntimeException("lock not got " + Thread.currentThread());
                }
            } catch (InterruptedException ite) {
                throw new RuntimeException("interrupt exception");
            }
        }

        @Override
        public void release() {
            if (locked)
                rLock.unlock();
        }
    }

    public static void main(String[] args) {
        System.out.println(new Date(1707667296793L));

    }
}
