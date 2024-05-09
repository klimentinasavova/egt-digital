package model.dto.responses;

import model.Currency;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "history")
public class XmlHistoryResponse {
    private List<Currency> history;

    public XmlHistoryResponse(List<Currency> history) {
        this.history = history;
    }

    @XmlElement(name = "currency")
    public List<Currency> getHistory() {
        return history;
    }

    public void setHistory(List<Currency> history) {
        this.history = history;
    }
}
