package br.com.challenge.web.crawler;

import java.util.ArrayList;
import java.util.List;

import br.com.challenge.model.bean.Word;
import br.com.challenge.model.dao.WordDAO;

/**
 * Represents the relationship of a Word.
 * 
 * @author Publio B. Guirelli
 * @version 1.0 03/2019
 * @since 1.0 03/2019
 */
public class CollectWords {

	/**
	 * Evaluates the collection of strings and returns as a collection of Words.
	 * 
	 * @param wordsStringCollection
	 *            The string list.
	 * 
	 * @return The Words list.
	 */
	public static List<Word> getCollection(List<String> wordsStringCollection) {
		List<Word> wordCollection = new ArrayList<Word>();

		for (String word : wordsStringCollection) {
			Word newWord = WordDAO.get(word);
			if (newWord != null) {
				wordCollection.add(newWord);
			} else {
				wordCollection.add(WordDAO.save(word, null));
			}
		}

		return wordCollection;
	}

}
