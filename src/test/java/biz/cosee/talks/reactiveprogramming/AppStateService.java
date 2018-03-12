package biz.cosee.talks.reactiveprogramming;

import biz.cosee.talks.reactiveprogramming.boring.ApplicationState;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

import java.util.Locale;

public class AppStateService {
    private BehaviorProcessor<ApplicationState> state; // returns last emission to every new subscriber

    public AppStateService(ApplicationState initialState) {
        state = BehaviorProcessor.createDefault(initialState);
    }

    public Flowable<ApplicationState> onStateChanges() {
        return state;
    }

    public void toggleDebugMode() {
        ApplicationState nextState = state.getValue().shallowCopy();
        nextState.setDebugMode(!nextState.isDebugMode());
        state.onNext(nextState);
    }

    public void setNewLanguage(Locale locale) {
        ApplicationState nextState = state.getValue().shallowCopy();
        nextState.setLanguage(locale);
        state.onNext(nextState);
    }

    public static AppStateService createDefault() {
        return new AppStateService(ApplicationState.createDefault());
    }
}
