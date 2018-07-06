package biz.cosee.talks.reactiveprogramming.boring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Random;

public class Service {

    private static Logger log = LoggerFactory.getLogger(Service.class);

    private static void printNext(Object o) {
        log.info("event: {}", o);
    }

    private static void printError(Throwable err) {
        log.error(err.getMessage());
    }

    private static void printCompleted() {
        log.info("completed");
    }

    private static Service service = new Service();

    public int expensiveOperation() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return new Random().nextInt();
    }

    public int expensiveOperation(Object any) {
        return expensiveOperation();
    }

    public int netWorkOperation(String url) {
        int duration = new Random().nextInt(100) + 50;
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return duration;
    }

    public Mono<Integer> netWorkOperationMono(String url) {
        return Mono.fromCallable(() -> netWorkOperation(url))
                .subscribeOn(Schedulers.elastic())
                .doOnSuccess(delay -> log.info("network: {}", delay));
    }

    public Mono<Integer> expensiveOperationSingle(Integer integer) {
        return Mono.fromCallable(this::expensiveOperation)
                .subscribeOn(Schedulers.parallel())
                .doOnSuccess(delay -> log.info("computation: {}", delay));
    }
}
