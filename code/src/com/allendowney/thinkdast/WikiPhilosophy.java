package com.allendowney.thinkdast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class WikiPhilosophy {

    final static List<String> visited = new ArrayList<String>();
    final static WikiFetcher wf = new WikiFetcher();

    /**
     * Tests a conjecture about Wikipedia and Philosophy.
     *
     * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
     *
     * 1. Clicking on the first non-parenthesized, non-italicized link
     * 2. Ignoring external links, links to the current page, or red links
     * 3. Stopping when reaching "Philosophy", a page with no links or a page
     *    that does not exist, or when a loop occurs
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String destination = "https://en.wikipedia.org/wiki/Philosophy";
        String source = "https://en.wikipedia.org/wiki/Java_(programming_language)";
        // String source = "https://en.wikipedia.org/wiki/Linus_Torvalds"; // page already seen
        // String source = "https://en.wikipedia.org/wiki/6_Journey";      // no links

        testConjecture(destination, source, 20);
    }

    /**
     * Starts from given URL and follows first link until it finds the destination or exceeds the limit.
     *
     * @param destination
     * @param source
     * @throws IOException
     */
    public static void testConjecture(String destination, String source, int limit) throws IOException {
        // TODO: FILL THIS IN!

        for (int i = 0; i < limit; i++) {
            // 1. download and parse the document
            try {
                String url = source;
                do {
                    if (visited.contains(url)) { // Failure: already seen
                        System.err.println("Failure: Page already seen");
                        System.exit(-1);
                    }

                    System.out.println("Fetching " + url);
                    Elements paragraphs = wf.fetchWikipedia(url);
                    visited.add(url);
                    url = processParagraphs(paragraphs);
                    System.out.println(url);

                    if (url == null) {           // Failure: no links (orphan articles)
                        System.err.println("Failure: Page has no links");
                        System.exit(-1);
                    } else {
                        url = "https://en.wikipedia.org" + url;
                    }
                    System.out.println();
                } while (!url.equals(destination));
                System.out.println("Eureka!");
                break;
            } catch (Exception IOException) {   // Error: wrong link
                System.err.println("Error: Can't fetch such link: " + IOException);
                System.exit(-1);
            }
        }
    }

    private static String processParagraphs(Elements paragraphs) {
        for (Element paragraph : paragraphs) {
            // 2. traverse the DOM tree for each paragraph
            int count = 0;
            Iterable<Node> iter = new WikiNodeIterable(paragraph);
            for (Node node : iter) {
                if (node instanceof TextNode) { // keep track of opening and closing parentheses
                    // System.out.println(node);
                    count += countParentheses(node.toString());
                    // System.out.println("count: " + count);
                }
                if (node instanceof Element) {
                    // System.out.println(node.nodeName());
                    Element element = (Element) node;
                    if (element.tagName().equals("a")) { // find the first valid link
                        if (isValid(element, count)) {
                            System.out.println("**" + element.text() + "**");
                            return element.attr("href"); // found
                        }
                    }
                }
            }
        }
        return null;                                     // not found
    }

    private static int countParentheses(String s) {
        int count = 0;
        for (char c : s.toCharArray()) {
            if (c == '(') {
                count++;
            } else if (c == ')') {
                count--;
            }
        }
        return count;
    }

    private static boolean isValid(Element link, int parentheses) {
        // not be in parentheses
        if (parentheses != 0) {
            return false;
        }
        // not be in italics
        Element parent = link.parent();
        boolean italicFlag = parent.tagName().equals("i") ||
                             parent.tagName().equals("em");
        if (italicFlag) {
            return false;
        }
        // skip external, red and current page link
        String href = link.attr("href");
        boolean ohterLinkFlag = link.hasClass("external") ||
                                link.hasClass("new") ||
                                href.startsWith("#");
        if (ohterLinkFlag) {
            return false;
        }
        // skip link text starting with an uppercase
        boolean upperFlag = false;
        String linkText = link.text();
        if (!linkText.isEmpty()) {
            char firstLetter = linkText.charAt(0);
            upperFlag = Character.isLetter(firstLetter) && 
                        Character.isUpperCase(firstLetter);
            if (upperFlag) {
                return false;
            }
        }

        return true;
    }
    
}