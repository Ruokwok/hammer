package cc.ruok.hammer.engine;

import cc.ruok.hammer.Logger;
import org.graalvm.polyglot.Context;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class ContextPool {

    private static final int MAX_REUSE = 1000;
    private static volatile ContextPool instance;

    private final int size;
    private final Semaphore permits;
    private final Deque<PooledContext> idle = new ArrayDeque<>();
    private final org.graalvm.polyglot.Engine sharedEngine;

    private ContextPool(int size) {
        this.size = size;
        this.permits = new Semaphore(size);
        this.sharedEngine = org.graalvm.polyglot.Engine.newBuilder().build();
    }

    public static synchronized void init(int size) {
        if (instance != null) return;
        instance = new ContextPool(Math.max(1, size));
        Logger.info("JavaScript context pool size: " + instance.size);
    }

    public static ContextPool getInstance() {
        ContextPool pool = instance;
        if (pool == null) {
            synchronized (ContextPool.class) {
                if (instance == null) {
                    init(Math.max(4, Runtime.getRuntime().availableProcessors() * 2));
                }
                pool = instance;
            }
        }
        return pool;
    }

    public static synchronized void close() {
        if (instance == null) return;
        instance.closeAll();
        instance = null;
    }

    public Lease acquire() {
        permits.acquireUninterruptibly();
        PooledContext pooled;
        synchronized (this) {
            pooled = idle.pollFirst();
        }
        if (pooled == null) {
            try {
                pooled = new PooledContext(Context.newBuilder("js")
                        .allowAllAccess(true)
                        .engine(sharedEngine)
                        .build());
            } catch (RuntimeException e) {
                permits.release();
                throw e;
            }
        }
        return new Lease(this, pooled);
    }

    private void release(Lease lease, boolean reusable) {
        PooledContext pooled = lease.pooled;
        pooled.uses++;
        boolean keep = reusable && !lease.closed && pooled.uses < MAX_REUSE;
        if (keep) {
            synchronized (this) {
                idle.offerLast(pooled);
            }
        } else if (!lease.closed) {
            try {
                pooled.context.close();
            } catch (Exception e) {
                Logger.logException(e);
            }
        }
        permits.release();
    }

    private void closeAll() {
        synchronized (this) {
            for (PooledContext pooled : idle) {
                try {
                    pooled.context.close();
                } catch (Exception e) {
                    Logger.logException(e);
                }
            }
            idle.clear();
        }
        try {
            sharedEngine.close();
        } catch (Exception e) {
            Logger.logException(e);
        }
    }

    private static class PooledContext {

        private final Context context;
        private int uses;

        private PooledContext(Context context) {
            this.context = context;
        }
    }

    public static class Lease {

        private final ContextPool pool;
        private final PooledContext pooled;
        private final AtomicBoolean released = new AtomicBoolean();
        private boolean closed;

        private Lease(ContextPool pool, PooledContext pooled) {
            this.pool = pool;
            this.pooled = pooled;
        }

        public Context getContext() {
            return pooled.context;
        }

        public void closeContext() {
            if (released.get() || closed) return;
            try {
                pooled.context.close(true);
            } catch (Exception e) {
                Logger.logException(e);
            } finally {
                closed = true;
            }
        }

        public boolean isClosed() {
            return closed;
        }

        public void release(boolean reusable) {
            if (released.compareAndSet(false, true)) {
                pool.release(this, reusable);
            }
        }
    }
}
