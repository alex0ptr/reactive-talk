package de.qaware.talks.reactiveprogramming;

import de.qaware.talks.reactiveprogramming.boring.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Schedulers;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import static java.time.Duration.ofMillis;

public class Examples {

    private static void updateLine(LineEndChanged lineEndChanged) {
    }

    private void setUserImage(Integer image) {
    }

    private static Logger log = LoggerFactory.getLogger(Examples.class);

    private static void printNext(Object o) {
        log.info("next: {}", o);
    }

    private static <T> Flux<T> printComputation(Flux<T> flux) {
        return flux.doOnNext(it -> log.info("computation: {}", it));
    }

    private static <T> Flux<T> printNetwork(Flux<T> flux) {
        return flux.doOnNext(it -> log.info("network: {}", it));
    }


    private static <T> Mono<T> printComputation(Mono<T> mono) {
        return mono.doOnNext(it -> log.info("computation: {}", it));
    }

    private static <T> Mono<T> printNetwork(Mono<T> mono) {
        return mono.doOnNext(it -> log.info("network: {}", it));
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
        Flux.just(1, 2, 3)
                .delayElements(ofMillis(10)) // documentation
                .map(number -> number * 3)
                .filter(number -> number % 2 == 0)
                .subscribe(  // nothing happens without subscription (pull)
                        number -> printNext(number),
                        err -> printError(err),
                        () -> printCompleted()
                );
    }

    @Test
    public void notFutures() {
        Mono<Integer> result = Mono.fromCallable(() -> service.expensiveOperation("calculate a random"));

        result.subscribe(Examples::printNext);
        result.subscribe(Examples::printNext);
    }


    @Test
    public void hot() {
        ConnectableFlux<Integer> result = Mono.fromCallable(() -> service.expensiveOperation())
                .flux()
                .publish();

        result.subscribe(Examples::printNext);
        result.subscribe(Examples::printNext);
        result.connect();
    }

    @Test
    public void apiGatewayMagic() {
        service.netWorkOperationMono("http://buggy.bank.money")
                .retry(1)
                .onErrorResume((err) -> service.netWorkOperationMono("http://slow.bank.money"))
                .zipWith(service.netWorkOperationMono("http://currency.conversions"),
                        (money, conversion) -> money * conversion)
                .subscribe(
                        Examples::printNext,
                        Examples::printError);

        sleep(1000);
    }

    // General
    @Test
    public void jobs() {
        Flux.just("http://jobs.de", "http://jobs.us", "http://jobs.timbuktu")
                .map(url -> service.netWorkOperation(url))
                .map(job -> service.expensiveOperation(job))
                .subscribe(Examples::printNext);
    }


    @Test
    public void poolDiving() {
        Flux.just("http://jobs.de", "http://jobs.us", "http://jobs.timbuktu")
                .map(url -> service.netWorkOperation(url))
                .subscribeOn(Schedulers.elastic())
                .compose(Examples::printNetwork) // same as doOnNext(it -> log.info("network: {}", it))
                .publishOn(Schedulers.parallel())
                .map(job -> service.expensiveOperation(job))
                .compose(Examples::printComputation)
                .subscribe();

        sleep(10000);
    }

    @Test
    public void jobsParallelNetwork() {
        Flux.just("http://jobs.de", "http://jobs.us", "http://jobs.timbuktu")
                .flatMap(url -> Mono.fromCallable(() -> service.netWorkOperation(url))
                        .subscribeOn(Schedulers.elastic())
                        .compose(Examples::printNetwork))
                .map(job -> service.expensiveOperation(job))
                .subscribe(Examples::printNext);

        sleep(10000);
    }

    @Test
    public void manyJobs() {
        Flux.just("http://jobs.de", "http://jobs.us", "http://jobs.timbuktu")
                .repeat(1000)
                .flatMap(url -> Mono.fromCallable(() -> service.netWorkOperation(url))
                        .subscribeOn(Schedulers.elastic())
                        .compose(Examples::printNetwork))

                .flatMap(job -> Mono.fromCallable(() -> service.expensiveOperation(job))
                        .subscribeOn(Schedulers.parallel())
                        .compose(Examples::printComputation))
                .subscribe();

        sleep(20000);
    }

    @Test
    public void jobsRefactored() {
        Flux.just("http://jobs.de", "http://jobs.us", "http://jobs.timbuktu")
                .flatMap(service::netWorkOperationMono)
                .flatMap(service::expensiveOperationSingle)
                .blockLast(); // never ever do this; legacy stacks are the exception
    }

    // GUIs
    @Test
    public void dimensionsStable() {
        app.onResizes()
                .sampleTimeout(dimension -> Mono.delay(ofMillis(500)))
                .flatMap(newSize -> service.netWorkOperationMono("http://web.service/user/avatar?width=" + newSize.getWidth() / 2))
                .subscribe(this::setUserImage);
    }

    @Test
    public void statePropagation() {
        MyViewComponent debuggableView = new MyViewComponent();
        MyViewComponent textView = new MyViewComponent();
        ReplayProcessor<Object> debugViewDestroying = ReplayProcessor.cacheLast();
        ReplayProcessor<Object> textViewDestroying = ReplayProcessor.cacheLast();

        appStateService.onStateChanges()
                .map(ApplicationState::isDebugMode)
                .distinctUntilChanged()
                .takeUntilOther(debugViewDestroying)
                .doOnNext(next -> log.info("DEBUG View now displaying debug: " + next))
                .subscribe(debuggableView::showDebug);

        appStateService.onStateChanges()
                .map(ApplicationState::getLanguage)
                .distinctUntilChanged()
                .takeUntilOther(textViewDestroying)
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

        Flux<String> awsNews = web.awsNews()
                .doOnNext(next -> log.info("news"));
        Flux<Integer> awsStock = web.awsStockInDollar()
                .doOnNext(next -> log.info("stock"));
        Flux<Double> dollarToEur = web.getDollarEurConversion()
                .doOnNext(next -> log.info("conversion"));

        Flux<Double> priceEuro = Flux.combineLatest(awsStock, dollarToEur, (stock, dollar) -> stock * dollar);
        Flux.combineLatest(priceEuro, awsNews, (price, news) -> "AWS " + df.format(price) + " EUR; Latest News: " + news)
                .subscribe(Examples::printNext);

        sleep(60000);
    }

    @Test
    public void drawLine() {
        app.mouseDown()
                .map(position -> new Line(position, position))
                .flatMap(line -> app.mouseMove()
                        .takeUntilOther(app.mouseUp())
                        .map(position -> new LineEndChanged(line, position)))
                .subscribe(Examples::updateLine); // side effects are here
    }

    @Test
    public void multiClick() {
        app.onClicks()
                .buffer(app.onClicks()
                        .sampleTimeout(it -> Mono.delay(ofMillis(200))))
                .map(List::size)
                .subscribe(Examples::printNext);
    }

}