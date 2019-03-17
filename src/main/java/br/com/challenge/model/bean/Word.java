package br.com.challenge.model.bean;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * Represents the Word entity.
 * 
 * @author Publio B. Guirelli
 * @version 1.0 03/2019
 * @since 1.0 03/2019
 */
@Entity
@Table(name = "Word")
public class Word {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(unique = true)
	private String description;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "Relantionship", joinColumns = { @JoinColumn(name = "wordId") }, inverseJoinColumns = {
			@JoinColumn(name = "wordCollection") })
	private List<Word> wordCollection;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Word> getWordCollection() {
		return wordCollection;
	}

	public void setWordCollection(List<Word> wordCollection) {
		this.wordCollection = wordCollection;
	}
}
