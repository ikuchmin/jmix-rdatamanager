package ru.udya.rdatamanager.store;

import io.jmix.core.DataStore;
import io.jmix.core.LoadContext;
import io.jmix.core.SaveContext;
import io.jmix.core.ValueLoadContext;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.impl.TransactionManagerLocator;
import io.jmix.eclipselink.impl.JpaDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.atomic.AtomicLong;


public class AbstractRDataStore implements RDataStore {

    private static final Logger log = LoggerFactory.getLogger(AbstractRDataStore.class);

    public static final String CHECK_TX_PREFIX = "AbstractRDataStore-check-";

    protected static final AtomicLong txCount = new AtomicLong();

    protected DataStore delegate;

    protected TransactionManagerLocator transactionManagerLocator;

    public AbstractRDataStore(TransactionManagerLocator transactionManagerLocator) {
        this.transactionManagerLocator = transactionManagerLocator;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public void setName(String name) {
        delegate.setName(name);
    }

    @Override
    public Mono<Object> load(LoadContext<?> context) {
        var result = Mono.fromCallable(() -> delegate.load(context));

        if (!isActualTransactionActive()) {
            return result.subscribeOn(Schedulers.boundedElastic());
        }

        return result;
    }

    @Override
    public Flux<Object> loadList(LoadContext<?> context) {
        var result = Flux.defer(() -> Flux
                .fromIterable(delegate.loadList(context)));

        if (!isActualTransactionActive()) {
            return result.subscribeOn(Schedulers.boundedElastic());
        }

        return result;
    }

    @Override
    public Mono<Long> getCount(LoadContext<?> context) {
        var result = Mono.fromCallable(() -> delegate.getCount(context));

        if (!isActualTransactionActive()) {
            return result.subscribeOn(Schedulers.boundedElastic());
        }

        return result;
    }

    @Override
    public Flux<?> save(SaveContext context) {
        var result = Flux.defer(() -> Flux
                .fromIterable(delegate.save(context)));

        if (!isActualTransactionActive()) {
            return result.subscribeOn(Schedulers.boundedElastic());
        }

        return result;
    }

    @Override
    public Flux<KeyValueEntity> loadValues(ValueLoadContext context) {
        var result = Flux.defer(() -> Flux
                .fromIterable(delegate.loadValues(context)));

        if (!isActualTransactionActive()) {
            return result.subscribeOn(Schedulers.boundedElastic());
        }

        return result;
    }

    @Override
    public Mono<Long> getCount(ValueLoadContext context) {
        var result = Mono.fromCallable(() -> delegate.getCount(context));

        if (!isActualTransactionActive()) {
            return result.subscribeOn(Schedulers.boundedElastic());
        }

        return result;
    }

    @Override
    public void wrapDelegate(DataStore dataStore) {
        delegate = (JpaDataStore) dataStore;
    }

    /**
     * Tries to get MANDATORY transaction
     *
     * @return true if it is got, false otherwise
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean isActualTransactionActive() {

        // create tx definition
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName(CHECK_TX_PREFIX + txCount.incrementAndGet());
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_MANDATORY);

        // got by tx manager
        var transactionManager = transactionManagerLocator.getTransactionManager(getName());
        try {
            transactionManager.getTransaction(def);
        } catch (IllegalTransactionStateException e) {
            if (e.getMessage().equals("No existing transaction found for transaction marked with propagation 'mandatory'")) {
                return false;
            }

            // if something another
            throw e;
        }

        return true;
    }

}
