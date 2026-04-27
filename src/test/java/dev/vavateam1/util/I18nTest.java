package dev.vavateam1.util;

import org.junit.jupiter.api.Test;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class I18nTest {

    @Test
    void setLocale_slovak_isSlovak() {
        I18n.setLocale(Locale.forLanguageTag("sk-SK"));
        assertTrue(I18n.isSlovak());
    }

    @Test
    void setLocale_english_isNotSlovak() {
        I18n.setLocale(Locale.ENGLISH);
        assertFalse(I18n.isSlovak());
    }

    @Test
    void setLocale_unsupportedLocale_fallsBackToEnglish() {
        I18n.setLocale(Locale.GERMAN);
        assertFalse(I18n.isSlovak());
        assertEquals("en", I18n.locale().getLanguage());
    }

    @Test
    void toggleLocale_fromSlovak_switchesToEnglish() {
        I18n.setLocale(Locale.forLanguageTag("sk-SK"));
        I18n.toggleLocale();
        assertFalse(I18n.isSlovak());
    }

    @Test
    void toggleLocale_fromEnglish_switchesToSlovak() {
        I18n.setLocale(Locale.ENGLISH);
        I18n.toggleLocale();
        assertTrue(I18n.isSlovak());
    }
}