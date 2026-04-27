package dev.vavateam1.util;

import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import com.google.inject.Injector;

import javafx.fxml.FXMLLoader;

public final class I18n {
    private static final String BUNDLE_NAME = "i18n.messages";
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("en", "sk");

    private static Locale locale = resolveInitialLocale();
    private static ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);

    private I18n() {
    }

    public static ResourceBundle bundle() {
        return bundle;
    }

    public static Locale locale() {
        return locale;
    }

    public static void setLocale(Locale newLocale) {
        locale = normalizeLocale(newLocale);
        bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
    }

    public static void toggleLocale() {
        setLocale(isSlovak() ? Locale.ENGLISH : Locale.forLanguageTag("sk-SK"));
    }

    public static boolean isSlovak() {
        return "sk".equals(locale.getLanguage());
    }

    public static String nextLanguageCode() {
        return isSlovak() ? "EN" : "SK";
    }

    public static String t(String key, Object... args) {
        String pattern = bundle.getString(key);
        if (args.length == 0) {
            return pattern;
        }
        return new MessageFormat(pattern, locale).format(args);
    }

    public static FXMLLoader loader(URL resource, Injector injector) {
        FXMLLoader loader = new FXMLLoader(resource, bundle());
        loader.setControllerFactory(injector::getInstance);
        return loader;
    }

    private static Locale resolveInitialLocale() {
        String configuredLocale = System.getProperty("app.locale");
        if (configuredLocale == null || configuredLocale.isBlank()) {
            configuredLocale = System.getenv("APP_LOCALE");
        }

        if (configuredLocale != null && !configuredLocale.isBlank()) {
            return normalizeLocale(Locale.forLanguageTag(configuredLocale.replace('_', '-')));
        }

        return normalizeLocale(Locale.getDefault());
    }

    private static Locale normalizeLocale(Locale candidate) {
        if (candidate == null || !SUPPORTED_LANGUAGES.contains(candidate.getLanguage())) {
            return Locale.ENGLISH;
        }

        return switch (candidate.getLanguage()) {
            case "sk" -> Locale.forLanguageTag("sk-SK");
            default -> Locale.ENGLISH;
        };
    }
}
