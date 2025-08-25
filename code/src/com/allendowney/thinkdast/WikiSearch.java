package com.allendowney.thinkdast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import redis.clients.jedis.Jedis;


/**
 * Represents the results of a search query.
 *
 */
public class WikiSearch {

	// map from URLs that contain the term(s) to relevance score
	private Map<String, Double> map;

	/**
	 * Constructor.
	 *
	 * @param map
	 */
	public WikiSearch(Map<String, Double> map) {
		this.map = map;
	}

	/**
	 * Looks up the relevance of a given URL.
	 *
	 * @param url
	 * @return
	 */
	public Double getRelevance(String url) {
		Double relevance = map.get(url);
		return relevance==null ? 0: relevance;
	}

	/**
	 * Prints the contents in order of term frequency.
	 *
	 * @param
	 */
	private void print() {
		List<Entry<String, Double>> entries = sort();
		if (entries.isEmpty()) {
			System.out.println("There's no results found.");
			return;
		}

		for (Entry<String, Double> entry: entries) {
			System.out.println(entry);
		}
		System.out.println();
	}

	/**
	 * Computes the union of two search results. (A ∪ B)
	 *
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch or(WikiSearch that) {
		// TODO: FILL THIS IN!
		Map<String, Double> union = new HashMap<>(this.map);
		for (String term : that.map.keySet()) {
			// could potentially return null if the term doesn't exist: 
			// terms that might exist in one map but not the other. 
			double relevance = totalRelevance(this.getRelevance(term), that.getRelevance(term));
			union.put(term, relevance);
		}
		return new WikiSearch(union);
	}

	/**
	 * Computes the intersection of two search results. (A ∩ B)
	 *
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch and(WikiSearch that) {
		// TODO: FILL THIS IN!
		Map<String, Double> intersection = new HashMap<>();
		for (String term : this.map.keySet()) {
			if (that.map.containsKey(term)) { // only when both have the same key
				double relevance = totalRelevance(this.map.get(term), that.map.get(term));
				intersection.put(term, relevance);
			}
		}
		return new WikiSearch(intersection);
	}

	/**
	 * Computes the difference of two search results. (A - B)
	 *
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch minus(WikiSearch that) {
		// TODO: FILL THIS IN!
		Map<String, Double> diff = new HashMap<>(this.map);
		for (String term : that.map.keySet()) {
			diff.remove(term);
		}
		return new WikiSearch(diff);
	}

	/**
	 * Computes the relevance of a search with multiple terms.
	 *
	 * @param rel1: relevance score for the first search
	 * @param rel2: relevance score for the second search
	 * @return
	 */
	protected double totalRelevance(Double rel1, Double rel2) {
		// simple starting place: relevance is the sum of the term frequencies.
		return Math.sqrt(rel1 * rel2);
	}

	/**
	 * Sort the results by relevance. 
	 * (HashMap values don’t affect ordering. 
	 * To sort by values, you must use a Comparator)
	 *
	 * @return List of entries with URL and relevance.
	 */
	public List<Entry<String, Double>> sort() {
		// TODO: FILL THIS IN!
		List<Map.Entry<String, Double>> list = new ArrayList<>(map.entrySet()); // internal TimSort algorithm is optimized for arrays
		list.sort(Map.Entry.comparingByValue()); // returns a comparator that compares Map.Entry in natural order on value.
		return list;
	}


	/**
	 * Performs a search and makes a WikiSearch object.
	 *
	 * @param term
	 * @param index
	 * @return
	 */
	public static WikiSearch search(String term, JedisIndex index) {
		Map<String, Integer> map = index.getCounts(term);

		int df = getDf(map);

		Map<String, Double> mapTfDf = new HashMap<>();
		for (Entry<String, Integer> entry : map.entrySet()) {
			mapTfDf.put(entry.getKey(), (double) entry.getValue() / df);
		}
		return new WikiSearch(mapTfDf);
	}

	private static int getDf(Map<String, Integer> m) {
		int df = 0;
		for (Integer tf : m.values()) {
			df += tf;
		}
		return df;
	}

	public static void main(String[] args) throws IOException {

		// make a JedisIndex
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis);

		// search for the first term
		String term1 = "java";
		System.out.println("Query: " + term1);
		WikiSearch search1 = search(term1, index);
		search1.print();

		// search for the second term
		String term2 = "programming";
		System.out.println("Query: " + term2);
		WikiSearch search2 = search(term2, index);
		search2.print();

		// compute the intersection of the searches
		System.out.println("Query: " + term1 + " AND " + term2);
		WikiSearch intersection = search1.and(search2);
		intersection.print();
	}
}
