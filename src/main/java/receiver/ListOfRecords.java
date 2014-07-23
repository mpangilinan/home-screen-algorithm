package receiver;

/**
 * ListOfRecords.java creates a REST template and collects lists of 
 * records with a given receiver ID.
 * @author pangmel
 */

import java.util.List;

import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ListOfRecords {
	
	public List<Record> records;
	
	@JsonCreator
	public ListOfRecords(@JsonProperty("records")List<Record> records) {
		this.records = records;
	}
	

	public void setRecords(List<Record> records) {
		this.records = records;
	}
	
	public List<Record> getRecords() {
		return records;
	}
	
    public static void main(String[] args) {
    	RestTemplate rest = new RestTemplate();
    	ListOfRecords list = rest.getForObject("http://10.76.243.80/stb/seriesHistory.pl?recId=28a0a95baa35ea0d72d265f93346456673f13496fc25", ListOfRecords.class);
    	//for (Record r : list.getRecords())
    		//System.out.println(r.getRecords());
    	System.out.println(list.getRecords());
    	
    }

}