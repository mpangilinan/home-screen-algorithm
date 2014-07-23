package receiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class Theme {

	List<Record> allRecords = new ArrayList<Record>();
	
	public HashMap<Integer, Integer> gatherThemes(String rec_id) {
		Algorithm algInstance = new Algorithm();
		HashMap<Integer, Integer> themeCount = new HashMap<Integer, Integer>();

		allRecords = algInstance.collectRecords(rec_id);
	
		themeCount = makeThemeMap(allRecords, "theme");
		
		System.out.println("themeCount:");
		for (Entry<Integer, Integer> e: themeCount.entrySet())
			System.out.println(e.getKey()+", "+e.getValue());

		return filterThemes(themeCount);
	}
	
	public HashMap<Integer, Integer> makeThemeMap(List<Record> allRecs, String key) {
		HashMap<Integer, Integer> sortedMap = new HashMap<Integer, Integer>();
		
		for (Record r : allRecs) {
			if (key.equals("theme")) {
				Integer c = sortedMap.get(r.getTheme());
				if (c == null) {
					c = 0;
				}
				sortedMap.put(r.getTheme(), c+1);
			} else if (key.equals("subTheme")) {
				Integer c = sortedMap.get(r.getSubTheme());
				if (c == null) {
					c = 0;
				}
				sortedMap.put(r.getSubTheme(), c+1);
			}
		}					
		return sortedMap;
	}
	
	
	public HashMap<Integer, Integer> filterThemes(HashMap<Integer,Integer> unfilteredHash) {
		HashMap<Integer, Integer> filteredThemes = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> subthemeCount = new HashMap<Integer, Integer>();
		
		for (Entry<Integer, Integer> e : unfilteredHash.entrySet()) {
			if (e.getValue() >= 10)
				filteredThemes.put(e.getKey(), e.getValue());
		}
		
		subthemeCount = makeThemeMap(allRecords, "subTheme");
		
		System.out.println("filteredThemes:");
		for (Entry<Integer, Integer> e: filteredThemes.entrySet())
			System.out.println(e.getKey()+", "+e.getValue());
		
		System.out.println("subthemes:");
		for (Entry<Integer, Integer> e: subthemeCount.entrySet())
			System.out.println(e.getKey()+", "+e.getValue());
		 
		return filteredThemes;
	}
	
	
	public static void main(String[] args) {
		Theme testTheme = new Theme();
		String rec_id = "01b4a49a6e4b5b3369011315382ec048c58ec81dfb44";
		testTheme.gatherThemes(rec_id);
	}
}
