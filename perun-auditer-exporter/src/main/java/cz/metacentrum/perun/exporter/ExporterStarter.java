package cz.metacentrum.perun.exporter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.AuditerConsumer;

/**
 * Exporter which gets all the auditer messages and export them to defined output
 *
 * Author: Michal Prochazka <michalp@ics.muni.cz>
 */
public class ExporterStarter
{
	private DataSource dataSource;
	private AbstractApplicationContext springCtx;

	private boolean running;
	private OutputType outputType;
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public ExporterStarter(OutputType outputType) {
		springCtx = new ClassPathXmlApplicationContext("/perun-auditer-exporter.xml");
		this.dataSource = springCtx.getBean("dataSource", DataSource.class);

		this.outputType = outputType;
	}

	public static void main( String[] args )
	{
		// create Options object
		Options options = new Options();
		options.addOption("id", true, "exporter ID");
		OptionGroup outputOptions = new OptionGroup();
		outputOptions.addOption(new Option("stdout", "Print audit log messages to the stdout"));
		outputOptions.addOption(new Option("tcp", "sends audit log messages to the host:port over TCP"));
		outputOptions.addOption(new Option("udp", "sends audit log messages to the host:port over UDP"));
		options.addOptionGroup(outputOptions);

		CommandLineParser parser = new BasicParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);

			String exporterId = cmd.getOptionValue("id");
			if (exporterId == null) {
				System.err.println("Exporter ID must be specified.");
				System.exit(1);
			}

			OutputType outputType = cmd.hasOption("stdout") ? OutputType.STDOUT :
				cmd.hasOption("tcp") ? OutputType.TCP :
				cmd.hasOption("udp") ? OutputType.UDP :
				OutputType.STDOUT;

			ExporterStarter exporter = new ExporterStarter(outputType);
			exporter.run(exporterId);
		} catch (ParseException e) {
			help(options);
			System.exit(1);
		}
	}

	public static void help(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "Perun Auditer Exporter", options );
	}

	protected void output(String message) {
		switch (this.outputType) {
			case STDOUT:
				System.out.println(message);
				break;
			case TCP:
				System.err.println("Not implemented yet.");
			case UDP:
				System.err.println("Not implemented yet.");
				break;
		}
	}

	public void run(String exporterId) {
		//Get instance of auditerConsumer and set runnig to true
		AuditerConsumer auditerConsumer;
		try {
			auditerConsumer = new AuditerConsumer(exporterId, dataSource);
			running = true;
		} catch (Exception e) {
			throw new RuntimeException("Cannot initialize AuditerConsumer.", e);
		}

		try {
			//If running is true, then this proccess will be continously
			while (running) {

				List<String> messages = null;
				int sleepTime = 1000;
				//Waiting for new messages. If consumer failed in some internal case, waiting until it will be repaired (waiting time is increases by each attempt)
				do {
					try {
						// Get messages
						messages = auditerConsumer.getFullMessages();
					} catch (InternalErrorException ex) {
						Thread.sleep(sleepTime);
						sleepTime+=sleepTime;
					}
				} while (messages == null);

				// New messages have arrived
				Iterator<String> messagesIter = messages.iterator();
				while(messagesIter.hasNext()) {
					String message = messagesIter.next();
					messagesIter.remove();
					output(message);
				}
				//After all messages has been resolved, test interrupting of thread and if its ok, wait and go for another bulk of messages
				if (Thread.interrupted()) {
					running = false;
				} else {
					Thread.sleep(5000);
				}
			}
			//If ldapc is interrupted
		} catch (InterruptedException e) {
			Date date = new Date();
			System.err.println("Processing of last message has been interrupted at " + DATE_FORMAT.format(date) + " due to interrupting.");
			running = false;
			Thread.currentThread().interrupt();
			//If some other exception is thrown
		} catch (Exception e) {
			Date date = new Date();
			System.err.println("Processing of last message has been interrupted at " + DATE_FORMAT.format(date) + " due to exception " + e.toString());
			throw new RuntimeException(e);
		}
	}

	public enum OutputType {
		STDOUT, TCP, UDP;
	}
}



