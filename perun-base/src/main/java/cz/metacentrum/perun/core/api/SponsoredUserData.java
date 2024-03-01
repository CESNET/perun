package cz.metacentrum.perun.core.api;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class SponsoredUserData {

  private String guestName;
  private String firstName;
  private String lastName;
  private String titleBefore;
  private String titleAfter;
  private String namespace;
  private String email;
  private String password;
  private String login;

  public SponsoredUserData() {
  }

  public SponsoredUserData(String guestName, String firstName, String lastName, String titleBefore, String titleAfter,
                           String namespace, String email, String password, String login) {
    this.guestName = guestName;
    this.firstName = firstName;
    this.lastName = lastName;
    this.titleBefore = titleBefore;
    this.titleAfter = titleAfter;
    this.namespace = namespace;
    this.email = email;
    this.password = password;
    this.login = login;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getGuestName() {
    return guestName;
  }

  public void setGuestName(String guestName) {
    this.guestName = guestName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getTitleAfter() {
    return titleAfter;
  }

  public void setTitleAfter(String titleAfter) {
    this.titleAfter = titleAfter;
  }

  public String getTitleBefore() {
    return titleBefore;
  }

  public void setTitleBefore(String titleBefore) {
    this.titleBefore = titleBefore;
  }

  @Override
  public String toString() {
    return "SponsoredUserData{" + "guestName='" + guestName + '\'' + ", firstName='" + firstName + '\'' +
           ", lastName='" + lastName + '\'' + ", titleBefore='" + titleBefore + '\'' + ", titleAfter='" + titleAfter +
           '\'' + ", namespace='" + namespace + '\'' + ", email='" + email + '\'' +
           ", password='realPasswordIsNotLogged'" + ", login='" + login + '\'' + '}';
  }

}
