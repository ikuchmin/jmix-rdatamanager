package ru.udya.rdatamanager;

import io.jmix.core.LoadContext;
import io.jmix.core.SaveContext;
import io.jmix.core.ValueLoadContext;
import io.jmix.core.entity.KeyValueEntity;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public interface RDataStore {

    /**
     * This data store instance name with which it is registered in {@link io.jmix.core.Stores}.
     */
    String getName();

    /**
     * Sets this data store instance name with which it is registered in {@link io.jmix.core.Stores}.
     */
    void setName(String name);

    /**
     * Loads a single entity instance.
     *
     * @return the loaded object, or null if not found
     */
    @Nullable
    Mono<Object> load(LoadContext<?> context);

    /**
     * Loads collection of entity instances.
     *
     * @return a list of instances, or empty list if nothing found
     */
    Flux<Object> loadList(LoadContext<?> context);

    /**
     * Returns the number of entity instances for the given query passed in the {@link LoadContext}.
     *
     * @return number of instances in the storage
     */
    Mono<Long> getCount(LoadContext<?> context);

    /**
     * Saves a collection of entity instances.
     *
     * @return set of saved instances
     */
    Flux<?> save(SaveContext context);

    /**
     * Loads list of key-value pairs.
     *
     * @param context defines a query for scalar values and a list of keys for returned KeyValueEntity
     * @return list of KeyValueEntity instances
     */
    Mono<KeyValueEntity> loadValues(ValueLoadContext context);

    /**
     * Returns the number of key-value pairs for the given query passed in the {@link ValueLoadContext}.
     *
     * @param context defines the query
     * @return number of key-value pairs in the data store
     */
    Mono<Long> getCount(ValueLoadContext context);
}
