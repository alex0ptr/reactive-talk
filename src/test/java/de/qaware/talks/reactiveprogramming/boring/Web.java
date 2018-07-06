package de.qaware.talks.reactiveprogramming.boring;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Random;

import static java.time.Duration.ofMillis;

public class Web {

    private static final String[] headLines = new String[] {
            "Cal Poly Taps AWS To Help It Solve Public Sector Tech Problems",
            "Nokia, AWS Partner To Drive Cloud Adoption in Large Orgs",
            "AWS Adds VPC Support to Elasticsearch Service",
            "AWS Now Lets Lightsail Spin Up Windows Virtual Servers",
            "SaaS, PaaS Spending To Outpace Estimates in 2017",
            "AWS Links CodeStar and CodeBuild to GitHub",
            "AWS, Microsoft Collaborate on Deep Learning with 'Gluon'"
    };
    private final static Random random = new Random();
    private final Flux<String> awsNewsStream;
    private final Flux<Integer> awsStocksStream;
    private final Flux<Double> dollarEurConversionStream;


    public Web() {
        awsNewsStream = Mono.fromCallable(() -> headLines[random.nextInt(headLines.length)])
                .flux()
                .compose(Web::repeatAtRandomTime)
                .share();

        awsStocksStream = Mono.fromCallable(() -> 135 + random.nextInt(50))
                .flux()
                .compose(Web::repeatAtRandomTime)
                .share();

        dollarEurConversionStream = Mono.fromCallable(() -> 0.8 + random.nextFloat() * 2)
                .flux()
                .compose(Web::repeatAtRandomTime)
                .share();
    }

    private static <T> Flux<T> repeatAtRandomTime(Flux<T> toRepeat) {
        return toRepeat
                .delaySubscription(ofMillis(1000 + random.nextInt(4000)))
                .repeat();
    }

    public Flux<String> awsNews() {
        return awsNewsStream;
    }

    public Flux<Integer> awsStockInDollar() {
        return awsStocksStream;
    }

    public Flux<Double> getDollarEurConversion() {
        return dollarEurConversionStream;
    }
}
