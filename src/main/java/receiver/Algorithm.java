package receiver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.web.client.RestTemplate;


public class Algorithm {
	
	private Calendar today = Calendar.getInstance();
	private Calendar currentHour = Calendar.getInstance();
	private Calendar tomorrow = Calendar.getInstance();
	private int bucketSize = 5;
	private int hourField;
	private int minuteField;
	private RestTemplate rest = new RestTemplate();

	/*
	 * If necessary, hard code the day and time
	 */	
	public void setDateAndBucket(boolean hardCode) {
		if (hardCode) {
			today.set(Calendar.DAY_OF_WEEK, 2);
			currentHour.set(Calendar.HOUR_OF_DAY, 10);
			currentHour.set(Calendar.MINUTE, 30);
			bucketSize = 3;
		}
			hourField = currentHour.get(Calendar.HOUR_OF_DAY);
			minuteField = currentHour.get(Calendar.MINUTE);
		
	
		System.out.println("today is " + today.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US));
		System.out.println("time "+hourField+":"+minuteField);
		System.out.println("bucketSize = "+bucketSize);
		System.out.println();
	}
	
	/*
	 * collectRecords() collects all the records of a receiver_id beginning at the
	 * current hour until the end of the day.
	 */
	public List<Record> collectRecords(String receiver_id) {

		ListOfRecords list = rest.getForObject("http://10.76.243.80/stb/viewHistory.pl?recId="+receiver_id, ListOfRecords.class);
		List<Record> restList = list.getRecords();
		List<Record> unfilteredRecords = new ArrayList<Record>();
				
		for (Record r : restList) {
			if ((r.getWeekDay().equals(today.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US))) && (r.getHour() >= hourField)) {
				unfilteredRecords.add(r);
			} 
		} 	

		System.out.println("unfilteredRecords:");
		for (Record r : unfilteredRecords)
			System.out.println(r.getSeriesName() +" "+r.getSeries()+"; "+ r.getWeekDay()+ ", "+ r.getHour()+":"+r.getMinute());
		System.out.println();
		
		return unfilteredRecords; 
	}

	
	/*
	 * filterRecords() keeps records and HashMap entries of programs that have been 
	 * watched at least three times and makes a new HashMap.
	 * @return records 
	 */
	public List<Record> filterRecords(List<Record> unfilteredRecords) {	
		List<Record> records = new ArrayList<Record>();
		HashMap<String, Integer> count = new HashMap<String, Integer>();
		HashMap<String, Integer> frequent = new HashMap<String, Integer>();

		makeMap(unfilteredRecords, count);
		
		System.out.println("count:");
		for (Entry<String, Integer> f :count.entrySet()) 
			System.out.println(f.getKey()+", "+f.getValue());
		System.out.println();
		
		for (Entry<String, Integer> s : count.entrySet()) {
			if (s.getValue() > 2){
				frequent.put(s.getKey(), s.getValue());
			}
		}
		for (Entry<String, Integer> s : frequent.entrySet()) {						
			for (Record r : unfilteredRecords) {
				if ((s.getKey()).equals(r.getSeriesName())) {
					records.add(r);
				}
			}
		}
		
		System.out.println("frequent:");
		for (Entry<String, Integer> f :frequent.entrySet()) 
			System.out.println(f.getKey()+", "+f.getValue());
		System.out.println();
		
		return records;

	}


	/*
	 * makeMap() takes a list of records and creates a hash table where 
	 * (Key = seriesNames, Value = occurrences in records).
	 */
	public HashMap<String, Integer> makeMap(List<Record> records, HashMap<String, Integer> hash) {
		for (Record r: records) {
			Integer c = hash.get(r.getSeriesName());			
			if (c==null) {
				c = 0;
			}
			hash.put(r.getSeriesName(), c+1);			
		}
		return hash;
	}
	
	
	/*
	 * showInBucket() returns the series_ids and occurrences of the most frequently watched show in a
	 * given half-hour time frame.
	 */
	public List<Record> showInBucket(List<Record> records) {
		
		HashMap<Record, Integer> countsPerSeries = new HashMap<Record, Integer>();
		Set<String> noEPG = new TreeSet<String>();
		 
		for (Record r : records){
			if (inTimeBucket(r.getHour(), r.getMinute())) {
				if (hasValidEPG(r)) {
					Integer c = countsPerSeries.get(r.getSeries());
					if (c==null) {
						c = 0;
					}
					countsPerSeries.put(r, c+1);
				} else {
					noEPG.add(r.getSeriesName());
				}
			}
		}
			
		System.out.println();
		System.out.println("series without a valid EPG:");
		for (String e : noEPG) 
			System.out.println(e);
		System.out.println();
		
		return sortBucketShows(countsPerSeries); 
	}
	
	/*
	 * inTimeBucket() checks if a record falls into the desired time frame.
	 */
	public boolean inTimeBucket(int recordHour, int recordMinute) {

		if (!(recordHour == hourField)) {
			return false;
		}		
		return (((minuteField < 30) && (recordMinute<30)) || ((minuteField >=30) && (recordMinute>=30)));
	}
		
	/*
	 * hasValidEPG() checks for future instances of a series_id.
	 */
	public boolean hasValidEPG(Record record) {
    	ListOfEPGRecords list = rest.getForObject("http://10.76.243.80/epg/seriesFutureShowings.pl?series="+record.getSeries(), ListOfEPGRecords.class);  
		List<EPGRecord> restList = list.getEPGRecords();
				
		for (EPGRecord r : restList) {
			if ((record.getHour()==r.getHour()) && (inTimeBucket(record.getHour(), record.getMinute())) 
					&& (inTimeBucket(r.getHour(), r.getMinute())) )  {
				return true;
			} 
		}
		return false;
	}
	
	
	/*
	 * sortBucketShows() creates a list of entries<record, count> and sorts records by value
	 * in descending order, so that the most frequently watched show in a time frame
	 * is returned first and the most seldom, returned last.
	 */
	public List<Record> sortBucketShows(HashMap<Record, Integer> countsPerSeries) { 
		List<Entry<Record, Integer>> list = new ArrayList<Entry<Record, Integer>>();
		HashMap<Record, Integer> sortedBucketShows = new LinkedHashMap<Record, Integer>();

	
		Collections.sort(list, new Comparator<Map.Entry<Record, Integer>>() {
			public int compare(Map.Entry<Record, Integer> o1, Map.Entry<Record, Integer> o2) {
				return (((Map.Entry<Record, Integer>) (o2)).getValue())
						.compareTo(((Map.Entry<Record, Integer>) (o1)).getValue());
			}
		});
		
		for (Iterator<Entry<Record, Integer>> it = list.iterator(); it.hasNext();) {
			Map.Entry<Record, Integer> entry = (Map.Entry<Record, Integer>) it.next();
			sortedBucketShows.put(entry.getKey(), entry.getValue());
		} 

						
		System.out.println("time "+currentHour.get(Calendar.HOUR_OF_DAY)+":"+currentHour.get(Calendar.MINUTE));
		System.out.println("showsInBucket:");
		for (Entry<Record, Integer> e : sortedBucketShows.entrySet()) 
			
			System.out.println(e.getKey().getSeriesName());
		System.out.println();
		
		return limitShowsInBucket(countsPerSeries);
	}	
		
	/*
	 * Depending on the global variable bucketSize, limitShowsInBucket() limits the number
	 * of records returned for a given time frame to a list of bucketSize elements. 
	 */
	public List<Record> limitShowsInBucket(HashMap<Record, Integer> sortedBucketShows) {
		int j = 0;
		List<Record> mostWatchedSeries = new ArrayList<Record>();

		while (j < sortedBucketShows.size()) {
			for (Entry<Record, Integer> l : sortedBucketShows.entrySet()) {
				mostWatchedSeries.add(l.getKey());
				j++;
								
				filledBucketCapacity:
				if (mostWatchedSeries.size() == bucketSize) {
					System.out.println("most watched in bucket size="+bucketSize+":");
					break filledBucketCapacity;				
				}

			}
		}
				
		return mostWatchedSeries;
	}

	/*
	 * allShows() calls showInBucket() every thirty minutes for the rest of the day.		
	 */
	public List<List<Record>> allShows(List<Record> records) {
		List<List<Record>> todaysShows = new ArrayList<List<Record>>();
		List<Record> halfHour;
		
		tomorrow.add(Calendar.DATE, 1);
		tomorrow.set(Calendar.HOUR_OF_DAY, 0);
		tomorrow.set(Calendar.MINUTE, 0);
		currentHour.add(Calendar.MINUTE, 30);
		
		while (currentHour.before(tomorrow)) {
			halfHour = showInBucket(records);
			todaysShows.add(halfHour);			
			currentHour.add(Calendar.MINUTE, 30);			
		}
		
		for (List<Record> b : todaysShows) 			
			System.out.println(b);			
		
		return todaysShows;
	}
		
	public static void main(String[] args) {
		Algorithm testAlgorithm = new Algorithm();
		testAlgorithm.setDateAndBucket(true);
		String rec_id = "01b4a49a6e4b5b3369011315382ec048c58ec81dfb44";
		List<Record> showList = testAlgorithm.collectRecords(rec_id);
		System.out.println(showList);
		List<Record> filteredShows = testAlgorithm.filterRecords(showList);
		System.out.println("---CURRENT TIME BUCKET---");
		testAlgorithm.showInBucket(filteredShows);
		System.out.println("-------------------------");
		testAlgorithm.allShows(filteredShows);	
	}

}
