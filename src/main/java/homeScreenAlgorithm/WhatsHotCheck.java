package homeScreenAlgorithm;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

public class WhatsHotCheck {
	
	long series;
	int episode;
	long count;
	String title;
	
	public long getSeries() {
		return series;
	}
	
	@XmlElement
	public void setSeries(long series) {
		this.series = series;
	}

	public int getEpisode() {
		return episode;
	}
	
	@XmlElement
	public void setEpisode(int episode) {
		this.episode= episode;
	}
	
	public long getCount() {
		return count;
	}
	
	@XmlElement
	public void setCount(long count) {
		this.count = count;
	}
	
	public String getTitle() {
		return title;
	}
	
	@XmlElement
	public void setTitle(String title) {
		this.title = title;
	}
}
