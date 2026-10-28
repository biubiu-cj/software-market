package software_market.service.impl;

import org.junit.jupiter.api.Test;
import software_market.entity.RefreshToken;
import software_market.exception.BizException;
import software_market.mapper.RefreshTokenMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTokenServiceTest {

    private final RefreshTokenMapper mapper = mock(RefreshTokenMapper.class);
    private final RefreshTokenService service = new RefreshTokenService(mapper);

    @Test
    void consumeSucceedsWhenTokenUnusedAndUserIdMatches() {
        when(mapper.selectByToken("rt")).thenReturn(record(false, LocalDateTime.now().plusDays(1)));
        when(mapper.markUsedIfUnused("rt")).thenReturn(1);

        service.consume("rt", 7);

        verify(mapper).markUsedIfUnused("rt");
    }

    @Test
    void reusedTokenIsRejected() {
        when(mapper.selectByToken("rt")).thenReturn(record(true, LocalDateTime.now().plusDays(1)));

        assertThrows(BizException.class, () -> service.consume("rt", 7));
        verify(mapper, never()).markUsedIfUnused("rt");
    }

    @Test
    void expiredTokenIsRejected() {
        when(mapper.selectByToken("rt")).thenReturn(record(false, LocalDateTime.now().minusSeconds(1)));

        assertThrows(BizException.class, () -> service.consume("rt", 7));
        verify(mapper, never()).markUsedIfUnused("rt");
    }

    @Test
    void userIdMismatchIsRejected() {
        when(mapper.selectByToken("rt")).thenReturn(record(false, LocalDateTime.now().plusDays(1)));

        assertThrows(BizException.class, () -> service.consume("rt", 8));
        verify(mapper, never()).markUsedIfUnused("rt");
    }

    @Test
    void concurrentRefreshOnlyOneSucceeds() throws Exception {
        // 模拟两个请求同时读到“未使用”记录，随后竞争条件更新，只有第一个能拿到 1 行受影响
        when(mapper.selectByToken("rt")).thenReturn(record(false, LocalDateTime.now().plusDays(1)));
        AtomicInteger updateCalls = new AtomicInteger();
        when(mapper.markUsedIfUnused("rt")).thenAnswer(invocation ->
                updateCalls.getAndIncrement() == 0 ? 1 : 0);

        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                start.await();
                try {
                    service.consume("rt", 7);
                    return true;
                } catch (BizException e) {
                    return false;
                }
            }));
        }
        start.countDown();

        int success = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) {
                success++;
            }
        }
        executor.shutdown();

        assertEquals(1, success, "同一个 Refresh Token 并发刷新最多只能成功一次");
    }

    private RefreshToken record(boolean used, LocalDateTime expiresAt) {
        RefreshToken record = new RefreshToken();
        record.setUserId(7);
        record.setToken("rt");
        record.setIsUsed(used);
        record.setExpiresAt(expiresAt);
        return record;
    }
}
