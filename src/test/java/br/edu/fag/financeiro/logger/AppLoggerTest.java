package br.edu.fag.financeiro.logger;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class AppLoggerTest {

    @Test
    void construtorDeveSerPrivado() throws Exception {
        Constructor<AppLogger> c = AppLogger.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(c.getModifiers()));
    }

    @Test
    void getInstanceDeveRetornarSempreAMesmaInstancia() {
        assertSame(AppLogger.getInstance(), AppLogger.getInstance());
    }

    @Test
    void deveSerThreadSafe() throws Exception {
        int threads = 50;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch largada = new CountDownLatch(1);
        Set<AppLogger> instancias = ConcurrentHashMap.newKeySet();
        CountDownLatch fim = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    largada.await();
                    instancias.add(AppLogger.getInstance());
                } catch (InterruptedException ignored) {
                } finally {
                    fim.countDown();
                }
            });
        }
        largada.countDown();
        fim.await();
        pool.shutdown();

        assertEquals(1, instancias.size());
    }
}
