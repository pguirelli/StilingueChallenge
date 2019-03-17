package br.com.challenge.connection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Represents a MySql connection factory.
 * 
 * @author Publio B. Guirelli
 * @version 1.0 03/2019
 * @since 1.0 03/2019
 */
public class ConnectionFactory {

	private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("PUDS");

	/**
	 * Create a new connection.
	 * 
	 * @return A connection to the persistence context.
	 */
	public EntityManager getConnection() {
		return emf.createEntityManager();
	}

	/**
	 * Close the opened connection.
	 * 
	 */
	public static void closeConnection() {
		emf.close();
	}
}
