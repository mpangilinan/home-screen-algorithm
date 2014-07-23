package receiver;

public class CSVRecord {

	private int themeId;
	private int subthemeId;
	private String themeName;
	private String subthemeName;
	
	public CSVRecord(int themeId, String themeName, int subthemeId, String subthemeName) {
		super();
		this.themeId = themeId;
		this.themeName = themeName;
		this.subthemeId = subthemeId;
		this.subthemeName = subthemeName;
	}

	public int getThemeId() {
		return themeId;
	}
	
	public String getThemeName() {
		return themeName;
	}
	
	public int getSubthemeId() {
		return subthemeId;
	}
	
	public String getSubthemName() {
		return subthemeName;
	}
	
	public static void main(String[] args) {
		CSVRecord testCSV = new CSVRecord(10, "MISCELLANEOUS", 99, "misc");
		System.out.println(testCSV.getThemeId());
		System.out.println(testCSV.getThemeName());
		System.out.println(testCSV.getSubthemeId());
		System.out.println(testCSV.getSubthemName());
		
	}

}
