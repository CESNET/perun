package cz.metacentrum.perun.dispatcher.parser;

import java.util.List;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
public interface ParserManager {

	void summonParsers();

	List<Parser> getParsers();

	void disposeParsers();
}
