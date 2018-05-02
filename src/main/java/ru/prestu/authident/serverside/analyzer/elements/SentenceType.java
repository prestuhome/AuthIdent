package ru.prestu.authident.serverside.analyzer.elements;

public enum SentenceType {

    INTERROGATIVE,
    EXCLAMATORY,
    AFFIRMATIVE,
    UNDEFINED;

    public static SentenceType getByMark(char mark) {
        switch (mark) {
            case '!':
                return EXCLAMATORY;
            case '?':
                return INTERROGATIVE;
            case '.':
                return AFFIRMATIVE;
            default:
                return UNDEFINED;
        }
    }

}
