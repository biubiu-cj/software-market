package software_market.service.impl;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software_market.mapper.RefreshTokenMapper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTokenCleanupTaskTest {

    private final RefreshTokenMapper mapper = mock(RefreshTokenMapper.class);
    private final RefreshTokenCleanupTask task = new RefreshTokenCleanupTask(mapper);

    @Test
    void cleanupDeletesWithSevenDayRetentionBoundary() {
        when(mapper.deleteExpiredOrUsedBefore(any())).thenReturn(3);

        task.cleanup();

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(mapper).deleteExpiredOrUsedBefore(captor.capture());

        LocalDateTime boundary = captor.getValue();
        assertTrue(boundary.isBefore(LocalDateTime.now().minusDays(6)), "保留期不应短于 6 天");
        assertTrue(boundary.isAfter(LocalDateTime.now().minusDays(8)), "保留期不应长于 8 天");
    }
}