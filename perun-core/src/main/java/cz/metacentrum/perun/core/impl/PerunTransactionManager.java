package cz.metacentrum.perun.core.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;


public class PerunTransactionManager extends DataSourceTransactionManager implements ResourceTransactionManager, InitializingBean {

	private final static Logger log = LoggerFactory.getLogger(PerunLocksUtils.class);
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

		List<Lock> locks = (List<Lock>) TransactionSynchronizationManager.getResource(PerunLocksUtils.uniqueKey.get());
		PerunLocksUtils.unlockAll(locks);

		//Because we are recycle threads, we need to unbind all resources after completion if any exist
		TransactionSynchronizationManager.unbindResourceIfPossible(PerunLocksUtils.uniqueKey.get());

		this.getAuditer().clean();
	}

	public Auditer getAuditer() {
		return this.auditer;
	}

	public void setAuditer(Auditer auditer) {
		this.auditer = auditer;
	}

}
