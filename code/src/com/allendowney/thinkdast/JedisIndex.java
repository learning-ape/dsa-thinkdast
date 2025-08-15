package com.allendowney.thinkdast;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.jsoup.select.Elements;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * Represents a Redis-backed web search index.
 *
 */
public class JedisIndex {

	private Jedis jedis;

	/**
	 * Constructor.
	 *
	 * @param jedis
	 */
	public JedisIndex(Jedis jedis) {
		this.jedis = jedis;
	}

	/**
	 * Returns the Redis key for a given search term.
	 *
	 * @return Redis key.
	 */
	private String urlSetKey(String term) {
		return "URLSet:" + term;
	}

	/**
	 * Returns the Redis key for a URL's TermCounter.
	 *
	 * @return Redis key.
	 */
	private String termCounterKey(String url) {
		return "TermCounter:" + url;
	}

	/**
	 * Checks whether we have a TermCounter for a given URL.
	 *
	 * @param url
	 * @return
	 */
	public boolean isIndexed(String url) {
		String redisKey = termCounterKey(url);
		return jedis.exists(redisKey);
	}
	
	/**
	 * Adds a URL to the set associated with `term`.
	 * 
	 * @param term
	 * @param tc
	 */
	public void add(String term, TermCounter tc) {
		jedis.sadd(urlSetKey(term), tc.getLabel());
	}

	/**
	 * Looks up a search term and returns a set of URLs.
	 * 
	 * @param term
	 * @return Set of URLs.
	 */
	public Set<String> getURLs(String term) {
        // FILL THIS IN!
		return jedis.smembers(urlSetKey(term));
	}

    /**
	 * Looks up a term and returns a map from URL to count.
	 * index(term -> pages) -> count of each page
	 * 
	 * @param term
	 * @return Map from URL to count.
	 */
	public Map<String, Integer> getCounts(String term) {
        // FILL THIS IN!
		Map<String, Integer> map = new HashMap<>();

		// Set<String> urls = getURLs(term);
		// for (String url : urls) {
		// 	Integer count = getCount(url, term); // round-trip to db each time
		// 	map.put(url, count);
		// }

		/* faster version. not using getCount method */

		// 1. get pages of the term (single/atomic transaction)
		Set<String> urls = getURLs(term); // Set traversal order is retained as long as it's unchanged

		// 2. get count for each page (multiple transactions)
		// transaction to perform all lookups
		Transaction t = jedis.multi();
		for (String url : urls) {
			String redisKey = termCounterKey(url);
			t.hget(redisKey, term);
		}
		List<Object> counts = t.exec();
		
		// iterate the results and make the map
		int i = 0;
		for (String url : urls) {
			System.out.println(url);
			Integer count = Integer.valueOf((String) counts.get(i++));
			map.put(url, count);
		}

		return map;
	}

    /**
	 * Returns the number of times the given term appears at the given URL.
	 * 
	 * @param url
	 * @param term
	 * @return
	 */
	public Integer getCount(String url, String term) {
        // FILL THIS IN!
		String redisKey = termCounterKey(url);
		String count = jedis.hget(redisKey, term);
		return Integer.valueOf(count);
	}

	/**
	 * Adds a page to the index.
	 *
	 * @param url         URL of the page.
	 * @param paragraphs  Collection of elements that should be indexed.
	 */
	public void indexPage(String url, Elements paragraphs) {
		// TODO: FILL THIS IN!
		// same logic as Index.java, but store in database
		System.out.println("Indexing " + url);

		// 1. make a TermCounter (count the terms in the paragraphs)
		// JedisTermCounter tc = new JedisTermCounter(url);
		// tc.processElements(paragraphs);
		// tc.pushToRedis(jedis);
		// System.out.println("Done pushing TermCounter.");

		// 2. for each term in the TermCounter, add the TermCounter to the index
		// Map<String, String> map = tc.pullFromRedis(jedis);
		// for (Map.Entry<String, String> entry: map.entrySet()) {
		// 	String term = entry.getKey();
		// 	add(term, tc);
		// }
		// System.out.println("Done pushing URLSet.");

		/* clean && efficient (much faster) version: 
			for each term, add TermCounter && URLSet at the same time, 
			avoiding many small transactions */
		// 1. make TermCounter
		TermCounter tc = new TermCounter(url);
		tc.processElements(paragraphs);

		// 2. push it to Redis
		Transaction t = jedis.multi();
		pushTermCounterToRedis(tc, t);
		t.exec();
	}

	private void pushTermCounterToRedis(TermCounter tc, Transaction t) {
		String url = tc.getLabel();
		String hashname = termCounterKey(url);

		// if this page has already been indexed, delete the old hash (replace to new one)
		t.del(hashname);
		
		for (String term : tc.keySet()) {
			Integer count = tc.get(term);
			t.hset(hashname, term, count.toString()); // push TermCounter(the page)
			t.sadd(urlSetKey(term), url); // push the page to URLSet(index/register terms in the page) 
		}
	}

	/**
	 * Prints the contents of the index.
	 *
	 * Should be used for development and testing, not production.
	 */
	public void printIndex() {
		// loop through the search terms
		for (String term: termSet()) {
			System.out.println(term);

			// for each term, print the pages where it appears
			Set<String> urls = getURLs(term);
			for (String url: urls) {
				Integer count = getCount(url, term);
				System.out.println("    " + url + " " + count);
			}
		}
	}

	/**
	 * Returns the set of terms that have been indexed.
	 *
	 * Should be used for development and testing, not production.
	 *
	 * @return
	 */
	public Set<String> termSet() {
		Set<String> keys = urlSetKeys();
		Set<String> terms = new HashSet<String>();
		for (String key: keys) {
			String[] array = key.split(":");
			if (array.length < 2) {
				terms.add("");
			} else {
				terms.add(array[1]);
			}
		}
		return terms;
	}

	/**
	 * Returns URLSet keys for the terms that have been indexed.
	 *
	 * Should be used for development and testing, not production.
	 *
	 * @return
	 */
	public Set<String> urlSetKeys() {
		return jedis.keys("URLSet:*");
	}

	/**
	 * Returns TermCounter keys for the URLS that have been indexed.
	 *
	 * Should be used for development and testing, not production.
	 *
	 * @return
	 */
	public Set<String> termCounterKeys() {
		return jedis.keys("TermCounter:*");
	}

	/**
	 * Deletes all URLSet objects from the database.
	 *
	 * Should be used for development and testing, not production.
	 *
	 * @return
	 */
	public void deleteURLSets() {
		Set<String> keys = urlSetKeys();
		Transaction t = jedis.multi();
		for (String key: keys) {
			t.del(key);
		}
		t.exec();
	}

	/**
	 * Deletes all URLSet objects from the database.
	 *
	 * Should be used for development and testing, not production.
	 *
	 * @return
	 */
	public void deleteTermCounters() {
		Set<String> keys = termCounterKeys();
		Transaction t = jedis.multi();
		for (String key: keys) {
			t.del(key);
		}
		t.exec();
	}

	/**
	 * Deletes all keys from the database.
	 *
	 * Should be used for development and testing, not production.
	 *
	 * @return
	 */
	public void deleteAllKeys() {
		Set<String> keys = jedis.keys("*");
		Transaction t = jedis.multi();
		for (String key: keys) {
			t.del(key);
		}
		t.exec();
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis);

		//index.deleteTermCounters();
		//index.deleteURLSets();
		//index.deleteAllKeys();
		loadIndex(index);

		Map<String, Integer> map = index.getCounts("the");
		for (Entry<String, Integer> entry: map.entrySet()) {
			System.out.println(entry);
		}
	}

	/**
	 * Stores two pages in the index for testing purposes.
	 *
	 * @return
	 * @throws IOException
	 */
	private static void loadIndex(JedisIndex index) throws IOException {
		WikiFetcher wf = new WikiFetcher();

		String url = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		Elements paragraphs = wf.fetchWikipedia(url);
		index.indexPage(url, paragraphs);

		url = "https://en.wikipedia.org/wiki/Programming_language";
		paragraphs = wf.fetchWikipedia(url);
		index.indexPage(url, paragraphs);
	}
}
