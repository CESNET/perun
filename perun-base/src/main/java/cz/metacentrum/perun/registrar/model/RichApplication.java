package cz.metacentrum.perun.registrar.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Application including form item data
 *
 * @author David Flor <493294@mail.muni.cz>
 */
public class RichApplication extends Application {


  private List<ApplicationFormItemData> formData = new ArrayList<>();

  public RichApplication() {
  }

  public RichApplication(Application application) {
    super(application.getId(), application.getVo(), application.getGroup(), application.getType(),
        application.getFedInfo(), application.getState(), application.getExtSourceName(),
        application.getExtSourceType(), application.getExtSourceLoa(), application.getUser());
    this.setCreatedBy(application.getCreatedBy());
    this.setCreatedAt(application.getCreatedAt());
    this.setCreatedByUid(application.getCreatedByUid());
    this.setModifiedAt(application.getModifiedAt());
    this.setModifiedBy(application.getModifiedBy());
    this.setModifiedByUid(application.getModifiedByUid());
  }

  public RichApplication(Application application, List<ApplicationFormItemData> formData) {
    this(application);
    this.formData = formData;
  }

  public RichApplication(Application application, ApplicationFormItemData form) {
    this(application);
    this.formData.add(form);
  }

  public void addData(ApplicationFormItemData data) {
    if (data == null) {
      throw new NullPointerException("Application form data cannot be null");
    }
    this.formData.add(data);
  }

  public List<ApplicationFormItemData> getFormData() {
    return formData;
  }

  public void setFormData(List<ApplicationFormItemData> formData) {
    this.formData = formData;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + ":[" + "id='" + getId() + '\'' + ", vo='" + getVo() + '\'' + ", group='" +
           getGroup() + '\'' + ", fedInfo='" + getFedInfo() + '\'' + ", type='" + getType().toString() + '\'' +
           ", state='" + getState().toString() + '\'' + ", extSourceName='" + getExtSourceName() + '\'' +
           ", extSourceType='" + getExtSourceType() + '\'' + ", extSourceLoa='" + getExtSourceLoa() + '\'' +
           ", user='" + getUser() + '\'' + ", created_at='" + getCreatedAt() + '\'' + ", created_by='" +
           getCreatedBy() + '\'' + ", modified_at='" + getModifiedAt() + '\'' + ", modified_by='" + getModifiedBy() +
           '\'' + ", formData='" + getFormData() + '\'' + ']';
  }

}
