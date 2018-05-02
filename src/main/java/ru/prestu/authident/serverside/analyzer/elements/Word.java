package ru.prestu.authident.serverside.analyzer.elements;

public class Word {

    private String text;
    private boolean isForeignWord;
    private PartOfSpeech partOfSpeech;
    private int syllablesCount;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isForeignWord() {
        return isForeignWord;
    }

    public void setForeignWord(boolean foreignWord) {
        isForeignWord = foreignWord;
    }

    public PartOfSpeech getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(PartOfSpeech partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public int getSyllablesCount() {
        return syllablesCount;
    }

    public void setSyllablesCount(int syllablesCount) {
        this.syllablesCount = syllablesCount;
    }
}
