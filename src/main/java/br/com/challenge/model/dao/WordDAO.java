package br.com.challenge.model.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import br.com.challenge.connection.ConnectionFactory;
import br.com.challenge.model.bean.Word;

/**
 * Represents the DAO class to centralize the persistence operations of the Word
 * entity.
 * 
 * @author Publio B. Guirelli
 * @version 1.0 03/2019
 * @since 1.0 03/2019
 */
public class WordDAO {

	/**
	 * Save a new Word in database.
	 * 
	 * @param description
	 *            The Word's description.
	 * @param collectionWord
	 *            Collection of Words related to the Word being added.
	 * 
	 * @return The Word saved.
	 */
	public static Word save(String description, List<Word> collectionWord) {
		EntityManager em = new ConnectionFactory().getConnection();
		Word newWord = new Word();

		try {
			newWord.setDescription(description);
			if (collectionWord != null && !collectionWord.isEmpty()) {
				newWord.setWordCollection(collectionWord);
			}
			em.getTransaction().begin();
			em.persist(newWord);
			em.getTransaction().commit();
		} catch (Exception e) {
			em.getTransaction().rollback();
			System.err.println(e);
		} finally {
			em.close();
		}

		return newWord;
	}

	/**
	 * Update a Word with a collection of words.
	 * 
	 * @param word
	 *            The word object that will be updated.
	 * @param collectionWord
	 *            Collection of words related to the word being added.
	 * 
	 * @return The word saved.
	 */
	public static Word save(Word word, List<Word> collectionWord) {
		EntityManager em = new ConnectionFactory().getConnection();

		try {
			em.getTransaction().begin();
			word.setWordCollection(collectionWord);
			em.merge(word);
			em.getTransaction().commit();
		} catch (Exception e) {
			em.getTransaction().rollback();
			System.err.println(e);
		} finally {
			em.close();
		}

		return word;
	}

	/**
	 * Get a Word by description.
	 * 
	 * @param description
	 *            The word's description.
	 * 
	 * @return The searched word.
	 */
	public static Word get(String description) {
		EntityManager em = new ConnectionFactory().getConnection();
		List<Word> words = null;

		try {
			TypedQuery<Word> query = em
					.createQuery("select w from Word w where w.description = '" + description + "'", Word.class)
					.setMaxResults(1);
			words = query.getResultList();
		} catch (Exception e) {
			System.err.println(e);
		} finally {
			em.close();
		}

		if (!words.isEmpty()) {
			return words.get(0);
		}
		return null;

	}

	/**
	 * Get all words in the database.
	 * 
	 * @return The collection of words.
	 */
	public static List<Word> getAllWords() {
		EntityManager em = new ConnectionFactory().getConnection();

		String jpql = "select p from Word p";
		TypedQuery<Word> query = em.createQuery(jpql, Word.class);
		List<Word> results = query.getResultList();

		em.close();

		return results;
	}

}
