package model.dto.responses;

import model.Currency;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "currencies")
public class XmlCurrenciesByBaseResponse {

    private Map<String, Double> rates;
    private String base;

    public XmlCurrenciesByBaseResponse(Map<String, Double> rates, String currency) {
        this.rates = rates;
        this.base = currency;
    }

    @XmlElement(name = "rates")
    public Map<String, Double> getRates() {
        return rates;
    }

    public void setRates(Map<String, Double> rates) {
        this.rates = rates;
    }

    @XmlElement(name = "base")
    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }
}
