package biz.cosee.talks.reactiveprogramming.boring;

import java.util.Locale;

public final class ApplicationState {
    private boolean debugMode;
    private Locale language;

    public ApplicationState(boolean debugMode, Locale language) {
        this.debugMode = debugMode;
        this.language = language;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public Locale getLanguage() {
        return language;
    }

    public void setLanguage(Locale language) {
        this.language = language;
    }

    public ApplicationState shallowCopy() {
        return new ApplicationState(debugMode, language);
    }

public static ApplicationState createDefault() {
        return new ApplicationState(false, Locale.GERMAN);
    }
}
