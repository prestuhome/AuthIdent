package ru.prestu.authident.serverside.analyzer;

import ru.prestu.authident.serverside.analyzer.elements.Paragraph;
import ru.prestu.authident.serverside.analyzer.elements.Sentence;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextAnalyzer {

    public static final List<String> sentenceSeparators = Arrays.asList(".", "!", "?", "...", "!..", "?..");

    public void analyze() throws IOException {
        List<Paragraph> paragraphs = new ArrayList<>();
        getParagraphs(paragraphs);

    }

    private void getParagraphs(List<Paragraph> paragraphs) throws IOException {
        File file = new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("idiot.txt")).getFile());

        AtomicInteger counter = new AtomicInteger();
        Files.lines(Paths.get("C:\\Users\\prest\\tmp\\Тургенев  Иван. Муму - royallib.ru.txt"), StandardCharsets.UTF_8).forEach(
                paragraph -> {
                    if (!paragraph.isEmpty() && counter.get() < 1000) {
                        //System.out.println(counter.incrementAndGet() + ": " + paragraph);
                        //if (counter.get() < 19) getSentences("То-есть, где остановлюсь?.. Да не знаю еще, право… так… И заполните строку Ф.И.О.");
                    }
                }
        );
    }

    public void processParagraph(Paragraph paragraph) {
        String paragraphText = paragraph.getText();
        paragraphText = paragraphText.replaceAll("- ", "— ")
                .replaceAll("\\?(\\.+)", "?")
                .replaceAll("!(\\.+)", "!")
                .replaceAll("\\[.+]", "");

        List<Sentence> sentences = new ArrayList<>();
        boolean isDirectOrationParagraph = false;
        int indexOfDot;
        int indexOfQuestionMark;
        int indexOfExclamationMark;
        int minIndex;
        while (!paragraphText.isEmpty()) {
            Sentence sentence = new Sentence();
            boolean isStartOfReplica = isStartOfReplica(paragraphText);
            isDirectOrationParagraph = isDirectOrationParagraph || isStartOfReplica;

            int indexOfReplicaEnd = 0;
            if (isStartOfReplica) {
                indexOfReplicaEnd = getIndexOfReplicaEnd(paragraphText);
            }
            indexOfDot = paragraphText.contains(".") ? paragraphText.indexOf(".") : Integer.MAX_VALUE;
            indexOfQuestionMark = paragraphText.contains("?") ? paragraphText.indexOf("?") : Integer.MAX_VALUE;
            indexOfExclamationMark = paragraphText.contains("!") ? paragraphText.indexOf("!") : Integer.MAX_VALUE;

             minIndex = Math.min(indexOfDot, Math.min(indexOfExclamationMark, indexOfQuestionMark));
             if (indexOfDot == indexOfExclamationMark && indexOfDot == indexOfQuestionMark) {


             } else if (minIndex == indexOfQuestionMark || minIndex == indexOfExclamationMark) {

             }

             processSentence(sentence);
        }

        paragraph.setSentences(sentences);
    }

    private void processSentence(Sentence sentence) {

    }

    public int getIndexOfReplicaEnd(String paragraphText) {
        String[] replicaEndMarks = new String[] {", — ", "! — ", "? — "};
        int minIndex = Integer.MAX_VALUE;
        for (String mark : replicaEndMarks) {
            System.out.println("---");
            if (paragraphText.contains(mark)) {
                System.out.println(mark);
            }
            minIndex = Math.min(minIndex, paragraphText.contains(mark) ? paragraphText.indexOf(mark) : Integer.MAX_VALUE);
        }
        return minIndex;
    }

    public static void main(String[] args) throws IOException {
        TextAnalyzer analyzer = new TextAnalyzer();
        Files.lines(Paths.get("C:\\Users\\prest\\tmp\\Тургенев  Иван. Муму - royallib.ru.txt"), StandardCharsets.UTF_8).forEach(
                System.out::println
        );
    }

    private String normalize(String text) {
        Charset cset = Charset.forName("UTF-8");
        ByteBuffer buf = cset.encode(text);
        byte[] b = buf.array();
        text = new String(b);
        return text.replaceAll("- ", "— ")
//                .replaceAll(﻿"- ", "— ")
                .replaceAll("\\?(\\.+)", "?")
                .replaceAll("!(\\.+)", "!")
                .replaceAll("\\[.+]", "")
                .replaceAll("…", "...")
                .replaceAll(" ", " ");
    }

    private boolean isStartOfReplica(String text) {
        return text.indexOf("— ") == 0;
    }

}
