package br.com.challenge.engine;

import br.com.challenge.connection.ConnectionFactory;
import br.com.challenge.web.crawler.GenerateJson;
import br.com.challenge.web.crawler.WebCrawler;

/**
 * Engine that initiates the process of Words collection, internalizes in the
 * database and generates the json file to be consumed by the js library.
 * 
 * @author Publio B. Guirelli
 * @version 1.0 03/2019
 * @since 1.0 03/2019
 */
public class Execute {

	/**
	 * The idea would be to generate a jar to do this execution by Windows
	 * scheduler.
	 * 
	 */
	public static void main(String[] args) {
		WebCrawler.startWebCrawler();
		GenerateJson.generateJson();
		ConnectionFactory.closeConnection();
	}

}
