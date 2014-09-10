package homeScreenAlgorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

public class Main extends FilterRecords<String> {
	
	Series s = new Series();
	Theme t = new Theme();
	List<Record> filteredSubthemeRecords = new ArrayList<Record>();

	
	/*
	 * bruteAggregate() ran both Series.java and Theme.java one after another
	 * and aggregated the results of both lists into one.
	 */
	public List<Set<Long>> bruteAggregate(String rec_id, boolean hardCode){
		setDateAndBucket(hardCode);
		List<Set<Long>> series = s.collect(rec_id, hardCode, "series");
		List<Set<Long>> theme = t.collect(rec_id, hardCode, "subtheme");
		List<Set<Long>> aggregate = new ArrayList<Set<Long>>();
		
		for (int i = 0; i< series.size(); i++) {
			Set<Long> bucket = new TreeSet<Long>();
			bucket.addAll(series.get(i));
			
			if (bucket.size() < bucketSize) {
				int j = bucket.size();
				for (Long t : theme.get(i)) {
					if (j< bucketSize) {
						bucket.add(t);
						j++;
					}
				}
			}	
			aggregate.add(bucket);
		}		
		for (Set<Long> b : aggregate) 
			System.out.println(b);
		
		return aggregate;
	}

	
	/*
	 * betterAggregate() collects records by frequently watched series and 
	 * subthemes and aggregates shows for every time bucket. 
	 */
	public List<Set<Long>> betterAggregate(String rec_id, boolean hardCode) {
		List<Set<Long>> aggregate = new ArrayList<Set<Long>>();
		setDateAndBucket(hardCode);
		List<Record> allRecords = collectRecords(rec_id);
		List<Record> series = sFilterRecords(allRecords);
		List<Record> theme = tFilterRecords(allRecords);
		System.out.println("-----CURRENT TIME BUCKET-----");
		newShowInBucket(series, theme);
		System.out.println("-----------------------------");
		aggregate = allShows(series, theme);
		
		return aggregate;
	}
	
	/*
	 * newShowInBucket() collects records that belong in a 30-minute time
	 * frame using Series algorithm and Theme algorithm.
	 */
	public Set<Long> newShowInBucket(List<Record> series, List<Record> theme) {
		Set<Long> bucket = new TreeSet<Long>();
		bucket = sShowInBucket(series, "series");
		int i = bucket.size();
		if (bucket.size() < bucketSize) {
			for (Long l : tShowInBucket(theme, "subtheme")) {
				if (i < bucketSize){
					bucket.add(l);
					i++;
				}
			}
		}
		return bucket;
	}
	
	/*
	 * sFilterRecords() collects records by frequently watched series.
	 */
 	public List<Record> sFilterRecords(List<Record> unfilteredRecords) {	
		
		List<Record> records = new ArrayList<Record>();		
		HashMap<String, Integer> count = new HashMap<String, Integer>();
		HashMap<String, Integer> frequent = new HashMap<String, Integer>();
		
		count = makeMap(unfilteredRecords);
		
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
 	 * getKey() is a helper method to collect the series name of a record.
 	 */
	public String getKey(Record r) {
		return r.getSeriesName();
	}

	/*
	 * sShowInBucket() determines which frequently watched series records
	 * fall into a 30-minute time bucket.
	 */
	public Set<Long> sShowInBucket(List<Record> records, String type) {
		HashMap<Long, Integer> countsPerSeries = new HashMap<Long, Integer>();
		Set<String> noEPG = new TreeSet<String>();
		
		for (Record r : records){
			if (inTimeBucket(r.getHour(), r.getMinute())) {
				if (hasValidEPG(r, type )) {
					
					Integer c = sGet(countsPerSeries, r.getSeries());
					if (c == null) {
						c = 0;
					}
					countsPerSeries.put(r.getSeries(), c+1);
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
	 * sGet() is a helper function for the Series filter
	 */
	public Integer sGet(HashMap<Long, Integer> map, long seriesComparator) {
		for (Entry<Long, Integer> m : map.entrySet()) {
			if (seriesComparator==(m.getKey())) {
				return m.getValue();
			}
		}
		return null;
	}
	

	/*
	 * tFilterRecords() filters by subtheme.
	 */
	public List<Record> tFilterRecords(List<Record> allRecs) {
		HashMap<Integer, Integer> unfilteredSubthemes = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> filteredSubthemes = new HashMap<Integer, Integer>();
		
		HashMap<Integer, Integer> hash = new HashMap<Integer, Integer>();
		for (Record r: allRecs) {
			Integer c = hash.get(tGetKey(r));			
			if (c==null) {
				c = 0;
			}
			unfilteredSubthemes.put(tGetKey(r), c+1);			
		}		
		
		for (Entry<Integer, Integer> e : unfilteredSubthemes.entrySet()) {
			if (e.getValue() >= 7)
				filteredSubthemes.put(e.getKey(), e.getValue());
		}
		
		System.out.println("filteredSubthemes:");
		for (Entry<Integer, Integer> e: filteredSubthemes.entrySet())
			System.out.println(e.getKey()+", "+e.getValue());
		
		for (Entry<Integer, Integer> e : filteredSubthemes.entrySet()) {
			for (Record r : allRecs) {
				if (e.getKey().equals(r.getSubTheme())) {
					filteredSubthemeRecords.add(r);
				}
			}
		}
		
		
		return filteredSubthemeRecords;
	}
	
	public Integer tGetKey(Record r) {
		return r.getSubTheme();
	}
	
	public Set<Long> tShowInBucket(List<Record> records, String type) {
		HashMap<Integer, Integer> countsPerSeries = new HashMap<Integer, Integer>();

		for (Record r : records) {
			if (inTimeBucket(r.getHour(), r.getMinute())) {

				Integer c = tGet(countsPerSeries, r.getSubTheme());
				if (c==null) {
					c = 0;
				}
				countsPerSeries.put(r.getSubTheme(), c+1);
			}
			
		}

		System.out.println();

		
		return sortBucketSubthemes(countsPerSeries);
		
	}
	
	/*
	 * sortBucketSubthemes() sorted the subthemes of each 30-minute time bucket in descending order
	 * so as to find the most popular subtheme in each time bucket
	 */
	public Set<Long> sortBucketSubthemes(HashMap<Integer, Integer> countsPerSeries) { 
		System.out.println("time "+currentHour.get(Calendar.HOUR_OF_DAY)+":"+currentHour.get(Calendar.MINUTE));
		System.out.println("unsorted bucket shows: " + countsPerSeries);
				
		List<Entry<Integer, Integer>> list = new ArrayList<Entry<Integer, Integer>>();
		HashMap<Integer, Integer> sortedBucketSubthemes = new LinkedHashMap<Integer, Integer>();
		List<Integer> subthemePreferences = new ArrayList<Integer>();

		for (Entry<Integer, Integer> entry : countsPerSeries.entrySet()) {
			list.add(entry);
		}
		
		Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
			public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
				return (((Map.Entry<Integer, Integer>) (o2)).getValue())
						.compareTo(((Map.Entry<Integer, Integer>) (o1)).getValue());
			}
		});
				
		for (Iterator<Map.Entry<Integer, Integer>> it = list.iterator(); it.hasNext();) {
			Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) it.next();
			sortedBucketSubthemes.put(entry.getKey(), entry.getValue());
		} 
				
		System.out.println();
		System.out.println("unlimited sorted bucket shows: "+ sortedBucketSubthemes);
		System.out.println();
		
		for (Entry<Integer, Integer> e : sortedBucketSubthemes.entrySet()) {
			subthemePreferences.add(e.getKey());
		}
		
		
		if (subthemePreferences.isEmpty()) {
			System.out.println("subtheme prefrences: empty counts for this bucket");
			return noCountsPerSeries();
		} else {
			System.out.println("subthemePreferences:  "+subthemePreferences);
			return subthemeToSeries(subthemePreferences);
		}
		
	}
		
	public Set<Long> noCountsPerSeries(){
		Set<Long> popularShows = new TreeSet<Long>();
		ListOfWhatsHotCheck check = rest.getForObject("http://vmeasuredl.dishaccess.tv/Now/National/All-TV-Shows.xml", ListOfWhatsHotCheck.class);
		List<WhatsHotCheck> restList = check.getWhatsHotCheckItems();
		int i = 0;
		for (WhatsHotCheck w : restList) {
			if (i < bucketSize) {
				popularShows.add(w.getSeries());
				i++;
			}
		}
		
		return popularShows;
				
	}
		
		
	public Integer tGet(HashMap<Integer, Integer> map, int subthemeComparator) {
		for (Entry<Integer, Integer> m : map.entrySet()) {
			if (subthemeComparator==(m.getKey())) {
				return m.getValue();
			}
		}
		return null;
	}
	
	public Set<Long> subthemeToSeries(List<Integer> subthemeList) {
		Set<Long> subthemeSeries = new TreeSet<Long>();
		Integer[] comedy  = {32, 33, 106, 213, 226};
		Integer[] drama  = {37, 43, 61, 69, 81, 90, 105, 119, 122, 131};
		Integer[] kidsAndFamily  = {5, 26, 29, 49, 56, 59, 63, 87, 88, 
				110, 111, 138, 149, 152, 200};
		Integer[] newsAndTalkShows  = {10, 17, 22, 36, 44, 55, 75, 86, 
				92, 99, 134, 143, 166, 167, 170, 193, 197, 204};
		Integer[] reality  = {31, 35, 50, 71, 73, 80, 84, 91, 96, 102, 112, 113, 148, 153, 184, 230, 231};
		Integer[] tvMovies  = {41, 42, 174};
		ListOfWhatsHotCheck check = null;
		
		if (Arrays.asList(comedy).contains(subthemeList.get(0))) {
			check = rest.getForObject("http://vmeasuredl.dishaccess.tv/Now/National/Comedy.xml", ListOfWhatsHotCheck.class);	
		} else if (Arrays.asList(drama).contains(subthemeList.get(0))) {
			check = rest.getForObject("http://vmeasuredl.dishaccess.tv/Now/National/Drama.xml", ListOfWhatsHotCheck.class);	
		} else if (Arrays.asList(kidsAndFamily).contains(subthemeList.get(0))) {
			check = rest.getForObject("http://vmeasuredl.dishaccess.tv/Now/National/Kids-and-Family.xml", ListOfWhatsHotCheck.class);	
		} else if (Arrays.asList(newsAndTalkShows).contains(subthemeList.get(0))) {
			check = rest.getForObject("http://vmeasuredl.dishaccess.tv/Now/National/News-and-Talk-Shows.xml", ListOfWhatsHotCheck.class);	
		} else if (Arrays.asList(reality).contains(subthemeList.get(0))) {
			check = rest.getForObject("http://vmeasuredl.dishaccess.tv/Now/National/Reality.xml", ListOfWhatsHotCheck.class);	
		} else if (Arrays.asList(tvMovies).contains(subthemeList.get(0))) {
			check = rest.getForObject("http://vmeasuredl.dishaccess.tv/Now/National/TV-Movies.xml", ListOfWhatsHotCheck.class);	
		} else {
			List<EPGRecord> misc = (rest.getForObject("http://10.76.243.80/epg/futureShowingByTheme.pl?subTheme="+(subthemeList.get(0)), ListOfEPGRecords.class)).getEPGRecords();
			int i = 0;
			if (i < bucketSize) {
				for (EPGRecord r : misc) {				
					if ((inTimeBucket(r.getHour(), r.getMinute())) )  {
						
						//change this part
						for (Record f : filteredSubthemeRecords) {
							if (f.getSeriesName().equals(r.getName())) {
								subthemeSeries.add(f.getSeries());

								i++;
								
							}
						}
					}
				} 
			}
			return subthemeSeries;
		}
		
		List<WhatsHotCheck> restList = check.getWhatsHotCheckItems();
		int i = 0;
			for (WhatsHotCheck w : restList) {
				if (i<bucketSize) {
				subthemeSeries.add(w.getSeries());
				i++;
				} 
					
		}
		return subthemeSeries;
		
	}
		

	public List<Set<Long>> allShows(List<Record> series, List<Record> theme) {
		List<Set<Long>> todaysShows = new ArrayList<Set<Long>>();
		Set<Long> halfHour;
		
		tomorrow.add(Calendar.DATE, 1);
		tomorrow.set(Calendar.HOUR_OF_DAY, 0);
		tomorrow.set(Calendar.MINUTE, 0);
		currentHour.add(Calendar.MINUTE, 30);
		
		while (currentHour.before(tomorrow)) {
			halfHour = newShowInBucket(series, theme);			
			todaysShows.add(halfHour);			
			currentHour.add(Calendar.MINUTE, 30);	
		}
		
		for (Set<Long> b : todaysShows){
			System.out.println("------");
			System.out.println(b);
		}
		
		return todaysShows;
	}
	
	public static void main(String[] args) {
		Main m = new Main();
		String rec_id = "01b4a49a6e4b5b3369011315382ec048c58ec81dfb44";
		//m.aggregate(rec_id, true);
		m.betterAggregate(rec_id, true);
		//s.collect(rec_id, true, "series");
	}




}
