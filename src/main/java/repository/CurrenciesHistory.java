package repository;

import controller.JsonControllerImpl;
import model.Currency;
import model.dto.responses.CurrencyResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

@Repository
public class CurrenciesHistory {

    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5433/postgres}")
    private String url;

    @Value("${spring.datasource.username:gateway}")
    private String username;

    @Value("${spring.datasource.password:gateway}")
    private String password;

    private static final String TABLE_NAME = "currencies";
    private final Logger logger = LogManager.getLogger(CurrenciesHistory.class);


    public CurrenciesHistory() {
        initializeCurrenciesTable();
    }

    public void initializeCurrenciesTable() {

        //Because of the limited requests count provided by "fixer.io" free subscription plan DB won't be cleaned up
        // in order to have more currency data stored in the DB
        // deleteTables();

        //Create currencies table if it does not exist
        createTable();
    }

    public void saveCurrenciesData(CurrencyResponse cr) {
        String insertSQL = String.format("INSERT INTO %s (timestamp, currency, value ) VALUES (?, ?, ?)", TABLE_NAME);

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {

            preparedStatement.setString(1, cr.timestamp());

            for(String currency : cr.rates().keySet()) {
                preparedStatement.setString(2, currency);
                preparedStatement.setString(3, cr.rates().get(currency).toString());
                preparedStatement.executeUpdate();
                logger.trace("Currency: {} inserted successfully.", currency);
            }

        } catch (SQLException e) {
            logger.error("Error saving currency data: {}", e.getMessage());
        }
    }

    public List<Currency> getAllCurrencies() {
        String selectSQL = String.format("SELECT * FROM %s ", TABLE_NAME);

        List<Currency> currencies = new LinkedList<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(selectSQL);

            while (resultSet.next()) {
                currencies.add(parseCurrency(resultSet));
            }
            logger.debug("Successfully retrieved currencies data from table {}.", TABLE_NAME);
        } catch (SQLException e) {
            logger.error("Error retrieving currencies from table: {}", e.getMessage());
        }
        return  currencies;
    }

    public List<Currency> getCurrencyByPeriod( String currency, long period) {
        String selectSQL = String.format("SELECT * FROM %s WHERE currency='%s' AND timestamp>'%s';",
                TABLE_NAME, currency, String.valueOf(period));
        List<Currency> currencyData = new LinkedList<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(selectSQL);

            while (resultSet.next()) {
                currencyData.add(parseCurrency(resultSet));
            }
            logger.debug("Successfully retrieved currency data from table {}.", TABLE_NAME);

        } catch (SQLException e) {
            logger.error("Error retrieving currency data from table: {}", e.getMessage());
        }
        return currencyData;
    }

    public List<Currency> getCurrency( String currency) {
        String selectSQL = String.format("SELECT * FROM %s WHERE currency='%s';", TABLE_NAME, currency);
        List<Currency> currencyData = new LinkedList<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(selectSQL);

            while (resultSet.next()) {
                currencyData.add(parseCurrency(resultSet));
            }
            logger.debug("Successfully retrieved currency data from table {}.", TABLE_NAME);

        } catch (SQLException e) {
            logger.error("Error retrieving currency data from table: {}", e.getMessage());
        }
        return currencyData;
    }

    protected Currency parseCurrency(ResultSet resultSet) throws SQLException {
        String timestamp = resultSet.getString("timestamp");
        String currency = resultSet.getString("currency");
        Double value = Double.valueOf(resultSet.getString("value"));

        return new Currency(
                timestamp,
                currency,
                value);
    }

    private void createTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( "
                + "id SERIAL PRIMARY KEY,"
                + "timestamp VARCHAR(255), "
                + "currency VARCHAR(3), "
                + "value VARCHAR(20));";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(createTableSQL);
            logger.info("Table with name {} created successfully.", TABLE_NAME);

        } catch (SQLException e) {
            logger.error("Table creation failed: {}", e.getMessage());
        }
    }

    private void deleteTables() {
        String deleteTableStatement = String.format("DROP TABLE IF EXISTS %s" + TABLE_NAME);

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(deleteTableStatement);
            logger.info("Table with name {} deleted successfully.", TABLE_NAME);

        } catch (SQLException e) {
            logger.error("Table deletion failed: {}", e.getMessage());
        }
    }
}
