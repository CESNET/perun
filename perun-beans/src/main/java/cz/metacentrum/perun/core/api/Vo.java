package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.Auditable;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.api.BeansUtils;

/**
 * Vo entity.
 */
public class Vo extends Auditable implements Comparable<Vo> {
    private String name;
    private String shortName;

    public Vo() {
    }

    public Vo(int id, String name, String shortName) {
        super(id);
        if (name == null)  throw new InternalErrorRuntimeException(new NullPointerException("name is null"));
        if (shortName == null)  throw new InternalErrorRuntimeException(new NullPointerException("shortName is null"));
        this.name = name;
        this.shortName = shortName;

    }

    @Deprecated
    public Vo(int id, String name, String shortName, String createdAt, String createdBy, String modifiedAt, String modifiedBy) {
        super(id, createdAt, createdBy, modifiedAt, modifiedBy, null, null);
        if (name == null)  throw new InternalErrorRuntimeException(new NullPointerException("name is null"));
        if (shortName == null)  throw new InternalErrorRuntimeException(new NullPointerException("shortName is null"));
        this.name = name;
        this.shortName = shortName;
    }

    public Vo(int id, String name, String shortName, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
        super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
        if (name == null)  throw new InternalErrorRuntimeException(new NullPointerException("name is null"));
        if (shortName == null)  throw new InternalErrorRuntimeException(new NullPointerException("shortName is null"));
        this.name = name;
        this.shortName = shortName;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setName(String name) {
        if (name == null)  throw new InternalErrorRuntimeException(new NullPointerException("name is null"));
        this.name = name;
    }

    public void setShortName(String shortName) {
        if (shortName == null)  throw new InternalErrorRuntimeException(new NullPointerException("shortName is null"));
        this.shortName = shortName;
    }

    @Override
    public String serializeToString() {
        return this.getClass().getSimpleName() +":[" +
                "id=<" + getId() + ">" +
                ", name=<" + (getName() == null ? "\\0" : BeansUtils.createEscaping(getName())) + ">" +
                ", shortName=<" + (getShortName() == null ? "\\0" : BeansUtils.createEscaping(getShortName())) + ">" +
                ']';
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+":[" +
                "id='" + this.getId() + '\'' +
                ", name='" + name + '\'' +
                ", shortName='" + shortName + '\'' +
                ']';
    }

    public int compareTo(Vo vo) {
        if (vo != null) {
            return this.getName().compareTo(vo.getName());
        }
        return 1;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.getId();
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 53 * hash + (this.shortName != null ? this.shortName.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Vo other = (Vo) obj;
        if (this.getId() != other.getId()) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.shortName == null) ? (other.shortName != null) : !this.shortName.equals(other.shortName)) {
            return false;
        }
        return true;
    }
}
