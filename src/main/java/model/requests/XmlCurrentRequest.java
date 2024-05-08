package model.requests;

import model.GetContent;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "command")
public class XmlCurrentRequest implements XmlRequest {
    private String id;
    private GetContent getContent;

    @XmlAttribute
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement(name = "get")
    public GetContent getGetContent() {
        return getContent;
    }

    public void setGetCommand(GetContent getContent) {
        this.getContent = getContent;
    }
}
