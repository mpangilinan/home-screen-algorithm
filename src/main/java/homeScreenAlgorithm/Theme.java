package homeScreenAlgorithm;

/*
 * Theme.java on its own returns series sorted by the most popular subthemes
 */

import org.apache.commons.csv.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
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



public class Theme extends FilterRecords<Integer> {
	
	/*
	 * filterRecords() filters records and makes hashmaps so that you only keep 
	 * records that have a count of at least 10
	 * Aggregate into one giant list of records
	 * 
	 */
	//@Override
	List<Record> filteredSubthemeRecords = new ArrayList<Record>();

	public List<Record> filterRecords(List<Record> allRecs) {
		HashMap<Integer, Integer> unfilteredSubthemes = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> filteredSubthemes = new HashMap<Integer, Integer>();
		
		unfilteredSubthemes = makeMap(allRecs);
		
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
	
	
	public Integer getKey(Record r) {
		return r.getSubTheme();
	}
	
	/*
	 * showInBucket() takes a list of records and sorts the records into 30 minute 
	 * time buckets. For every 30 minute time bucket, showInBucket will return the 
	 * series in descending order. 
	 */
	public Set<Long> showInBucket(List<Record> records, String type) {
		HashMap<Integer, Integer> countsPerSeries = new HashMap<Integer, Integer>();

		for (Record r : records) {
			if (inTimeBucket(r.getHour(), r.getMinute())) {

				Integer c = get(countsPerSeries, r.getSubTheme());
				if (c==null) {
					c = 0;
				}
				countsPerSeries.put(r.getSubTheme(), c+1);
			}
			
		}

		System.out.println();

		
		return sortBucketSubthemes(countsPerSeries);
		
	}
	
	
	public Integer get(HashMap<Integer, Integer> map, int subthemeComparator) {
		for (Entry<Integer, Integer> m : map.entrySet()) {
			if (subthemeComparator==(m.getKey())) {
				return m.getValue();
			}
		}
		return null;
	}
	
	
	/*
	 * sortBucketSubthemes() sorts the popular subthemes for each user in a hashMap
	 * in descending order;
	 * is a helping function of showInBucket()
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
	
	/*
	 * In the event there is no record of a popular subtheme, noCountsPerSeries()
	 * returns the most watche series in the US via What's Hot Check List xml files
	 */
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
	
	/*
	 * Using dish's What's Hot Check List, subthemeToSeries() matches the popular
	 * subtheme to the most viewed show in that category. The method creates a 
	 * REST template, so it can pull information into java objects.
	 */
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
	
	/*
	 * Unused method to retrieve the names of subthemes with their subtheme number 
	 * and vice versa.
	 */
	public void CSVMap() {
		Reader readerObject;
		int themeId = 0;
		int themeText = 1;
		int subthemeId = 2;
		int subthemeText = 3;
		try {
			readerObject = new FileReader("/home/pangmel/workspace/HomeScreen/src/main/resources/eit_theme_subtheme_research.csv");
			Iterable <CSVRecord> CSVrecs = CSVFormat.EXCEL.parse(readerObject);
			//System.out.println(CSVrecs.getHeaderMap());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	public static void main(String[] args) {
		Theme testTheme = new Theme();
		String rec_id = "01b4a49a6e4b5b3369011315382ec048c58ec81dfb44";
		testTheme.collect(rec_id, true, "subtheme");
//		testTheme.CSVMap();
			
	}





	
}
