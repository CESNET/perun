package cz.metacentrum.perun.dispatcher.parser.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.dispatcher.model.Event;
import cz.metacentrum.perun.dispatcher.parser.Parser;
import cz.metacentrum.perun.dispatcher.processing.EventQueue;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
@org.springframework.stereotype.Service(value = "parserGrouper")
public class ParserGrouper implements Parser {

	private final static Logger log = LoggerFactory
			.getLogger(ParserGrouper.class);

	@Autowired
	private EventQueue eventQueue;
	private boolean running = true;

	public ParserGrouper() {
	}

	@Override
	public void run() {/*
						 * // BEGIN JUST A TEST DUMMY !!! List<String> data =
						 * new ArrayList<String>(); //String file =
						 * Thread.currentThread
						 * ().getContextClassLoader().getResource
						 * ("test-data-for-parser-grouper.txt").getFile();
						 * InputStream in =
						 * getClass().getClassLoader().getResourceAsStream
						 * ("test-data-for-parser-grouper.txt"); BufferedReader
						 * input = null;
						 * 
						 * try { //input = new BufferedReader(new FileReader(new
						 * File(file))); input = new BufferedReader(new
						 * InputStreamReader(in)); String line = null; while
						 * ((line = input.readLine()) != null) { data.add(line);
						 * }
						 * 
						 * while (running) { for (String string : data) { Event
						 * event = new Event();
						 * event.setTimeStamp(System.currentTimeMillis());
						 * String[] headerData = string.split(";");
						 * event.setHeader(headerData[0]);
						 * event.setData(headerData[1]); eventQueue.add(event);
						 * //Thread.yield(); Thread.sleep(1); } } } catch
						 * (FileNotFoundException e) {
						 * log.error(e.toString()+"\n"+e.getCause()); } catch
						 * (IOException e) {
						 * log.error(e.toString()+"\n"+e.getCause()); } catch
						 * (Exception e) {
						 * log.error(e.toString()+"\n"+e.getCause()); } // END
						 * JUST A TEST DUMMY !!!
						 */
	}

	@Override
	public void stop() {
		running = false;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	public EventQueue getEventQueue() {
		return eventQueue;
	}

	public void setEventQueue(EventQueue eventQueue) {
		this.eventQueue = eventQueue;
	}

}
