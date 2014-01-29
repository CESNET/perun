package cz.metacentrum.perun.webgui.json;


/**
 * Overlaying class for a GET request
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class JsonClientRequest {

	private int id = 0;
	private String url = "";
	private String parameters = "";
	private boolean important = false;
	private int timeout = JsonClient.DEFAULT_TIMEOUT;
	
	/**
	 * Creates a new instance of request
	 * 
	 * @param requestId
	 * @param url
	 * @param parameters
	 */
	public JsonClientRequest(int requestId, String url, String parameters)
	{
		this.id = requestId;
		this.url = url;
		this.parameters = parameters;
	}

	/**
	 * Creates a new instance of request
	 * @param requestId
	 * @param url
	 * @param parameters
	 * @param important
	 * @param timeout
	 */
	public JsonClientRequest(int requestId, String url, String parameters, boolean important, int timeout)
	{
		this(requestId, url, parameters);
		this.important = important;
		this.timeout = timeout;		
	}
	

	/**
	 * Returns formatted request
	 */
	public String toString()
	{
		return url + "?" + parameters;
	}

	/**
	 * @return the requestId
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return the parameters
	 */
	public String getParameters() {
		return parameters;
	}

	/**
	 * @return the important
	 */
	public boolean isImportant() {
		return important;
	}

	/**
	 * @return the timeout
	 */
	public int getTimeout() {
		return timeout;
	}
	
	
}
