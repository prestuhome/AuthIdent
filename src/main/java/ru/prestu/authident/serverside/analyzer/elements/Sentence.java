package ru.prestu.authident.serverside.analyzer.elements;

import java.util.List;

public class Sentence {

    private String text;
    private List<Word> words;
    private int punctuationMarksCount;
    private int length;
    private boolean isDirectOration;
    private SentenceType type;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Word> getWords() {
        return words;
    }

    public void setWords(List<Word> words) {
        this.words = words;
        this.length = words.size();
    }

    public int getLength() {
        return length;
    }

    public int getPunctuationMarksCount() {
        return punctuationMarksCount;
    }

    public void setPunctuationMarksCount(int punctuationMarksCount) {
        this.punctuationMarksCount = punctuationMarksCount;
    }

    public boolean isDirectOration() {
        return isDirectOration;
    }

    public void setDirectOration(boolean directOration) {
        isDirectOration = directOration;
    }

    public SentenceType getType() {
        return type;
    }

    public void setType(SentenceType type) {
        this.type = type;
    }
}
