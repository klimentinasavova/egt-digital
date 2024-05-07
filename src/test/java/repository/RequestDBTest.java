package repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import redis.clients.jedis.Jedis;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class RequestDBTest {
    private RequestDB requestDB;

    @Mock
    private Jedis jedisMock;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        requestDB = new RequestDB();
        requestDB.jedis = jedisMock;
    }

    @AfterEach
    public void tearDown() {
        requestDB.close();
    }

    @Test
    public void testAddRequestId_whenRequestIdDoesNotExist_shouldSucceed() {
        when(jedisMock.setnx("123", "")).thenReturn(1L);

        boolean result = requestDB.addRequestId("123");

        assertTrue(result);
        verify(jedisMock, times(1)).setnx("123", "");
    }

    @Test
    public void testAddRequestId_whenRequestIdAlreadyExists_shouldFail() {
        when(jedisMock.setnx("456", "")).thenReturn(0L);

        boolean result = requestDB.addRequestId("456");

        assertFalse(result);
        verify(jedisMock, times(1)).setnx("456", "");
    }

    @Test
    public void testClose() {
        requestDB.close();
        verify(jedisMock, times(1)).close();
    }
}
