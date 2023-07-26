package ru.udya.rdatamanager;

import io.jmix.core.FluentLoader;
import io.jmix.core.FluentValueLoader;
import io.jmix.core.FluentValuesLoader;
import io.jmix.core.Id;
import io.jmix.core.LoadContext;
import io.jmix.core.SaveContext;
import io.jmix.core.ValueLoadContext;
import io.jmix.core.entity.KeyValueEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class UnconstrainedRDataManagerImpl implements UnconstrainedRDataManager {

    @Override
    public <E> Mono<E> load(LoadContext<E> context) {
        return null;
    }

    @Override
    public <E> Flux<E> loadList(LoadContext<E> context) {
        return null;
    }

    @Override
    public Mono<Long> getCount(LoadContext<?> context) {
        return null;
    }

    @Override
    public <E> Flux<E> save(SaveContext context) {
        return null;
    }

    @Override
    public <E> Flux<E> save(Object... entities) {
        return null;
    }

    @Override
    public <E> Mono<E> save(E entity) {
        return null;
    }

    @Override
    public Mono<Void> remove(Object... entity) {
        return null;
    }

    @Override
    public <E> Mono<Void> remove(Id<E> entityId) {
        return null;
    }

    @Override
    public Flux<KeyValueEntity> loadValues(ValueLoadContext context) {
        return null;
    }

    @Override
    public Mono<Long> getCount(ValueLoadContext context) {
        return null;
    }

    @Override
    public <E> FluentLoader<E> load(Class<E> entityClass) {
        return null;
    }

    @Override
    public <E> FluentLoader.ById<E> load(Id<E> entityId) {
        return null;
    }

    @Override
    public FluentValuesLoader loadValues(String queryString) {
        return null;
    }

    @Override
    public <T> FluentValueLoader<T> loadValue(String queryString, Class<T> valueClass) {
        return null;
    }

    @Override
    public <T> T create(Class<T> entityClass) {
        return null;
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object id) {
        return null;
    }

    @Override
    public <T> T getReference(Id<T> entityId) {
        return null;
    }
}
