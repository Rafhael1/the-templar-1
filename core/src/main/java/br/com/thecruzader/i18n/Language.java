package br.com.thecruzader.i18n;

import java.util.Locale;

public enum Language {
    PT("pt", Locale.forLanguageTag("pt-BR")),
    EN("en", Locale.ENGLISH);

    final String fileCode;
    final Locale locale;
    Language(String fileCode, Locale locale) {
        this.fileCode = fileCode;
        this.locale = locale;
    }
    public Language next() { return this == PT ? EN : PT; }
}
