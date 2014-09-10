package homeScreenAlgorithm;

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.springframework.web.client.RestTemplate;

@XmlRootElement(name="TopInDMA")
public class ListOfWhatsHotCheck {

	private List<WhatsHotCheck> whatsHotCheckItems;
	


	public List<WhatsHotCheck> getWhatsHotCheckItems() {
		return whatsHotCheckItems;
	}

	@XmlElement(name="Row")
	public void setWhatsHotCheckItems(List<WhatsHotCheck> whatsHotCheckItems) {
		this.whatsHotCheckItems = whatsHotCheckItems;
	}
	
	public static void main(String[] args) {
		RestTemplate rest = new RestTemplate();
		ListOfWhatsHotCheck list = rest.getForObject("http://vmeasuredl.dishaccess.tv/Now/National/Drama.xml", ListOfWhatsHotCheck.class);
		System.out.println(list.getWhatsHotCheckItems());
	}
}
