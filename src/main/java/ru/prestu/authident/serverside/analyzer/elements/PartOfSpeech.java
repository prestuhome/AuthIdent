package ru.prestu.authident.serverside.analyzer.elements;

public enum PartOfSpeech {

    NOUN,
    ADJECTIVE,
    ADVERB,
    INTERJECTION,
    PRONOUN,
    VERB,
    OTHER,
    IGNORED;

    public static PartOfSpeech getByReduction(String reduction) {
        switch (reduction) {
            case "S":
                return NOUN;
            case "A":
            case "ANUM":
                return ADJECTIVE;
            case "ADV":
                return ADVERB;
            case "INTJ":
                return INTERJECTION;
            case "SPRO":
            case "APRO":
            case "ADVPRO":
                return PRONOUN;
            case "V":
                return VERB;
            case "CONJ":
            case "PART":
            case "PR":
                return OTHER;
            default:
                return IGNORED;
        }
    }
}
