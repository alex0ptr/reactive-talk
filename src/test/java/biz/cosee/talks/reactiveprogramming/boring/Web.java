package biz.cosee.talks.reactiveprogramming.boring;

import io.reactivex.Flowable;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Web {

    private static final String[] headLines = new String[] {
            "iPhone Scores 92% Loyalty Rate in Recent Survey Ahead of iPhone 8",
            "Apple is shoring up Siri for its next generation of intelligent devices",
            "Apple could release MacBook updates soon",
            "Jimmy Iovine Says Apple Music Would Have '400 Million' Listeners If It Had a Free Version Like Spotify",
            "Former Starbucks Worker Says Attending Apple's Developer Academy Was 'Opportunity of My Life'",
            "Apple Pay Officially Launches in Italy",
            "Apple Highlights Accessibility Features in New 'Designed for' Video Series"
    };
    private final static Random random = new Random();
    private final Flowable<String> appleNewsStream;
    private final Flowable<Integer> appleStocksStream;
    private final Flowable<Double> dollarEurConversionStream;


    public Web() {
        appleNewsStream = Flowable.fromCallable(() -> headLines[random.nextInt(headLines.length)])
                .compose(Web::repeatAtRandomTime)
                .share();

        appleStocksStream = Flowable.fromCallable(() -> 135 + random.nextInt(50))
                .compose(Web::repeatAtRandomTime)
                .share();

        dollarEurConversionStream = Flowable.fromCallable(() -> 0.8 + random.nextFloat() * 2)
                .compose(Web::repeatAtRandomTime)
                .share();
    }

    private static <T> Flowable<T> repeatAtRandomTime(Flowable<T> toRepeat) {
        return toRepeat
                .delaySubscription(1000 + random.nextInt(4000), TimeUnit.MILLISECONDS)
                .repeat();
    }

    public Flowable<String> appleNews() {
        return appleNewsStream;
    }

    public Flowable<Integer> appleStocksInDollar() {
        return appleStocksStream;
    }

    public Flowable<Double> getDollarEurConversionStream() {
        return dollarEurConversionStream;
    }
}
