package de.qaware.talks.reactiveprogramming;

import de.qaware.talks.reactiveprogramming.boring.ApplicationState;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

import java.util.Locale;

public class AppStateService {
    private ApplicationState lastState;
    private ReplayProcessor<ApplicationState> stateChanges; // returns last emission to every new subscriber

    public AppStateService(ApplicationState initialState) {
        lastState = initialState;
        stateChanges = ReplayProcessor.cacheLastOrDefault(initialState);
    }

    public Flux<ApplicationState> onStateChanges() {
        return stateChanges;
    }

    public void toggleDebugMode() {
        ApplicationState nextState = lastState.shallowCopy();
        nextState.setDebugMode(!nextState.isDebugMode());
        lastState = nextState;
        stateChanges.onNext(nextState);
    }

    public void setNewLanguage(Locale locale) {
        ApplicationState nextState = lastState.shallowCopy();
        nextState.setLanguage(locale);
        lastState = nextState;
        stateChanges.onNext(nextState);
    }

    public static AppStateService createDefault() {
        return new AppStateService(ApplicationState.createDefault());
    }
}
