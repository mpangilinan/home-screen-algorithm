package homeScreenAlgorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

public class Series extends FilterRecords<String> {

	/*
	 * filterRecords() filters records by frequently watched series.
	 */
	@Override
	public List<Record> filterRecords(List<Record> unfilteredRecords) {	
		
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
	 * makeMap() takes a list of records and creates a hash table where 
	 * (Key = seriesNames, Value = occurrences in records).
	 */
	public String getKey(Record r) {
		return r.getSeriesName();
	}
	
	/*
	 * showInBucket() returns the series_ids and occurrences of the most frequently watched show in a
	 * given half-hour time frame.
	 */
	public Set<Long> showInBucket(List<Record> records, String type) {
		HashMap<Long, Integer> countsPerSeries = new HashMap<Long, Integer>();
		Set<String> noEPG = new TreeSet<String>();
		

		for (Record r : records){
			if (inTimeBucket(r.getHour(), r.getMinute())) {
				if (hasValidEPG(r, type )) {
					
					Integer c = get(countsPerSeries, r.getSeries());
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
	
	public Integer get(HashMap<Long, Integer> map, long seriesComparator) {
		for (Entry<Long, Integer> m : map.entrySet()) {
			if (seriesComparator==(m.getKey())) {
				return m.getValue();
			}
		}
		return null;
	}
	
	public static void main(String[] args) {
		Series testSeries = new Series();
		String rec_id = "01b4a49a6e4b5b3369011315382ec048c58ec81dfb44";
		testSeries.collect(rec_id, true, "series");

	}


}
