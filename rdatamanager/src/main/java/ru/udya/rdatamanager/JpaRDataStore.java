package ru.udya.rdatamanager;

import io.jmix.core.LoadContext;
import io.jmix.core.SaveContext;
import io.jmix.core.ValueLoadContext;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.eclipselink.impl.JpaDataStore;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import static org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive;

@Component("rdm_JpaDataStore")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JpaRDataStore implements RDataStore {

    protected JpaDataStore jpaDataStore;

    public JpaRDataStore(JpaDataStore jpaDataStore) {
        this.jpaDataStore = jpaDataStore;
    }

    @Override
    public String getName() {
        return jpaDataStore.getName();
    }

    @Override
    public void setName(String name) {
        jpaDataStore.setName(name);
    }

    @Override
    public Mono<Object> load(LoadContext<?> context) {
        if (isActualTransactionActive()) {
            return Mono.justOrEmpty(jpaDataStore.load(context));
        } else {
            return Mono.fromCallable(() -> jpaDataStore.load(context))
                    .subscribeOn(Schedulers.boundedElastic());
        }
    }

    @Override
    public Flux<Object> loadList(LoadContext<?> context) {
        return null;
    }

    @Override
    public Mono<Long> getCount(LoadContext<?> context) {
        return null;
    }

    @Override
    public Flux<?> save(SaveContext context) {
        return null;
    }

    @Override
    public Mono<KeyValueEntity> loadValues(ValueLoadContext context) {
        return null;
    }

    @Override
    public Mono<Long> getCount(ValueLoadContext context) {
        return null;
    }
}
