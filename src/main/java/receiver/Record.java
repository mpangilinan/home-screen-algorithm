package receiver;

/**
 * Record.java turns JSON responses from the REST interface into 
 * Java objects that are used by Algorithm.java
 * @author pangmel
 */

import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.ArrayList;


public class Record {

	private String recId;
	private String seriesName;
	private String weekDay;
	private int hour;
	private int minute;
	private int suid;
	private int theme;
	private int subTheme;
	private int dupFlag;
    private long epoch;
    private long series;

    @JsonCreator
    public Record(@JsonProperty("hour") int hour, 
    		@JsonProperty("minute") int minute,
    		@JsonProperty("seriesName") String seriesName, 
    		@JsonProperty("suid") int suid, 
    		@JsonProperty("theme") int theme,
    		@JsonProperty("subTheme") int subTheme,
    		@JsonProperty("epoch") long epoch, 
    		@JsonProperty("dupFlag") int dupFlag, 
    		@JsonProperty("weekDay") String weekDay, 
    		@JsonProperty("series") long series) {
    	
        this.hour = hour;
        this.minute = minute;
        this.seriesName = seriesName;
        this.suid = suid;
        this.theme = theme;
        this.subTheme = subTheme;
        this.epoch = epoch;
        this.dupFlag = dupFlag;
        this.weekDay = weekDay;
        this.series = series;
    }
    

	public String getRecId() {
    	return recId;
    }
    
    public int getHour() {
    	return hour;
    }
    
    public int getMinute() {
    	return minute;
    }

    public String getSeriesName() {
    	return seriesName;
    }
    
    public int getSuid() {
    	return suid;
    }
    
    public int getTheme() {
    	return theme;
    }
    
    public int getSubTheme() {
    	return subTheme;
    }
    public long getEpoch() {
    	return epoch;
    }
    
    public int getDupFlag() {
    	return dupFlag;
    }
    
    public String getWeekDay() {
    	return weekDay;
    }
    
    public long getSeries() {
    	return series;
    }
    
    
    
    public static void main(String[] args) {
    	RestTemplate rest = new RestTemplate();
    	ListOfRecords list = rest.getForObject("http://10.76.243.80/stb/viewHistory.pl?recId=28a0a95baa35ea0d72d265f93346456673f13496fc25", ListOfRecords.class);
    	List<Record> recs = new ArrayList<Record>();
    	recs = list.getRecords();
    	for (Record r : recs)
    		System.out.println(r.getMinute());
    }
    
}

