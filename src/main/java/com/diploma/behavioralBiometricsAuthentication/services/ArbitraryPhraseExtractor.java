package com.diploma.behavioralBiometricsAuthentication.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class ArbitraryPhraseExtractor {

    private final String resourceLocation = "https://www.infosecurity-magazine.com/secure-coding/";

    private List<String> links;
    private List<String> phrases;

    @PostConstruct
    private void initializeVariables() throws IOException {
        phrases = new ArrayList<>();
        Document document = Jsoup.parse(
                Jsoup.connect(resourceLocation).get()
                        .childNode(1)
                        .childNode(2)
                        .toString()
        );
        Element newsBlocks = document.getElementsByAttributeValue("class", "col-2-3 col-left").get(2);
        Elements newsSet = newsBlocks.getElementsByTag("a");
        links = newsSet.eachAttr("href");
        extractPhrases();
    }

    public String getRandomPhrase() throws IOException {
        return phrases.get(new Random().nextInt(phrases.size()))
                .replaceAll("[“”]", "\"")
                .replaceAll("\\s*—\\s*", " - ")
                .replaceAll("’", "'");
    }

    public void extractPhrases() throws IOException {
        int linkIndex = new Random().nextInt(links.size());
        Document doc = Jsoup.parse(
                Jsoup.connect(links.get( linkIndex )).get()
                        .childNode(1)
                        .childNode(2)
                        .toString()
        );
        Element content = doc.getElementsByAttributeValue("class", "article-body").get(0);
        Elements paragraphs = content.getElementsByTag("p");
        paragraphs.eachText().stream()
                .map(paragraph -> Arrays.asList(paragraph.split("\\.\\s+")))
                .forEach(phrases::addAll);
    }
}
