package server;

import controller.JsonControllerImpl;
import model.Currency;
import model.requests.CurrencyHistoryRequest;
import model.requests.CurrencyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import repository.CurrenciesHistory;
import repository.RatesCollector;

import java.util.List;
import java.util.Map;


@SpringBootApplication
@ComponentScan(basePackages = "controller;model;repository")
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);

        System.out.println("<<< READY >>>");


//        To test manually
//
//        Server 1: Json requests
//        JsonControllerImpl server1 = new JsonControllerImpl();
//        CurrencyRequest requestCurrent = new CurrencyRequest("b89577fe-8c37-4962-8af3-7cb89a245160",
//                "1714992664", "1234", "USD");
//        CurrencyRequest requestCurrent2 = new CurrencyRequest("b89577fe-8c37-4962-8af3-7cb89a245161",
//                "1714992664", "1234", "USD");
//        CurrencyHistoryRequest requestHistory = new CurrencyHistoryRequest("b89577fe-8c37-4962-8af3-7cb89a245162",
//                "1714992664", "1234", "USD", 24);
//
//        double usdCurrentValue = server1.getCurrencyInfo(requestCurrent);
//        System.out.println("USD current value is: " + usdCurrentValue);
//
//        Map<String, Double> currencies = server1.getAllCurrenciesByBaseCurrency(requestCurrent2);
//        System.out.println("all values by usd base are: " + currencies);
//
//        List<Currency> usdHistory = server1.getHistory(requestHistory);
//        System.out.println("USD history is: " + usdHistory);


//        Server 2: XML requests
//        XmlControllerImpl server2 = new XmlControllerImpl();
//        GetContent getContent = new GetContent();
//        getContent.setCurrency("USD");
//        getContent.setConsumer("1234");
//        CurrentCommand requestCurrentAsXML = new CurrentCommand();
//        requestCurrentAsXML.setGetCommand(getContent);
//        requestCurrentAsXML.setId("b89577fe-8c37-4962-8af3-7cb89a245160");
//
//        HistoryContent historyContent = new HistoryContent();
//        historyContent.setCurrency("USD");
//        historyContent.setConsumer("1234");
//        historyContent.setPeriod(24);
//        HistoryCommand requestHistoryAsXML = new HistoryCommand();
//        requestHistoryAsXML.setHistory(historyContent);
//        requestHistoryAsXML.setId("b89577fe-8c37-4962-8af3-7cb89a245160");
//
//        XmlCurrencyCurrentValueResponse usdCurrentValueAsXML = server2.getCurrencyInfo(requestCurrentAsXML);
//        System.out.println("USD current value is: " + usdCurrentValueAsXML.getValue());
//
//        XmlCurrenciesByBaseResponse currenciesAsXML = server2.getAllCurrenciesByBaseCurrency(requestCurrentAsXML);
//        System.out.println("all values by " + currenciesAsXML.getBase() + " base are: " + currenciesAsXML.getRates());
//
//        XmlHistoryResponse usdHistoryAsXML = server2.getHistory(requestHistoryAsXML);
//        System.out.println("USD history is: " + usdHistoryAsXML.getHistory());
    }
}