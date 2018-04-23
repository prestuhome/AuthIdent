package ru.prestu.authident.serverside.analyzer.elements;

import java.util.List;

public class Paragraph {

    private String text;
    private List<Sentence> sentences;
    private int length;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Sentence> getSentences() {
        return sentences;
    }

    public void setSentences(List<Sentence> sentences) {
        this.sentences = sentences;
        this.length = sentences.size();
    }

    public int getLength() {
        return length;
    }
}
