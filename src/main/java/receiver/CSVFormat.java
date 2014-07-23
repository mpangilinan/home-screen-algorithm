package receiver;

import java.io.FileReader;
import java.io.Reader;
import java.io.Serializable;

public class CSVFormat extends Object implements Serializable {

	/**
	 * 
	 */
	Reader in = new FileReader("/home/pangmel/Desktop/eit_theme_subtheme_research.xls");
	Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
	for (CSVRecord record : records) {
		System.out.println(record.getThemeId());
	}
}
