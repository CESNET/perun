package cz.metacentrum.perun.core.impl;


import cz.metacentrum.perun.core.api.BeansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import javax.transaction.*;

public class PerunTransactionManager extends DataSourceTransactionManager implements ResourceTransactionManager, InitializingBean {

	private final static Logger log = LoggerFactory.getLogger(PerunLocksUtils.class);
	private static final long serialVersionUID = 1L;
	private Auditer auditer;
	private CacheManager cacheManager;

	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition) {
		this.getAuditer().newTopLevelTransaction();
		if(BeansUtils.getCoreConfig().isCacheEnabled()) {
			this.getCacheManager().newTopLevelTransaction();
		}
		super.doBegin(transaction, definition);
	}

	@Override
	protected void doCommit(DefaultTransactionStatus status) {
		if(BeansUtils.getCoreConfig().isCacheEnabled()) {
			this.getCacheManager().commit();
		}
		super.doCommit(status);
		this.getAuditer().flush();
	}

	@Override
	protected void doRollback(DefaultTransactionStatus status) {
		if(BeansUtils.getCoreConfig().isCacheEnabled()) {
			this.getCacheManager().rollback();
		}
		super.doRollback(status);
		this.getAuditer().clean();
	}

	@Override
	protected void doCleanupAfterCompletion(Object transaction) {
		if(BeansUtils.getCoreConfig().isCacheEnabled()) {
			this.getCacheManager().clean();
		}
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

	public CacheManager getCacheManager() {
		return cacheManager;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
}
