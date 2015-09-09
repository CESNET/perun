package cz.metacentrum.perun.core.impl;


import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;


public class PerunTransactionManager extends DataSourceTransactionManager implements ResourceTransactionManager, InitializingBean {

	private static final long serialVersionUID = 1L;

	private Auditer auditer;
	
	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition) {
		this.getAuditer().newTopLevelTransaction();
		super.doBegin(transaction, definition);
	}

	@Override
	protected void doCommit(DefaultTransactionStatus status) {
		super.doCommit(status);
		this.getAuditer().flush();
	}

	@Override
	protected void doRollback(DefaultTransactionStatus status) {
		super.doRollback(status);
		this.getAuditer().clean();
	}

	@Override
	protected void doCleanupAfterCompletion(Object transaction) {
		super.doCleanupAfterCompletion(transaction);
		this.getAuditer().clean();
	}

	public Auditer getAuditer() {
		return this.auditer;
	}

	public void setAuditer(Auditer auditer) {
		this.auditer = auditer;
	}

}
