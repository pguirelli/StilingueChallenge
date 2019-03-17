package br.com.challenge.web.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import br.com.challenge.model.bean.Word;
import br.com.challenge.model.dao.WordDAO;
import br.com.challenge.util.Common;

/**
 * Represents the collection of Words on the web.
 * 
 * @author Publio B. Guirelli
 * @version 1.0 03/2019
 * @since 1.0 03/2019
 */
public class WebCrawler {

	static List<String> wordList = new ArrayList<String>();
	static List<String> relationship = new ArrayList<String>();
	static String wordCollected;

	/**
	 * Browses a dictionary web page and collects Words and their relationships.
	 * 
	 */
	public static void startWebCrawler() {
		try {
			Document doc = Jsoup.connect("http://dicionariocriativo.com.br").get();
			Elements node = doc.getElementsByClass("tags");
			Elements words = node.select("a");
			wordCollected = words.attr("title");
			wordList.add(wordCollected);

			for (int i = 0; i < wordList.size(); i++) {
				relationship.clear();
				wordCollected = wordList.get(i);

				doc = Jsoup.connect("http://dicionariocriativo.com.br/" + wordCollected).get();
				node = doc.getElementsByClass("tags");
				words = node.select("a");

				for (Element word : words) {
					String wordTitle = word.attr("title");
					if (!wordCollected.equals(wordTitle)) {
						relationship.add(Common.encode(wordTitle));
					}
				}
				checkWord(relationship, wordCollected);
			}
		} catch (IOException e) {
			Logger.getLogger(WebCrawler.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	/**
	 * Evaluates whether a Word exists in the database, and otherwise persists in
	 * the database with its Word relationship.
	 * 
	 * @param relationshipList
	 *            The collection of strings related to the Word.
	 * @param word
	 *            The Word object.
	 */
	private static void checkWord(List<String> relationshipList, String word) {
		for (int i = 0; i < relationshipList.size(); i++) {
			if (!wordList.contains(relationshipList.get(i))) {
				wordList.add(relationshipList.get(i));
			}
		}

		Word wordToSave = WordDAO.get(word);
		if (wordToSave == null) {
			wordToSave = WordDAO.save(word, null);
		}
		List<Word> wordCollection = CollectWords.getCollection(relationshipList);
		WordDAO.save(wordToSave, wordCollection);
	}
}