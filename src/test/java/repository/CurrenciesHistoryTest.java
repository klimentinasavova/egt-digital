package repository;

import model.Currency;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class CurrenciesHistoryTest {

    @Spy
    private CurrenciesHistory db = spy(new CurrenciesHistory());

    ResultSet resultSet = mock(ResultSet.class);

    @Test
    public void test_parseTransaction_withValidTransaction_shouldSucceed() throws SQLException {
        String timestamp = "1714992664";
        String currency = "USD";
        double value = 1.076924;

        when(resultSet.getString("timestamp")).thenReturn(timestamp);
        when(resultSet.getString("currency")).thenReturn(currency);
        when(resultSet.getString("value")).thenReturn(String.valueOf(value));

        Currency result = db.parseCurrency(resultSet);

        Assertions.assertEquals(timestamp, result.timestamp());
        Assertions.assertEquals(currency, result.currency());
        Assertions.assertEquals(value, result.value());
    }

    @Test
    public void test_parseCurrency_withInvalidCurrency_shouldThrowSQLException() throws SQLException {
        when(resultSet.getString("timestamp")).thenThrow(SQLException.class);

        Assertions.assertThrows(SQLException.class,
                () -> db.parseCurrency(resultSet));
    }

}
