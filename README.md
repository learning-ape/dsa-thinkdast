# ThinkDataStructures

## My Solutions to given Exercises and Web Search Engine Project

1. implementation of List interface (ArrayList & Singly-LinkedList) and Map interface (HashMap & TreeMap—BST)
2. implement sort algorithms
  * general-purpose: insertion sort, merge sort
  * special-purpose: radix sort, (bounded) heap sort
3. Web Search Engine
  * Crawler (Fetcher: jsoup, BFS + Parser: Tree, DFS)
  * Indexer (Tokenizer: parser, regex, HashMap, HashSet -> Redis—presistent, Jedis client): SearchTerm -> URLs(pages) -> PageTermFreq
  * Retrieval (boolean search queries: AND, OR, NOT(DIFF)+ most relevant pages: tf-df)

## PS
### .vscode config
classpath for src, output(bin), and lib
```json
{
    "java.project.sourcePaths": [
        "code/src",
        // "solutions/src"
    ],
    "java.project.outputPath": "bin",
    "java.project.referencedLibraries": [
        "code/lib/**/*.jar",
    ],
}
```
### Jsoup `HttpStatusException` `403` Error
```java
	public Elements fetchWikipedia(String url) throws IOException {
		sleepIfNeeded();

		// download and parse the document
		Connection conn = Jsoup.connect(url);
		Document doc = conn
				.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
						+ "AppleWebKit/537.36 (KHTML, like Gecko) "
						+ "Chrome/115.0 Safari/537.36")
				.get();

		// select the content text and pull out the paragraphs.
		Element content = doc.getElementById("mw-content-text");

		// TODO: avoid selecting paragraphs from sidebars and boxouts
		Elements paras = content.select("p");
		return paras;
	}
```

* Use a realistic `userAgent`, otherwise Wikipedia may still block you.

* If you’re scraping heavily, consider using Wikipedia’s *official API* (https://en.wikipedia.org/w/api.php) instead of raw HTML scraping. It’s safer and less likely to be blocked.