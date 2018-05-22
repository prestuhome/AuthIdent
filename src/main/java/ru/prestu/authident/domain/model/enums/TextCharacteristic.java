package ru.prestu.authident.domain.model.enums;

public enum TextCharacteristic {

    AVG_PARAGRAPH_LENGTH("Средняя длина абзаца"),
    MAX_PARAGRAPH_LENGTH("Максимальная длина абзаца"),
    AVG_SENTENCE_LENGTH("Средняя длина предложения"),
    MAX_SENTENCE_LENGTH("Максимальная длина предложения"),
    AVG_WORD_LENGTH("Средняя длина слова"),
    MAX_WORD_LENGTH("Максимальная длина слова"),
    QUESTION_MARKS_FREQUENCY("Частота употребления вопросительных предложений"),
    EXCLAMATION_MARKS_FREQUENCY("Частота употребления восклицательных предложений"),
    PUNCTUATION_MARKS_FREQUENCY("Частота использования знаков препинания внутри предложения"),
    MAX_PUNCTUATION_MARKS_IN_SENTENCE("Максимальное количество знаков препинания внутри предложения"),
    NOUNS_FREQUENCY("Частота употребления существительных"),
    ADJECTIVES_FREQUENCY("Частота употребления прилагательных"),
    VERBS_FREQUENCY("Частота употребления глаголов"),
    ADVERBS_FREQUENCY("Частота употребления наречий"),
    PRONOUNS_FREQUENCY("Частота употребления местоимений"),
    INTERJECTIONS_FREQUENCY("Частота употребления междометий"),
    OTHER_WORDS_FREQUENCY("Частота употребления служебных частей речи"),
    FOREIGN_WORDS_FREQUENCY("Частота употребления иностранных слов"),
    DIRECT_ORATION_FREQUENCY("Частота употребления прямой речи"),
    SENTENCES_WITH_NEGATION_FREQUENCY("Частота употребления частицы \"не\"");

    private String description;

    TextCharacteristic(String description) {
        this.description = description;

    }

    public String getDescription() {
        return description;
    }
}
