package model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "command")
public class HistoryCommand {
    private String id;
    private HistoryContent history;

    @XmlAttribute
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement
    public HistoryContent getHistory() {
        return history;
    }

    public void setHistory(HistoryContent history) {
        this.history = history;
    }
}

