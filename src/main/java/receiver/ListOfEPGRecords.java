package receiver;

/**
 * ListOfEPGRecords.java creates a REST template and collects lists of 
 * records with a given receiver ID.
 * @author pangmel
 */

import java.util.List;

import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ListOfEPGRecords {
	
	public List<EPGRecord> epgrecords;
	
	@JsonCreator
	public ListOfEPGRecords(@JsonProperty("epgrecords")List<EPGRecord> epgrecords) {
		this.epgrecords = epgrecords;
	}	

	public void setRecords(List<EPGRecord> epgrecords) {
		this.epgrecords = epgrecords;
	}
	
	public List<EPGRecord> getEPGRecords() {
		return epgrecords;
	}
	
    public static void main(String[] args) {
    	RestTemplate rest = new RestTemplate();
    	ListOfEPGRecords list = rest.getForObject("http://10.76.243.80/epg/seriesFutureShowings.pl?series=134219535", ListOfEPGRecords.class);
//    	for (EPGRecord r : list.getEPGRecords())
//    		System.out.println(r.get);
    	System.out.println(list.getEPGRecords());
    	
    }

}