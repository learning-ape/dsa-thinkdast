package com.allendowney.thinkdast;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class TestCrawler {
    public static void main(String[] args) throws IOException {
        String source = "https://en.wikipedia.org/wiki/Java_(programming_language)";
        // String url = "https://en.wikipedia.org/wiki/Example";
        // String url = "https://en.wikipedia.org/wiki/Linus_Torvalds";
        String destination = "https://en.wikipedia.org/wiki/Philosophy";

        Connection conn = Jsoup.connect(source);   // connection to web server
        Document doc = conn.get();              // returns DOM
        
        Element content = doc.getElementById("mw-content-text");
        Elements paragraphs = content.select("p");
        // System.out.println(paragraphs);

        Element firstPara = paragraphs.get(1);
        // System.out.println(firstPara);

        System.out.println(findFirstValidLink(firstPara));
        String firstLink = "https://en.wikipedia.org" + findFirstValidLink(firstPara);
        System.out.println(firstLink);

        WikiFetcher wf = new WikiFetcher();
        while (!firstLink.equals(destination)) {
            paragraphs = wf.fetchWikipedia(firstLink);
            for (Element paragraph : paragraphs) {
                firstLink = findFirstValidLink(paragraph);
                if (firstLink != null) {
                    firstLink = "https://en.wikipedia.org" + firstLink;
                    System.out.println(firstLink);
                    break;
                }
            }
        }
    }

    public static String findFirstValidLink(Element paragraph) {
        int count = 0;
        Iterable<Node> iter = new WikiNodeIterable(paragraph);
        for (Node node : iter) {
            if (node instanceof TextNode) { // keep track of opening and closing parentheses
                System.out.println(node);
                count += countParentheses(node.toString());
                System.out.println("count: " + count);
            }
            if (node instanceof Element) {
                // System.out.println(node.nodeName());
                Element element = (Element) node;
                if (element.tagName().equals("a")) {    // find the first valid link
                    System.out.println(element);
                    
                    if (count != 0) {
                        continue;
                    }
                    
                    Element parent = element.parent();
                    boolean italicFlag = parent.tagName().equals("i") ||
                                         parent.tagName().equals("em");
                    if (italicFlag) {
                        continue;
                    }
                    
                    String href = element.attr("href");
                    boolean ohterLinkFlag = element.hasClass("external") ||
                                            element.hasClass("new")      ||
                                            href.startsWith("#");
                    if (ohterLinkFlag) {
                        continue;
                    }
                    
                    String linkText = element.text();
                    boolean upperFlag = Character.isUpperCase(linkText.charAt(0));
                    if (upperFlag) {
                        continue;
                    }

                    // System.out.println(element);
                    System.out.println("**" + linkText + "**");
                    return href;
                }
            }
        }
        return null;
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
                                link.hasClass("new")      ||
                                href.startsWith("#");
        if (ohterLinkFlag) {
            return false;
        }
        // skip link text starting with an uppercase
        String linkText = link.text();
        char firstLetter = linkText.charAt(0);
        boolean upperFlag = Character.isLetter(firstLetter) && 
                            Character.isUpperCase(firstLetter);
        if (upperFlag) {
            return false;
        }
        
        return true;
    }
}
