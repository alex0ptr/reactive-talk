package biz.cosee.talks.reactiveprogramming.boring;

import io.reactivex.Flowable;

import java.util.Random;
import java.util.concurrent.TimeUnit;

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
    private final Flowable<String> awsNewsStream;
    private final Flowable<Integer> awsStocksStream;
    private final Flowable<Double> dollarEurConversionStream;


    public Web() {
        awsNewsStream = Flowable.fromCallable(() -> headLines[random.nextInt(headLines.length)])
                .compose(Web::repeatAtRandomTime)
                .share();

        awsStocksStream = Flowable.fromCallable(() -> 135 + random.nextInt(50))
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

    public Flowable<String> awsNews() {
        return awsNewsStream;
    }

    public Flowable<Integer> awsStockInDollar() {
        return awsStocksStream;
    }

    public Flowable<Double> getDollarEurConversion() {
        return dollarEurConversionStream;
    }
}
