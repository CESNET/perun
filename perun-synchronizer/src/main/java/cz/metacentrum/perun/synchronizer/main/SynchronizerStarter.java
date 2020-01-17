package cz.metacentrum.perun.synchronizer.main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SynchronizerStarter {
	private final static Logger log = LoggerFactory.getLogger(SynchronizerStarter.class);

	private AbstractApplicationContext springCtx;

	public SynchronizerStarter () {
		springCtx = new ClassPathXmlApplicationContext("/perun-synchronizer.xml");
	}
	
	public static void main(String[] args) {
		System.out.println("Starting Perun-Synchronizer...");

		SynchronizerStarter startMe = new SynchronizerStarter();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		log.info(dateFormat.format(date) + ": Done. Perun-Synchronizer has started.");
		System.out.println(dateFormat.format(date) + ": Done. Perun-Synchronizer has started.");

	}

}
