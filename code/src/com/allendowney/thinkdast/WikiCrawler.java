package com.allendowney.thinkdast;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import redis.clients.jedis.Jedis;


public class WikiCrawler {
	// keeps track of where we started
	@SuppressWarnings("unused")
	private final String source;

	// the index where the results go
	private JedisIndex index;

	// queue of URLs to be indexed
	private Queue<String> queue = new LinkedList<String>();

	// fetcher used to get pages from Wikipedia
	final static WikiFetcher wf = new WikiFetcher();

	/**
	 * Constructor.
	 *
	 * @param source
	 * @param index
	 */
	public WikiCrawler(String source, JedisIndex index) {
		this.source = source;
		this.index = index;
		queue.offer(source);
	}

	/**
	 * Returns the number of URLs in the queue.
	 *
	 * @return
	 */
	public int queueSize() {
		return queue.size();
	}

	/**
	 * Gets a URL from the queue and indexes it.
	 * @param testing
	 *
	 * @return URL of page indexed.
	 * @throws IOException
	 */
	public String crawl(boolean testing) throws IOException {
		// TODO: FILL THIS IN!
		if (queue.isEmpty()) {
			return null;
		}
		// 1. remove a URL from the queue
		String url = queue.poll();

		// unless it's in testing mode: if URL already indexed, should not index it again, return null
		if (!testing && index.isIndexed(url)) {
			System.out.println("Already indexed.");
			return null;
		}

		Elements paragraphs;
		if (testing) {
			// 2. read the contents of the cached copies of the pages under /resource
			paragraphs = wf.readWikipedia(url);
		} else {
			// otherwise, fetch current content from the web
			paragraphs = wf.fetchWikipedia(url);
		}
		// 3. index pages (regardless of whether they are already indexed; when testing is true)
		index.indexPage(url, paragraphs);
		// 4. find all the internal links on the page, and add them to the queue in order
		queueInternalLinks(paragraphs);
		// printQueue();
		// 5. return the URL of the page it indexed
		return url;
	}

	/**
	 * Parses paragraphs and adds internal links to the queue.
	 * 
	 * @param paragraphs
	 */
	// NOTE: absence of access level modifier means package-level
	void queueInternalLinks(Elements paragraphs) {
        // TODO: FILL THIS IN!
		// find all internal links for each paragraph
		for (Element paragraph : paragraphs) {
			queueInternalLinks(paragraph);
		}
	}

	private void queueInternalLinks(Element paragraph) {
		Elements elts = paragraph.select("a[href]");
		for (Element elt : elts) {
			String relURL = elt.attr("href");
			if (relURL.startsWith("/wiki/")) {
				// System.out.println("text: " + elt.text());
				// System.out.println("href: " + href);
				String absURL = elt.attr("abs:href");
				queue.offer(absURL);
			}
		}
	}

	public void printQueue() {
		System.out.println(queue);
		System.out.println(queueSize());
	}

	public static void main(String[] args) throws IOException {
		// make a WikiCrawler
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis);
		String source = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		WikiCrawler wc = new WikiCrawler(source, index);
		
		// for testing purposes, load up the queue
		// Elements paragraphs = wf.fetchWikipedia(source);
		// wc.queueInternalLinks(paragraphs);
		// wc.printQueue();

		// loop until we index a new page
		String res;
		do {
			res = wc.crawl(false);

            // REMOVE THIS BREAK STATEMENT WHEN crawl() IS WORKING
            // break;
		} while (res == null); // ends when you indexed a new page (so it ends when you found the first new link)
		
		Map<String, Integer> map = index.getCounts("the");
		for (Entry<String, Integer> entry: map.entrySet()) {
			System.out.println(entry);
		}
	}
}
