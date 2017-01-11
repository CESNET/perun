package cz.metacentrum.perun.cabinet.bl;
/**
 * This class holds information for sorting and paging.
 * Property is column in db used for sorting.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 */
public class SortParam {

	private Integer page;
	private Integer size;
	private String property;
	//	private Property property;
	private boolean isAscending;

	//	public enum Property {
	//		id ("id"),
	//		FIRST_NAME ("firstName"),
	//		USER_ID("userId"),
	//		CREATED_DATE("createdDate");
	//
	//		Property(String name) {
	//			this.name = name;
	//		}
	//
	//		private String name;
	//
	//		public String toString() {
	//			return this.name;
	//		}
	//	}

	public SortParam(int page, int size, String property, boolean isAscending) {
		this.page = page;
		this.size = size;
		this.property = property;
		//this.property = Property.valueOf(property);
		this.isAscending = isAscending;
	}

	//	public Property getProperty() {
	//		return property;
	//	}

	public Integer getPage() {
		return page;
	}

	public Integer getSize() {
		return size;
	}

	public String getProperty() {
		return property;
	}


	public boolean isAscending() {
		return isAscending;
	}
}
