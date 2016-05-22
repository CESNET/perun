package cz.metacentrum.perun.core.impl;


import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;

import javax.transaction.*;

public class PerunTestTransactionManager extends DataSourceTransactionManager implements ResourceTransactionManager, InitializingBean {

	private static final long serialVersionUID = 1L;

	private CacheManager cacheManager;

	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition) {
		this.getCacheManager().newTopLevelTransaction();
		super.doBegin(transaction, definition);
	}

	@Override
	protected void doCommit(DefaultTransactionStatus status) {
		this.getCacheManager().commit();
		super.doCommit(status);
	}

	@Override
	protected void doRollback(DefaultTransactionStatus status) {
		this.getCacheManager().rollback();
		super.doRollback(status);
	}

	@Override
	protected void doCleanupAfterCompletion(Object transaction) {
		this.getCacheManager().clean();
		super.doCleanupAfterCompletion(transaction);
	}

	public CacheManager getCacheManager() {
		return cacheManager;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
}
