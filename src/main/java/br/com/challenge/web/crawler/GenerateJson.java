package br.com.challenge.web.crawler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mysql.cj.xdevapi.JsonArray;

import br.com.challenge.model.bean.Word;
import br.com.challenge.model.dao.WordDAO;

/**
 * Mounts the json file from the data in the database.
 * 
 * @author Publio B. Guirelli
 * @version 1.0 03/2019
 * @since 1.0 03/2019
 */
public class GenerateJson {

	private static JSONArray arrayNodes = new JSONArray();
	private static JSONArray arrayEdges = new JSONArray();
	private static int idNodeRelationship = 50000;

	/**
	 * Generate the json file.
	 * 
	 */
	public static void generateJson() {
		JSONObject json = getDataToJson();
		try (FileWriter file = new FileWriter("src/main/webapp/data.json")) {
			file.write(json.toJSONString());
			file.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the Words in the database and puts them in a json object.
	 * 
	 * @return The json object with the Words collection.
	 */
	public static JSONObject getDataToJson() {
		List<Word> listWords = WordDAO.getAllWords();

		for (int i = 0; i < listWords.size(); i++) {
			Word word = listWords.get(i);

			getJsonDataNode(word);
			getJsonEdgeNode(listWords.get(i).getWordCollection(), word);
		}

		return getNodeJson();
	}

	/**
	 * Create the data to the "Data" node of the Word in json object.
	 * 
	 * @param word
	 *            The Word object.
	 */
	@SuppressWarnings("unchecked")
	public static void getJsonDataNode(Word word) {
		List<String> collection = new ArrayList<>();
		for (int i = 0; i < word.getWordCollection().size(); i++) {
			collection.add(word.getWordCollection().get(i).getDescription());
		}

		JSONArray arrayDataNodesCytoscape = new JSONArray();
		arrayDataNodesCytoscape.add(word.getDescription());
		String firstChar = word.getDescription().substring(0, 1);

		JSONObject objDataNodes = new JSONObject();
		objDataNodes.put("id", word.getId());
		objDataNodes.put("Strength", (int) (Math.random() * 1 + 5));
		objDataNodes.put("selected", false);
		objDataNodes.put("cytoscape_alias_list", arrayDataNodesCytoscape);
		objDataNodes.put("canonicalName", word.getDescription());
		objDataNodes.put("Milk", "Relacionamentos: " + collection.toString());
		objDataNodes.put("Quality", 90);
		objDataNodes.put("Type", firstChar);
		objDataNodes.put("SUID", word.getId());
		objDataNodes.put("NodeType", "Letra Inicial");
		objDataNodes.put("name", word.getDescription());
		objDataNodes.put("shared_name", word.getDescription());

		JSONObject objPositionNodes = new JSONObject();
		objPositionNodes.put("x", (int) (Math.random() * 1000 + 109000));
		objPositionNodes.put("y", (int) (Math.random() * 1000 + 15000));

		JSONObject objUnityNodes = new JSONObject();
		objUnityNodes.put("data", objDataNodes);
		objUnityNodes.put("position", objPositionNodes);
		objUnityNodes.put("selected", false);

		arrayNodes.add(objUnityNodes);
	}

	/**
	 * Create the data to the "Edge" node of the Word in json object.
	 * 
	 * @param collection
	 *            The Words collection.
	 * @param word
	 *            The Word object.
	 */
	@SuppressWarnings("unchecked")
	public static void getJsonEdgeNode(List<Word> collection, Word word) {
		for (int j = 0; j < collection.size(); j++) {
			Word wordRel = collection.get(j);

			JSONObject objDataEdges = new JSONObject();
			objDataEdges.put("id", String.valueOf(idNodeRelationship));
			objDataEdges.put("source", word.getId());
			objDataEdges.put("target", wordRel.getId());
			objDataEdges.put("selected", false);
			objDataEdges.put("canonicalName", wordRel.getDescription());
			objDataEdges.put("SUID", idNodeRelationship);
			objDataEdges.put("name", wordRel.getDescription());
			objDataEdges.put("interaction", "cc");
			objDataEdges.put("shared_interaction", "cc");
			objDataEdges.put("shared_name", wordRel.getDescription());

			JSONObject objUnityEdges = new JSONObject();
			objUnityEdges.put("data", objDataEdges);
			objUnityEdges.put("selected", false);

			arrayEdges.add(objUnityEdges);

			idNodeRelationship++;
		}
	}

	/**
	 * Create the data to the "Data" array in json object.
	 * 
	 * @return The json object is already filled.
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject getJsonArrayData() {
		JSONObject objArrayData = new JSONObject();
		objArrayData.put("selected", true);
		objArrayData.put("__Annotations", new JsonArray());
		objArrayData.put("shared_name", "Challenge Stilingue");
		objArrayData.put("SUID", 1);
		objArrayData.put("name", "Challenge Stilingue");

		return objArrayData;
	}

	/**
	 * Create the data to the "Elements" array in json object.
	 * 
	 * @return The json object is already filled.
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject getJsonArrayElements() {
		JSONObject objArrrayElements = new JSONObject();
		objArrrayElements.put("nodes", arrayNodes);
		objArrrayElements.put("edges", arrayEdges);

		return objArrrayElements;
	}

	/**
	 * Create the data to the principal node in json object.
	 * 
	 * @return The json object is already filled.
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject getNodeJson() {
		JSONObject mainObject = new JSONObject();
		mainObject.put("format_version", "1.0");
		mainObject.put("generated_by", "cytoscape-3.2.0");
		mainObject.put("target_cytoscapejs_version", "~2.1");
		mainObject.put("data", getJsonArrayData());
		mainObject.put("elements", getJsonArrayElements());

		return mainObject;
	}
}
