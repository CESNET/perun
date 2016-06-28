package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import java.util.List;

/**
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class PerunClient {

    private String id;
    private List<Scope> scopes;
    private Type type;

    /**
     * Create OAuth / OIDC client.
     *
     * @param id "Client id" in OAuth terminology
     * @param scopes Domains represent clinet rights. "Scopes" in OAuth terminology. "Claims" in OpenID terminology
     */
    public PerunClient(String id, List<Scope> scopes) {
        if (id == null) throw new InternalErrorRuntimeException(new NullPointerException("id is null"));
        if (scopes == null) throw new InternalErrorRuntimeException(new NullPointerException("scopes is null"));

        this.id = id;
        this.scopes = scopes;
        this.type = Type.OAUTH;
    }

    /**
     * Create trustful internal client. E.g. GUI or client for internal components.
     */
    public PerunClient() {
        this.type = Type.INTERNAL;
    }


    public String getId() {
        return id;
    }

    public List<Scope> getScopes() {
        return scopes;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).append(":[")
                .append("id='").append(id).append("', ")
                .append("scopes='").append(scopes).append("']").toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((scopes == null) ? 0 : scopes.hashCode());
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
        PerunClient other = (PerunClient) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (scopes == null) {
            if (other.scopes != null)
                return false;
        } else if (!scopes.equals(other.scopes))
            return false;
        return true;
    }

    public enum Scope {

        /* OAuth 2.0 custom scopes */
        ALL,

        /* OIDC custom claims */
        BONA_FIDE_STATUS,

        /* OIDC standard claims */
        SUB,
        NAME,
        GIVEN_NAME,
        FAMILY_NAME,
        MIDDLE_NAME,
        NICK_NAME,
        PREFERRED_USERNAME,
        PROFILE,
        PICTURE,
        WEBSITE,
        EMAIL,
        EMAIL_VERIFIED,
        GENDER,
        BIRTHDATE,
        ZONEINFO,
        LOCALE,
        PHONE_NUMBER,
        PHONE_NUMBER_VERIFIED,
        ADDRESS,
        UPDATED_AT;

        public String getSopeName() {
            return this.name().toLowerCase();
        }
    }

    public enum Type {
        INTERNAL, OAUTH;
    }
}
