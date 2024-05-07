package model.responses;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "current_value")
public class XmlCurrencyCurrentValueResponse {
    private double value;

    public XmlCurrencyCurrentValueResponse(double value) {
        this.value = value;
    }

    @XmlElement(name = "value")
    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
