package cz.metacentrum.perun.dispatcher.model;

import java.util.Date;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
public class Event {
	private String header;
	private String data;
	private long timeStamp;

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getHeader() {
		return header;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + (int) (timeStamp ^ (timeStamp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Event other = (Event) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (timeStamp != other.timeStamp)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[" + (new Date(timeStamp)).toString() + "][" + header + "]["
				+ data + "]";
	}

}
