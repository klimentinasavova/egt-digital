package repository;

import model.Currency;
import model.responses.CurrencyResponse;
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
        String insertSQL = "INSERT INTO "+ TABLE_NAME +
                "(timestamp, " +
                "currency, " +
                "value ) " +
                "VALUES (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {

            preparedStatement.setString(1, cr.timestamp());

            for(String currency : cr.rates().keySet()) {
                preparedStatement.setString(2, currency);
                preparedStatement.setString(3, cr.rates().get(currency).toString());
                preparedStatement.executeUpdate();
                System.out.println("Currency: " + currency + " inserted successfully.");
            }

        } catch (SQLException e) {
            System.err.println("Error saving currency data: " + e.getMessage());
        }
    }

    public List<Currency> getAllCurrencies() {
        String selectSQL = "SELECT * FROM " + TABLE_NAME;

        List<Currency> currencies = new LinkedList<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(selectSQL);

            while (resultSet.next()) {
                currencies.add(parseCurrency(resultSet));
            }
            System.out.println("Successfully retrieved currencies from table " + TABLE_NAME + ".");
        } catch (SQLException e) {
            System.err.println("Error retrieving currencies from table: " + e.getMessage());
        }
        return  currencies;
    }

    public List<Currency> getCurrencyByPeriod( String currency, long period) {
        String selectSQL = "SELECT * FROM " + TABLE_NAME +
                " WHERE currency='" + currency + "' AND timestamp>'" + String.valueOf(period) + "';";
        List<Currency> currencyData = new LinkedList<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(selectSQL);

            while (resultSet.next()) {
                currencyData.add(parseCurrency(resultSet));
            }
            System.out.println("Successfully retrieved currency data from table " + TABLE_NAME + ".");

        } catch (SQLException e) {
            System.err.println("Error retrieving  currency data from table: " + e.getMessage());
        }
        return currencyData;
    }

    public List<Currency> getCurrency( String currency) {
        String selectSQL = "SELECT * FROM " + TABLE_NAME +
                " WHERE currency='" + currency + "';";
        List<Currency> currencyData = new LinkedList<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(selectSQL);

            while (resultSet.next()) {
                currencyData.add(parseCurrency(resultSet));
            }
            System.out.println("Successfully retrieved currency data from table " + TABLE_NAME + ".");

        } catch (SQLException e) {
            System.err.println("Error retrieving  currency data from table: " + e.getMessage());
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
            System.out.println("Table with name " + TABLE_NAME + " created successfully.");

        } catch (SQLException e) {
            System.err.println("Table creation failed: " + e.getMessage());
        }
    }

    private void deleteTables() {
        String deleteTableStatement = "DROP TABLE IF EXISTS " + TABLE_NAME;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(deleteTableStatement);
            System.out.println("Table with name " + TABLE_NAME + " deleted successfully.");

        } catch (SQLException e) {
            System.err.println("Table deletion failed: " + e.getMessage());
        }
    }
}
