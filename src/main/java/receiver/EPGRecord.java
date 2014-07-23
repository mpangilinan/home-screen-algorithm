package receiver;

import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class EPGRecord {

	private String name;
	private String description;
	private int suid;
	private int theme;
	private int subTheme;
    private long originalDate;
    private long start;
    private long episode;

    @JsonCreator
    public EPGRecord(@JsonProperty("name") String name,
    		@JsonProperty("description") String description,
    		@JsonProperty("suid") int suid,
    		@JsonProperty("theme") int theme,
    		@JsonProperty("subTheme") int subTheme,
    		@JsonProperty("originalDate") long originalDate,
    		@JsonProperty("start") long start, 
    		@JsonProperty("episode") long episode) {
    	
        this.name = name;
        this.description = description;
        this.suid = suid;
        this.theme = theme;
        this.subTheme = subTheme;
        this.originalDate = originalDate;
        this.start = start;
        this.episode = episode;
    }
    

    public String getName() {
    	return name;
    }
    
    public String getDescription() {
    	return description;
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
    
    public long getOriginalDate() {
    	return originalDate;
    }
    
    public long getStart() {    	
    	return start;
    }
    
    public long getEpisode() {
    	return episode;
    }
       
    public String getWeekDay() {
    	Calendar c = Calendar.getInstance();
    	c.setTimeInMillis(getStart()); 	
    	return c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US);
    }
    
    public int getHour() {
    	Calendar c = Calendar.getInstance();
    	c.setTimeInMillis(getStart());
    	return c.get(Calendar.HOUR_OF_DAY);
    }
    
    public int getMinute() {
    	Calendar c = Calendar.getInstance();
    	c.setTimeInMillis(getStart());
    	return c.get(Calendar.MINUTE);
    }
    
    public static void main(String[] args) {
    	RestTemplate rest = new RestTemplate();
    	ListOfEPGRecords list = rest.getForObject("http://10.76.243.80/epg/seriesFutureShowings.pl?series=134219535", ListOfEPGRecords.class);  
    	List<EPGRecord> recs = new ArrayList<EPGRecord>();
    	recs = list.getEPGRecords();
    	System.out.println(recs);
    	for (EPGRecord r : recs)
    		System.out.println(r.getStart());
    		//System.out.println(r.getStart()+" "+r.getWeekDay()+" "+r.getHour()+" "+r.getMinute());
    	
    }
}
