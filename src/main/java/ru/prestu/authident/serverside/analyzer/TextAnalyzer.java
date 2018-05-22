package ru.prestu.authident.serverside.analyzer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.util.StringUtils;
import ru.prestu.authident.serverside.analyzer.elements.*;
import ru.stachek66.nlp.mystem.holding.Factory;
import ru.stachek66.nlp.mystem.holding.MyStem;
import ru.stachek66.nlp.mystem.holding.MyStemApplicationException;
import ru.stachek66.nlp.mystem.holding.Request;
import ru.stachek66.nlp.mystem.model.Info;
import scala.Option;
import scala.collection.JavaConversions;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextAnalyzer {

    private final List<Character> russianVowels = Arrays.asList('а', 'у', 'о', 'ы', 'и', 'э', 'я', 'ю', 'ё', 'е');
    private final List<Character> englishVowels = Arrays.asList('a', 'e', 'i', 'o', 'u');

    private final MyStem mystemAnalyzer =
            new Factory("-igd --format json")
                    .newMyStem("3.0", Option.empty()).get();

    public List<Double> analyze(String path) throws IOException {
        List<Paragraph> paragraphs = getParagraphs(path);

        AtomicReference<Double> avgParagraphLength = new AtomicReference<>((double) 0);
        AtomicReference<Double> maxParagraphLength = new AtomicReference<>((double) 0);
        AtomicReference<Double> avgSentenceLength = new AtomicReference<>((double) 0);
        AtomicReference<Double> maxSentenceLength = new AtomicReference<>((double) 0);
        AtomicReference<Double> avgWordLength = new AtomicReference<>((double) 0);
        AtomicReference<Double> maxWordLength = new AtomicReference<>((double) 0);
        AtomicReference<Double> questionMarksFrequency = new AtomicReference<>((double) 0);
        AtomicReference<Double> exclamationMarksFrequency = new AtomicReference<>((double) 0);
        AtomicReference<Double> punctuationMarksFrequency = new AtomicReference<>((double) 0);
        AtomicReference<Double> maxPunctuationMarksInSentence = new AtomicReference<>((double) 0);
        AtomicReference<Double> nounsFrequency = new AtomicReference<>((double) 0);
        AtomicReference<Double> adjectivesFrequency = new AtomicReference<>((double) 0);
        AtomicReference<Double> verbsFrequency = new AtomicReference<>((double) 0);
        AtomicReference<Double> adverbsFrequency = new AtomicReference<>((double) 0);
        AtomicReference<Double> pronounsFrequency = new AtomicReference<>((double) 0);
        AtomicReference<Double> interjectionsFrequency = new AtomicReference<>((double) 0);
        AtomicReference<Double> otherWordsFrequency = new AtomicReference<>((double) 0);
        AtomicReference<Double> foreignWordsFrequency = new AtomicReference<>((double) 0);
        AtomicReference<Double> directOrationFrequency = new AtomicReference<>((double) 0);
        AtomicReference<Double> sentencesWithNegationFrequency = new AtomicReference<>((double) 0);

        AtomicReference<Double> paragraphLengthCounter = new AtomicReference<>((double) 0);
        AtomicReference<Double> wordsCounter = new AtomicReference<>((double) 0);
        AtomicReference<Double> sentencesCounter = new AtomicReference<>((double) 0);

        paragraphs.forEach(
                paragraph -> {
                    paragraphLengthCounter.set((double) 0);
                    sentencesCounter.updateAndGet(v -> v + paragraph.getLength());
                    paragraph.getSentences().forEach(
                            sentence -> {
                                wordsCounter.updateAndGet(v -> v + sentence.getLength());
                                paragraphLengthCounter.updateAndGet(v -> v + sentence.getLength());
                                maxSentenceLength.set(Math.max(maxSentenceLength.get(), sentence.getLength()));
                                punctuationMarksFrequency.updateAndGet(v -> v + sentence.getPunctuationMarksCount());
                                maxPunctuationMarksInSentence.set(Math.max(maxPunctuationMarksInSentence.get(), sentence.getPunctuationMarksCount()));
                                if (sentence.isDirectOration()) directOrationFrequency.updateAndGet(v -> v + 1);
                                if (sentence.getText().contains("не"))
                                    sentencesWithNegationFrequency.updateAndGet(v -> v + 1);
                                switch (sentence.getType()) {
                                    case INTERROGATIVE:
                                        questionMarksFrequency.updateAndGet(v -> v + 1);
                                        break;
                                    case EXCLAMATORY:
                                        exclamationMarksFrequency.updateAndGet(v -> v + 1);
                                        break;
                                }
                                sentence.getWords().forEach(
                                        word -> {
                                            avgWordLength.updateAndGet(v -> v + word.getSyllablesCount());
                                            maxWordLength.set(Math.max(maxWordLength.get(), word.getSyllablesCount()));
                                            if (word.isForeignWord()) foreignWordsFrequency.updateAndGet(v -> v + 1);
                                            switch (word.getPartOfSpeech()) {
                                                case NOUN:
                                                    nounsFrequency.updateAndGet(v -> v + 1);
                                                    break;
                                                case VERB:
                                                    verbsFrequency.updateAndGet(v -> v + 1);
                                                    break;
                                                case ADVERB:
                                                    adverbsFrequency.updateAndGet(v -> v + 1);
                                                    break;
                                                case PRONOUN:
                                                    pronounsFrequency.updateAndGet(v -> v + 1);
                                                    break;
                                                case ADJECTIVE:
                                                    adjectivesFrequency.updateAndGet(v -> v + 1);
                                                    break;
                                                case INTERJECTION:
                                                    interjectionsFrequency.updateAndGet(v -> v + 1);
                                                    break;
                                                case OTHER:
                                                    otherWordsFrequency.updateAndGet(v -> v + 1);
                                                    break;
                                            }
                                        });
                            }
                    );
                    maxParagraphLength.set(Math.max(maxParagraphLength.get(), paragraphLengthCounter.get()));
                }
        );
        avgParagraphLength.set(wordsCounter.get() / paragraphs.size());
        avgSentenceLength.set(wordsCounter.get() / sentencesCounter.get());
        avgWordLength.set(avgWordLength.get() / wordsCounter.get());
        questionMarksFrequency.set(questionMarksFrequency.get() / sentencesCounter.get());
        exclamationMarksFrequency.set(exclamationMarksFrequency.get() / sentencesCounter.get());
        punctuationMarksFrequency.set(punctuationMarksFrequency.get() / sentencesCounter.get());
        nounsFrequency.set(nounsFrequency.get() / wordsCounter.get());
        verbsFrequency.set(verbsFrequency.get() / wordsCounter.get());
        adverbsFrequency.set(adverbsFrequency.get() / wordsCounter.get());
        pronounsFrequency.set(pronounsFrequency.get() / wordsCounter.get());
        adjectivesFrequency.set(adjectivesFrequency.get() / wordsCounter.get());
        interjectionsFrequency.set(interjectionsFrequency.get() / wordsCounter.get());
        otherWordsFrequency.set(otherWordsFrequency.get() / wordsCounter.get());
        foreignWordsFrequency.set(foreignWordsFrequency.get() / wordsCounter.get());
        directOrationFrequency.set(directOrationFrequency.get() / sentencesCounter.get());
        sentencesWithNegationFrequency.set(sentencesWithNegationFrequency.get() / sentencesCounter.get());

        return Arrays.asList(
                avgParagraphLength.get(),
                maxParagraphLength.get(),
                avgSentenceLength.get(),
                maxSentenceLength.get(),
                avgWordLength.get(),
                maxWordLength.get(),
                questionMarksFrequency.get(),
                exclamationMarksFrequency.get(),
                punctuationMarksFrequency.get(),
                maxPunctuationMarksInSentence.get(),
                nounsFrequency.get(),
                verbsFrequency.get(),
                adverbsFrequency.get(),
                pronounsFrequency.get(),
                adjectivesFrequency.get(),
                interjectionsFrequency.get(),
                otherWordsFrequency.get(),
                foreignWordsFrequency.get(),
                directOrationFrequency.get(),
                sentencesWithNegationFrequency.get()
        );
    }

    private List<Paragraph> getParagraphs(String path) throws IOException {
        List<Paragraph> paragraphs = new ArrayList<>();

        Files.lines(Paths.get(path), StandardCharsets.UTF_8).forEach(
                paragraphText -> {
                    if (paragraphText.isEmpty() || paragraphText.charAt(0) == '*') return;
                    Paragraph paragraph = getParagraph(paragraphText);
                    if (paragraph == null) return;
                    if (paragraph.getLength() == 1) {
                        Sentence sentence = paragraph.getSentences().get(0);
                        if (sentence.getType().equals(SentenceType.UNDEFINED) ||
                                (!sentence.isDirectOration() && sentence.getType().equals(SentenceType.AFFIRMATIVE) &&
                                 sentence.getLength() == 1 && sentence.getWords().get(0).isForeignWord())) return;
                    }
                    paragraphs.add(paragraph);
                }
        );
        return paragraphs;
    }

    private Paragraph getParagraph(String paragraphText) {
        Paragraph paragraph = new Paragraph();

        paragraphText = normalize(paragraphText);
        if (paragraphText.isEmpty()) return null;
        paragraph.setText(paragraphText);

        List<Sentence> sentences = new ArrayList<>();
        boolean isDirectOrationParagraph = false;
        Pattern pattern = Pattern.compile("[?!.]\\s([А-ЯA-Z]|([«\"][А-ЯA-Z]))");
        Matcher matcher;
        if (paragraphText.charAt(0) == ' ') paragraphText = paragraphText.substring(1);
        while (!paragraphText.isEmpty()) {
            boolean isStartOfReplica = isStartOfReplica(paragraphText);
            isDirectOrationParagraph = isDirectOrationParagraph || isStartOfReplica;

            matcher = pattern.matcher(paragraphText);
            String sentenceText;
            if (matcher.find()) {
                sentenceText = paragraphText.substring(0, matcher.start() + 1);
                paragraphText = paragraphText.substring(matcher.start() + 2);
            } else {
                sentenceText = paragraphText;
                paragraphText = "";
            }
            Sentence sentence = getSentence(sentenceText, isDirectOrationParagraph);
            sentences.add(sentence);
        }

        paragraph.setSentences(sentences);
        return paragraph;
    }

    private Sentence getSentence(String sentenceText, boolean isDirectOration) {
        Sentence sentence = new Sentence();
        sentence.setText(sentenceText);
        sentence.setDirectOration(isDirectOration);
        sentence.setType(SentenceType.getByMark(sentenceText.charAt(sentenceText.length() - 1)));
        sentence.setPunctuationMarksCount(
                StringUtils.countOccurrencesOf(sentenceText, ",") +
                StringUtils.countOccurrencesOf(sentenceText, ";") +
                StringUtils.countOccurrencesOf(sentenceText, ":") +
                StringUtils.countOccurrencesOf(sentenceText, "—")
        );
        sentence.setWords(getWords(sentenceText));
        return sentence;
    }

    private List<Word> getWords(String sentenceText) {
        List<Word> words = new ArrayList<>();
        Iterable<Info> result = null;
        try {
            result = JavaConversions.asJavaIterable(mystemAnalyzer
                            .analyze(Request.apply(sentenceText))
                            .info()
                            .toIterable());
        } catch (MyStemApplicationException e) {
            e.printStackTrace();
        }

        if (result != null) {
            result.forEach(
                    info -> {
                        Word word = new Word();
                        String wordText = info.initial();
                        word.setText(wordText);
                        boolean isForeignWord = isForeignWord(wordText);
                        word.setForeignWord(isForeignWord);
                        word.setSyllablesCount(countSyllables(word));
                        if (isForeignWord) word.setPartOfSpeech(PartOfSpeech.IGNORED);
                        else word.setPartOfSpeech(getPartOfSpeech(info.rawResponse()));
                        words.add(word);
                    }
            );
        }
        return words;
    }

    private PartOfSpeech getPartOfSpeech(String jsonInfo) {
        JSONObject json;
        try {
            json = (JSONObject) new JSONParser().parse(jsonInfo);
        } catch (ParseException e) {
            return PartOfSpeech.IGNORED;
        }
        JSONArray analysis = (JSONArray) json.get("analysis");
        Iterator analysisIt = analysis.iterator();
        if (analysisIt.hasNext()) {
            JSONObject analysisResult = (JSONObject) analysisIt.next();
            String gr = (String) analysisResult.get("gr");
            Pattern pattern = Pattern.compile("[A-Z]+");
            Matcher matcher = pattern.matcher(gr);
            if (matcher.find()) return PartOfSpeech.getByReduction(matcher.group());
        }
        return PartOfSpeech.IGNORED;
    }

    private int countSyllables(Word word) {
        List<Character> vowels;
        if (word.isForeignWord()) {
            vowels = englishVowels;
        } else {
            vowels = russianVowels;
        }
        return vowels.stream().mapToInt(letter -> StringUtils.countOccurrencesOf(word.getText().toLowerCase(), String.valueOf(letter))).sum();
    }

    private boolean isForeignWord(String word) {
        Pattern pattern = Pattern.compile("[A-Za-z]+");
        Matcher matcher = pattern.matcher(word);
        return matcher.matches();
    }

    private String normalize(String text) {
        Charset charset = Charset.forName("UTF-8");
        ByteBuffer buf = charset.encode(text);
        byte[] b = buf.array();
        text = new String(b);
        return text.replaceAll("- ", "— ")
                .replaceAll("-- ", "— ")
                .replaceAll("\\?(\\.+)", "?")
                .replaceAll("!(\\.+)", "!")
                .replaceAll("\\[.+]", "")
                .replaceAll("…", "...")
                .replaceAll(" ", " ")
                .replaceAll("– ", "— ")
                .replaceAll("–– ", "— ")
                .replaceAll("\uFEFF– ", "— ")
                .replaceAll("\uFEFF- ", "— ")
                .replaceAll("- ", "— ")
                .replaceAll("-- ", "— ")
                .replaceAll("\r\n|\r|\n", "")
                .replaceAll("– ", "— ")
                .replaceAll("–– ", "— ")
                .replaceAll("&#(\\d+);", "")
                .replaceAll("«", "\"")
                .replaceAll("»", "\"")
                .replaceAll("''", "\"")
                .trim();
    }

    private boolean isStartOfReplica(String text) {
        return text.indexOf("— ") == 0 || text.charAt(0) == '\"';
    }

}
