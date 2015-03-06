package cz.metacentrum.perun.core.api;

import java.io.Serializable;

public class Pair<L,R> implements Serializable {

	private static final long serialVersionUID = -1483403913023086683L;

	private L left;
	private R right;

	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}

	public Pair() {
	}

	public void put(L left, R right) {
		this.left = left;
		this.right = right;
	}

	public L getLeft() {
		return left;
	}

	public R getRight() {
		return right;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
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
		@SuppressWarnings("rawtypes")
		Pair other = (Pair) obj;
		if (left == null) {
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (right == null) {
			if (other.right != null)
				return false;
		} else if (!right.equals(other.right))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		return str.append(this.getClass().getSimpleName()).append(":[Left='").append(this.getLeft()).append("', Right='").append(this.getRight()).append("']").toString();
	}

}
