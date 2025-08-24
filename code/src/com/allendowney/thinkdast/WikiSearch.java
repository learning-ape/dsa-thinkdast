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
	private Map<String, Integer> map;

	/**
	 * Constructor.
	 *
	 * @param map
	 */
	public WikiSearch(Map<String, Integer> map) {
		this.map = map;
	}

	/**
	 * Looks up the relevance of a given URL.
	 *
	 * @param url
	 * @return
	 */
	public Integer getRelevance(String url) {
		Integer relevance = map.get(url);
		return relevance==null ? 0: relevance;
	}

	/**
	 * Prints the contents in order of term frequency.
	 *
	 * @param
	 */
	private void print() {
		List<Entry<String, Integer>> entries = sort();
		for (Entry<String, Integer> entry: entries) {
			System.out.println(entry);
		}
	}

	/**
	 * Computes the union of two search results. (A ∪ B)
	 *
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch or(WikiSearch that) {
		// TODO: FILL THIS IN!
		Map<String, Integer> unionSum = new HashMap<>(this.map);
		for (Map.Entry<String, Integer> entry : that.map.entrySet()) {
			// if key doesn't exist, insert it; 
			// otherwise(key already exist), apply merge function (in here: sum both values)
			unionSum.merge(entry.getKey(), entry.getValue(), Integer::sum); // combine two maps
		}
		return new WikiSearch(unionSum);
	}

	/**
	 * Computes the intersection of two search results. (A ∩ B)
	 *
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch and(WikiSearch that) {
		// TODO: FILL THIS IN!
		Map<String, Integer> intersectionSum = new HashMap<>();
		for (String key : this.map.keySet()) {
			if (that.map.containsKey(key)) { // only when both have the same key
				intersectionSum.put(key, this.map.get(key) + that.map.get(key));
			}
		}
		return new WikiSearch(intersectionSum);
	}

	/**
	 * Computes the difference of two search results. (A - B)
	 *
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch minus(WikiSearch that) {
		// TODO: FILL THIS IN!
		Map<String, Integer> diffSum = new HashMap<>(this.map);
		for (String key : that.map.keySet()) {
			if (diffSum.containsKey(key)) { // if both have the same key, remove it from the result
				diffSum.remove(key);
			}
		}
		return new WikiSearch(diffSum);
	}

	/**
	 * Computes the relevance of a search with multiple terms.
	 *
	 * @param rel1: relevance score for the first search
	 * @param rel2: relevance score for the second search
	 * @return
	 */
	protected int totalRelevance(Integer rel1, Integer rel2) {
		// simple starting place: relevance is the sum of the term frequencies.
		return rel1 + rel2;
	}

	/**
	 * Sort the results by relevance. 
	 * (HashMap values don’t affect ordering. 
	 * To sort by values, you must use a Comparator)
	 *
	 * @return List of entries with URL and relevance.
	 */
	public List<Entry<String, Integer>> sort() {
		// TODO: FILL THIS IN!
		List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet()); // internal TimSort algorithm is optimized for arrays
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
		return new WikiSearch(map);
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
