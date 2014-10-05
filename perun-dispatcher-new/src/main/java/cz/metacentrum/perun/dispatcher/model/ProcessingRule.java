package cz.metacentrum.perun.dispatcher.model;

/**
 * 
 * @author Michal Karm Babacek
 *         JavaDoc coming soon...
 * 
 */
public class ProcessingRule {

    private int id;
    private String rule;

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getRule() {
        return rule;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + ((rule == null) ? 0 : rule.hashCode());
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
        ProcessingRule other = (ProcessingRule) obj;
        if (id != other.id)
            return false;
        if (rule == null) {
            if (other.rule != null)
                return false;
        } else if (!rule.equals(other.rule))
            return false;
        return true;
    }

}
