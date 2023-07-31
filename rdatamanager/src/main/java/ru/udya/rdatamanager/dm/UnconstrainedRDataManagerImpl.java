package ru.udya.rdatamanager.dm;

import io.jmix.core.CoreProperties;
import io.jmix.core.EntityStates;
import io.jmix.core.ExtendedEntities;
import io.jmix.core.FetchPlan;
import io.jmix.core.FluentValueLoader;
import io.jmix.core.Id;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.SaveContext;
import io.jmix.core.Stores;
import io.jmix.core.ValueLoadContext;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.constraint.AccessConstraint;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.impl.CrossDataStoreReferenceLoader;
import io.jmix.core.impl.TransactionManagerLocator;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.udya.rdatamanager.fl.RFluentLoader;
import ru.udya.rdatamanager.fl.RFluentValueLoader;
import ru.udya.rdatamanager.fl.RFluentValuesLoader;
import ru.udya.rdatamanager.store.RDataStore;
import ru.udya.rdatamanager.store.RDataStoreFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

@Primary
@Component("rdm_UnconstrainedRDataManager")
public class UnconstrainedRDataManagerImpl implements UnconstrainedRDataManager {
    private static final Logger log = LoggerFactory.getLogger(UnconstrainedRDataManagerImpl.class);

    public static final String SAVE_TX_PREFIX = "RDataManager-save-";

    protected static final AtomicLong txCount = new AtomicLong();

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected EntityStates entityStates;

    @Autowired
    protected Stores stores;

    @Autowired
    protected ObjectProvider<RFluentLoader> fluentLoaderProvider;

    @Autowired
    protected ObjectProvider<RFluentValueLoader> fluentValueLoaderProvider;

    @Autowired
    protected ObjectProvider<RFluentValuesLoader> fluentValuesLoaderProvider;

    @Autowired
    protected ObjectProvider<CrossDataStoreReferenceLoader> crossDataStoreReferenceLoaderProvider;

    @Autowired
    protected ExtendedEntities extendedEntities;

    @Autowired
    protected CoreProperties properties;

    @Autowired
    protected TransactionManagerLocator transactionManagerLocator;

    @Autowired
    protected RDataStoreFactory rDataStoreFactory;

    @Override
    public <E> Mono<E> load(LoadContext<E> context) {
        MetaClass metaClass = getEffectiveMetaClassFromContext(context);
        RDataStore storage = rDataStoreFactory.get(getStoreName(metaClass));

        context.setAccessConstraints(mergeConstraints(context.getAccessConstraints()));

        //noinspection unchecked
        return (Mono<E>) storage.load(context).doOnNext(entity -> {
            if (entity != null)
                readCrossDataStoreReferences(Collections.singletonList(entity),
                        context.getFetchPlan(), metaClass, context.isJoinTransaction());
        });

    }

    @Override
    public <E> Flux<E> loadList(LoadContext<E> context) {
        MetaClass metaClass = getEffectiveMetaClassFromContext(context);
        RDataStore storage = rDataStoreFactory.get(getStoreName(metaClass));

        context.setAccessConstraints(mergeConstraints(context.getAccessConstraints()));

        //noinspection unchecked
        return (Flux<E>) storage.loadList(context).doOnNext(e ->
                readCrossDataStoreReferences(Collections.singletonList(e),
                        context.getFetchPlan(), metaClass, context.isJoinTransaction()));
    }

    @Override
    public Mono<Long> getCount(LoadContext<?> context) {
        MetaClass metaClass = getEffectiveMetaClassFromContext(context);
        RDataStore storage = rDataStoreFactory.get(getStoreName(metaClass));

        context.setAccessConstraints(mergeConstraints(context.getAccessConstraints()));

        return storage.getCount(context);
    }

    @Override
    public <E> Flux<E> save(SaveContext context) {
        context.setAccessConstraints(mergeConstraints(context.getAccessConstraints()));
        Map<String, SaveContext> storeToContextMap = new TreeMap<>();
        Set<Object> toRepeat = new HashSet<>();
        for (Object entity : context.getEntitiesToSave()) {
            MetaClass metaClass = metadata.getClass(entity);
            String storeName = getStoreName(metaClass);

            boolean repeatRequired = writeCrossDataStoreReferences(entity, context.getEntitiesToSave());
            if (repeatRequired) {
                toRepeat.add(entity);
            }

            SaveContext sc = storeToContextMap.computeIfAbsent(storeName, key -> createSaveContext(context));
            sc.saving(entity);
            FetchPlan fetchPlan = context.getFetchPlans().get(entity);
            if (fetchPlan != null)
                sc.getFetchPlans().put(entity, fetchPlan);
        }
        for (Object entity : context.getEntitiesToRemove()) {
            MetaClass metaClass = metadata.getClass(entity);
            String storeName = getStoreName(metaClass);

            SaveContext sc = storeToContextMap.computeIfAbsent(storeName, key -> createSaveContext(context));
            sc.removing(entity);
            FetchPlan fetchPlan = context.getFetchPlans().get(entity);
            if (fetchPlan != null)
                sc.getFetchPlans().put(entity, fetchPlan);
        }

        List<Flux<?>> result = new ArrayList<>();
        for (String store : storeToContextMap.keySet()) {
            result.add(saveContextToStore(store, storeToContextMap.get(store)));
        }

        //noinspection unchecked
        return (Flux<E>) Flux.merge(result).collectList().flatMapMany(entities -> {
            var res = new TreeSet<>(entities);

            if (! toRepeat.isEmpty()) {
                SaveContext sc = new SaveContext();
                sc.setJoinTransaction(context.isJoinTransaction());
                for (Object entity : entities) {
                    if (toRepeat.contains(entity)) {
                        sc.saving(entity, context.getFetchPlans().get(entity));
                    }
                }
                var committedEntities = save(sc).toIterable();
                for (Object committedEntity : committedEntities) {
                    if (res.contains(committedEntity)) {
                        res.remove(committedEntity);
                        res.add(committedEntity);
                    }
                }
            }

            return Flux.fromIterable(res);
        });
    }

    @Override
    public <E> Flux<E> save(Object... entities) {
        return save(new SaveContext().saving(entities));
    }

    @Override
    public <E> Mono<E> save(E entity) {
        return this.<E>save(new SaveContext().saving(entity))
                .filter(e -> e.equals(entity)).single()
                .switchIfEmpty(Mono.error(new IllegalStateException("Data store didn't return a saved entity")));
    }

    @Override
    public Mono<Void> remove(Object... entities) {
        return save(new SaveContext().removing(entities)).then();
    };

    @Override
    public <E> Mono<Void> remove(Id<E> entityId) {
        return remove(getReference(entityId));
    }

    @Override
    public Flux<KeyValueEntity> loadValues(ValueLoadContext context) {
        RDataStore store = rDataStoreFactory.get(getStoreName(context.getStoreName()));
        context.setAccessConstraints(mergeConstraints(context.getAccessConstraints()));
        return store.loadValues(context);
    }

    @Override
    public Mono<Long> getCount(ValueLoadContext context) {
        RDataStore store = rDataStoreFactory.get(getStoreName(context.getStoreName()));
        context.setAccessConstraints(mergeConstraints(context.getAccessConstraints()));
        return store.getCount(context);
    }

    @Override
    public <E> RFluentLoader<E> load(Class<E> entityClass) {
        //noinspection unchecked
        RFluentLoader<E> fluentLoader = fluentLoaderProvider.getObject(entityClass);
        fluentLoader.setDataManager(this);
        return fluentLoader;
    }

    @Override
    public <E> RFluentLoader.ById<E> load(Id<E> entityId) {
        //noinspection unchecked
        RFluentLoader<E> fluentLoader = fluentLoaderProvider.getObject(entityId.getEntityClass());
        fluentLoader.setDataManager(this);
        return fluentLoader.id(entityId.getValue());
    }

    @Override
    public RFluentValuesLoader loadValues(String queryString) {
        RFluentValuesLoader fluentValuesLoader = fluentValuesLoaderProvider.getObject(queryString);
        fluentValuesLoader.setDataManager(this);
        return fluentValuesLoader;
    }

    @Override
    public <T> RFluentValueLoader<T> loadValue(String queryString, Class<T> valueClass) {
        //noinspection unchecked
        RFluentValueLoader<T> fluentValueLoader = fluentValueLoaderProvider.getObject(queryString, valueClass);
        fluentValueLoader.setDataManager(this);
        return fluentValueLoader;

    }

    @Override
    public <T> T create(Class<T> entityClass) {
        return metadata.create(entityClass);
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object id) {
        T entity = metadata.create(entityClass, id);
        entityStates.makePatch(entity);
        return entity;
    }

    @Override
    public <T> T getReference(Id<T> entityId) {
        Preconditions.checkNotNullArgument(entityId, "entityId is null");
        return getReference(entityId.getEntityClass(), entityId.getValue());
    }

    protected Flux<?> saveContextToStore(String store, SaveContext context) {
        RDataStore dataStore = rDataStoreFactory.get(store);
        return dataStore.save(context);
    }

    protected SaveContext createSaveContext(SaveContext context) {
        SaveContext newCtx = new SaveContext();
        newCtx.setHints(context.getHints());
        newCtx.setDiscardSaved(context.isDiscardSaved());
        newCtx.setAccessConstraints(context.getAccessConstraints());
        newCtx.setJoinTransaction(context.isJoinTransaction());
        return newCtx;
    }

    protected boolean writeCrossDataStoreReferences(Object entity, Collection<Object> allEntities) {
        if (stores.getAdditional().isEmpty())
            return false;

        boolean repeatRequired = false;
        MetaClass metaClass = metadata.getClass(entity);
        for (MetaProperty property : metaClass.getProperties()) {
            if (property.getRange().isClass() && !property.getRange().getCardinality().isMany()) {
                MetaClass propertyMetaClass = property.getRange().asClass();
                if (! Objects.equals(propertyMetaClass.getStore().getName(), metaClass.getStore().getName())) {
                    List<String> dependsOnProperties = metadataTools.getDependsOnProperties(property);
                    if (dependsOnProperties.size() == 0) {
                        continue;
                    }
                    if (dependsOnProperties.size() > 1) {
                        log.warn("More than 1 property is defined for attribute {} in DependsOnProperty annotation, skip handling different data store", property);
                        continue;
                    }
                    String relatedPropertyName = dependsOnProperties.get(0);
                    if (entityStates.isLoaded(entity, relatedPropertyName)) {
                        Object refEntity = EntityValues.getValue(entity, property.getName());
                        if (refEntity == null) {
                            EntityValues.setValue(entity, relatedPropertyName, null);
                        } else {
                            Object refEntityId = EntityValues.getId(refEntity);
                            MetaClass refEntityMetaClass = metadata.getClass(refEntity);
                            if (refEntityId == null) {
                                Object refEntityGeneratedId = EntityValues.getGeneratedId(refEntity);
                                if (allEntities.stream().anyMatch(e -> EntityValues.getGeneratedId(e).equals(refEntityGeneratedId))) {
                                    repeatRequired = true;
                                } else {
                                    log.warn("No entity with generated ID={} in the context, skip handling different data store", refEntityGeneratedId);
                                }
                            } else if (metadataTools.hasCompositePrimaryKey(refEntityMetaClass)) {
                                MetaProperty relatedProperty = metaClass.getProperty(relatedPropertyName);
                                if (!relatedProperty.getRange().isClass()) {
                                    log.warn("PK of entity referenced by {} is a EmbeddableEntity, but related property {} is not", property, relatedProperty);
                                } else {
                                    EntityValues.setValue(entity, relatedPropertyName, metadataTools.copy(refEntityId));
                                }
                            } else {
                                EntityValues.setValue(entity, relatedPropertyName, refEntityId);
                            }
                        }
                    }
                }
            }
        }
        return repeatRequired;
    }

    protected void readCrossDataStoreReferences(Collection<?> entities, FetchPlan fetchPlan, MetaClass metaClass,
                                                boolean joinTransaction) {
        if (stores.getAdditional().isEmpty() || entities.isEmpty() || fetchPlan == null)
            return;

        CrossDataStoreReferenceLoader crossDataStoreReferenceLoader = crossDataStoreReferenceLoaderProvider.getObject(
                metaClass, fetchPlan, joinTransaction);
        crossDataStoreReferenceLoader.processEntities(entities);
    }

    protected String getStoreName(MetaClass metaClass) {
        return metaClass.getStore().getName();
    }

    protected String getStoreName(@Nullable String storeName) {
        return storeName == null ? Stores.NOOP : storeName;
    }

    protected <E> MetaClass getEffectiveMetaClassFromContext(LoadContext<E> context) {
        return extendedEntities.getEffectiveMetaClass(context.getEntityMetaClass());
    }

    protected List<AccessConstraint<?>> mergeConstraints(List<AccessConstraint<?>> accessConstraints) {
        if (accessConstraints.isEmpty()) {
            return getAppliedConstraints();
        } else {
            Set<AccessConstraint<?>> newAccessConstraints = new LinkedHashSet<>(getAppliedConstraints());
            newAccessConstraints.addAll(accessConstraints);
            return new ArrayList<>(newAccessConstraints);
        }
    }

    protected List<AccessConstraint<?>> getAppliedConstraints() {
        return Collections.emptyList();
    }

}
