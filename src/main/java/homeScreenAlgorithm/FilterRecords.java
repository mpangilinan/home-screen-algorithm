package homeScreenAlgorithm;

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

public abstract class FilterRecords<K> {

	
	RestTemplate rest = new RestTemplate();
	Calendar today = Calendar.getInstance();
	Calendar currentHour = Calendar.getInstance();
	Calendar tomorrow = Calendar.getInstance();
	int bucketSize = 3;
	int hourField;
	int minuteField;
	
	public abstract List<Record> filterRecords(List<Record> unfilteredRecs);
	public abstract K getKey(Record r);	
	public abstract Set<Long> showInBucket(List<Record> records, String type);
	public abstract Integer get(HashMap<Long, Integer> map, long seriesComparator);
	
	/*
	 * collect() is the single method needed to run the Series.java and Theme.java
	 * algorithm.=
	 */
	public List<Set<Long>> collect(String rec_id, boolean hardCode, String type){
		setDateAndBucket(hardCode);
		List<Record> allRecords = collectRecords(rec_id);
		List<Record> filteredRecords = filterRecords(allRecords);
		System.out.println("-----CURRENT TIME BUCKET-----");
		showInBucket(filteredRecords, type);
		System.out.println("-----------------------------");
		List<Set<Long>> allLongs = allShows(filteredRecords, type);
		return allLongs;
	}
	
	
	/*
	 * If necessary, hard code the day and time
	 */	
	public void setDateAndBucket(boolean hardCode) {
		if (hardCode==true) {
			today.set(Calendar.DAY_OF_WEEK, 2);
			currentHour.set(Calendar.HOUR_OF_DAY, 9);
			currentHour.set(Calendar.MINUTE, 30);
			bucketSize = 2;
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
			System.out.println(r.getWeekDay()+", "+ r.getHour()+":"+r.getMinute()+ "; "+ r.getSeriesName() +", "+r.getSeries());
		System.out.println();
		
		return unfilteredRecords; 
	}
	
	/*
	 * makeMap() is a helper method to make a hashMap
	 */
	public HashMap<K, Integer> makeMap(List<Record> records) {
		HashMap<K, Integer> hash = new HashMap<K, Integer>();
		for (Record r: records) {
			Integer c = hash.get(getKey(r));			
			if (c==null) {
				c = 0;
			}
			hash.put(getKey(r), c+1);			
		}
		return hash;
	}
	
	
		
	/*
	 * inTimeBucket() checks if a record falls into the desired time frame.
	 */
	public boolean inTimeBucket(int recordHour, int recordMinute) {
		hourField = currentHour.get(Calendar.HOUR_OF_DAY);
		minuteField = currentHour.get(Calendar.MINUTE);

		if (!(recordHour == hourField)) {
			return false;
		}		
		return (((minuteField < 30) && (recordMinute<30)) || ((minuteField >=30) && (recordMinute>=30)));
	}
		
	
	/*
	 * hasValidEPG() checks for future instances of a series_id.
	 */
	public boolean hasValidEPG(Record record, String type) {
    	ListOfEPGRecords list= null;
    	
    	if (type.equals("series")) {
    		list = rest.getForObject("http://10.76.243.80/epg/seriesFutureShowings.pl?series="+record.getSeries(), ListOfEPGRecords.class);
    	} else if (type.equals("theme")) {
			list = rest.getForObject("http://10.76.243.80/epg/futureShowingByTheme.pl?theme="+record.getTheme(), ListOfEPGRecords.class);
		} else if (type.equals("subtheme")) {
			list = rest.getForObject("http://10.76.243.80/epg/futureShowingByTheme.pl?subTheme="+record.getSubTheme(), ListOfEPGRecords.class);
		}
    	
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
	public Set<Long> sortBucketShows(HashMap<Long, Integer> countsPerSeries) { 
		System.out.println("time "+hourField+":"+minuteField);

		System.out.println("unsorted bucket shows: " + countsPerSeries);
				
		List<Entry<Long, Integer>> list = new ArrayList<Entry<Long, Integer>>();
		HashMap<Long, Integer> sortedBucketShows = new LinkedHashMap<Long, Integer>();

		for (Entry<Long, Integer> entry : countsPerSeries.entrySet()) {
			list.add(entry);
		}
		
		Collections.sort(list, new Comparator<Map.Entry<Long, Integer>>() {
			public int compare(Map.Entry<Long, Integer> o1, Map.Entry<Long, Integer> o2) {
				return (((Map.Entry<Long, Integer>) (o2)).getValue())
						.compareTo(((Map.Entry<Long, Integer>) (o1)).getValue());
			}
		});
				
		for (Iterator<Map.Entry<Long, Integer>> it = list.iterator(); it.hasNext();) {
			Map.Entry<Long, Integer> entry = (Map.Entry<Long, Integer>) it.next();
			sortedBucketShows.put(entry.getKey(), entry.getValue());
		} 
				
		System.out.println();
		System.out.println("unlimited sorted bucket shows: "+ sortedBucketShows);
		System.out.println();
		return limitShowsInBucket(sortedBucketShows);
	}	

	/*
	 * Depending on the global variable bucketSize, limitShowsInBucket() limits the number
	 * of records returned for a given time frame to a list of bucketSize elements. 
	 */
	public Set<Long> limitShowsInBucket(HashMap<Long, Integer> sortedBucketShows) {
		Set<Long> mostWatchedSeries = new TreeSet<Long>();
		
		if (sortedBucketShows.size() > 0) {
			int j = 0;
				for (Entry<Long, Integer> l : sortedBucketShows.entrySet()) {				
					if (j < bucketSize)
						mostWatchedSeries.add(l.getKey());
					j++;
				}
		}		
		System.out.println("limited bucket shows: " + mostWatchedSeries);
				
		return mostWatchedSeries;
	}
	


	/*
	 * allShows() calls showInBucket() every thirty minutes for the rest of the day.		
	 */
	public List<Set<Long>> allShows(List<Record> record, String type) {
		List<Set<Long>> todaysShows = new ArrayList<Set<Long>>();
		Set<Long> halfHour;
		
		tomorrow.add(Calendar.DATE, 1);
		tomorrow.set(Calendar.HOUR_OF_DAY, 0);
		tomorrow.set(Calendar.MINUTE, 0);
		currentHour.add(Calendar.MINUTE, 30);
		
		while (currentHour.before(tomorrow)) {
			halfHour = showInBucket(record, type);			
			todaysShows.add(halfHour);			
			currentHour.add(Calendar.MINUTE, 30);	
		}
		
		for (Set<Long> b : todaysShows){
			System.out.println("------");
			System.out.println(b);
		}
		
		return todaysShows;
	}
	
}
