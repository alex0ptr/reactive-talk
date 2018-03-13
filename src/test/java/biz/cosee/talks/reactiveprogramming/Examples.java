package biz.cosee.talks.reactiveprogramming;

import biz.cosee.talks.reactiveprogramming.boring.*;
import io.reactivex.Flowable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Examples {

    private static void updateLine(LineEndChanged lineEndChanged) {
    }

    private void setUserImage(Integer image) {
    }

    private static Logger log = LoggerFactory.getLogger(Examples.class);

    private static void printNext(Object o) {
        log.info("next: {}", o);
    }

    private static void printError(Throwable err) {
        log.error(err.getMessage());
    }

    private static void printCompleted() {
        log.info("completed");
    }

    private static Service service = new Service();
    private static App app = new App();
    private static AppStateService appStateService = AppStateService.createDefault();
    private static Web web = new Web();


    private void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void rtfm() {
        Flowable.fromArray(1, 2, 3)
                .delay(10, TimeUnit.MILLISECONDS) // documentation
                .map(number -> number * 3)
                .filter(number -> number % 2 == 0)
                .subscribe(                         // nothing happens without subscription (pull)
                        number -> printNext(number),
                        err -> printError(err),
                        () -> printCompleted()
                );
    }

    @Test
    public void notFutures() {
        Flowable<Integer> result = Flowable.fromCallable(() -> service.expensiveOperation("calculate a random"));

        result.subscribe(Examples::printNext);
        result.subscribe(Examples::printNext);
    }


    @Test
    public void hot() {
        ConnectableFlowable<Integer> result = Flowable.fromCallable(() -> service.expensiveOperation())
                .publish();

        result.subscribe(Examples::printNext);
        result.subscribe(Examples::printNext);
        result.connect();
    }


    // General
    @Test
    public void jobs() {
        Flowable.fromArray("http://jobs.de", "http://jobs.us", "http://jobs.timbuktu")
                .map(url -> service.netWorkOperation(url))
                .map(job -> service.expensiveOperation(job))
                .subscribe(Examples::printNext);
    }


    @Test
    public void poolDiving() {
        Flowable.fromArray("http://jobs.de", "http://jobs.us", "http://jobs.timbuktu")
                .map(url -> service.netWorkOperation(url))
                .subscribeOn(Schedulers.io())
                .doOnNext(delay -> log.info("network: {}", delay))
                .observeOn(Schedulers.computation())
                .map(job -> service.expensiveOperation(job))
                .subscribe(Examples::printNext);

        sleep(10000);
    }

    @Test
    public void jobsParallelNetwork() {
        Flowable.fromArray("http://jobs.de", "http://jobs.us", "http://jobs.timbuktu")
                .flatMap(url -> Flowable.fromCallable(() -> service.netWorkOperation(url))
                        .subscribeOn(Schedulers.io())
                        .doOnNext(delay -> log.info("network: {}", delay)))
                .map(job -> service.expensiveOperation(job))
                .subscribe(Examples::printNext);

        sleep(10000);
    }

    @Test
    public void manyJobs() {
        Flowable.fromArray("http://jobs.de", "http://jobs.us", "http://jobs.timbuktu")
                .repeat(100)
                .flatMap(url -> Flowable.fromCallable(() -> service.netWorkOperation(url))
                        .subscribeOn(Schedulers.io())
                        .doOnNext(delay -> log.info("network: {}", delay)))

                .flatMap(job -> Flowable.fromCallable(() -> service.expensiveOperation(job))
                        .subscribeOn(Schedulers.computation())
                        .doOnNext(delay -> log.info("computation: {}", delay)))
                .subscribe(Examples::printNext);

        sleep(10000);
    }

    @Test
    public void jobsRefactored() {
        Flowable.fromArray("http://jobs.de", "http://jobs.us", "http://jobs.timbuktu")
                .flatMapSingle(service::netWorkOperationSingle)
                .flatMapSingle(service::expensiveOperationSingle)
                .blockingSubscribe(Examples::printNext); // never ever do this; legacy stacks are the exception
    }

    @Test
    public void apiGatewayMagic() {
        service.netWorkOperationSingle("http://buggy.bank.money")
                .retry(1)
                .onErrorResumeNext((err) -> service.netWorkOperationSingle("http://slow.bank.money"))
                .zipWith(service.netWorkOperationSingle("http://currency.conversions"),
                        (money, conversion) -> money * conversion)
                .subscribe(
                        Examples::printNext,
                        Examples::printError);

        sleep(1000);
    }

    // GUIs
    @Test
    public void dimensionsStable() {
        app.onResizes()
                .debounce(500, TimeUnit.MILLISECONDS)
                .flatMapSingle(newSize -> service.netWorkOperationSingle("http://web.service/user/avatar?width=" + newSize.getWidth() / 2))
                .subscribe(this::setUserImage);
    }

    @Test
    public void statePropagation() {
        MyViewComponent debuggableView = new MyViewComponent();
        MyViewComponent textView = new MyViewComponent();
        PublishProcessor<Object> debugViewDestroying = PublishProcessor.create();
        PublishProcessor<Object> textViewDestroying = PublishProcessor.create();

        appStateService.onStateChanges()
                .map(ApplicationState::isDebugMode)
                .distinctUntilChanged()
                .takeUntil(debugViewDestroying)
                .doOnNext(next -> log.info("DEBUG View now displaying debug: " + next))
                .subscribe(debuggableView::showDebug);

        appStateService.onStateChanges()
                .map(ApplicationState::getLanguage)
                .distinctUntilChanged()
                .takeUntil(textViewDestroying)
                .doOnNext(next -> log.info("TEXT View now displaying in: " + next))
                .subscribe(textView::showInLanguage);

        appStateService.toggleDebugMode();
        appStateService.toggleDebugMode();
        appStateService.setNewLanguage(Locale.GERMAN); // default is GERMAN
        appStateService.setNewLanguage(Locale.ENGLISH);
        appStateService.setNewLanguage(Locale.GERMAN);
        appStateService.setNewLanguage(Locale.GERMAN);


        debugViewDestroying.onNext(new Object());
        debugViewDestroying.onComplete();

        appStateService.toggleDebugMode();
        appStateService.setNewLanguage(Locale.CHINA);
    }

    @Test
    public void sseAWSTwitterStocks() {
        DecimalFormat df = new DecimalFormat("#.00");

        Flowable<String> awsNews = web.awsNews()
                .doOnNext(next -> log.info("news"));
        Flowable<Integer> awsStock = web.awsStockInDollar()
                .doOnNext(next -> log.info("stock"));
        Flowable<Double> dollarToEur = web.getDollarEurConversion()
                .doOnNext(next -> log.info("conversion"));

        Flowable.combineLatest(
                awsNews,
                awsStock,
                dollarToEur,
                (news, stock, conversion) -> "AWS " + df.format(stock * conversion) + " EUR; Latest News: " + news)
                .subscribe(Examples::printNext);

        sleep(60000);
    }

    @Test
    public void multiClick() {
        app.onClicks()
                .buffer(app.onClicks()
                        .debounce(200, TimeUnit.MILLISECONDS))
                .map(List::size)
                .subscribe(Examples::printNext);
    }

    @Test
    public void drawLine() {
        app.mouseDown()
                .map(position -> new Line(position, position))
                .flatMap(line -> app.mouseMove()
                        .takeUntil(app.mouseUp())
                        .map(position -> new LineEndChanged(line, position)))
                .subscribe(Examples::updateLine); // side effects are here
    }

}