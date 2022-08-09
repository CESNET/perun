package cz.metacentrum.perun.oidc;

/**
 * Class representing response from http request to UserInfoEndpoint
 *
 * @author Lucie Kureckova <luckureckova@gmail.com>
 */
public class UserInfoEndpointResponse {
	private String issuer;
	private String sub;

	public UserInfoEndpointResponse(String issuer, String sub) {
		this.issuer = issuer;
		this.sub = sub;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public String getSub() {
		return sub;
	}

	public void setSub(String sub) {
		this.sub = sub;
	}
}
