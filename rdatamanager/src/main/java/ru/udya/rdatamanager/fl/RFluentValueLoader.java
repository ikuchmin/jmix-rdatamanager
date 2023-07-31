/*
 * Copyright 2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.udya.rdatamanager.fl;

import io.jmix.core.ValueLoadContext;
import io.jmix.core.entity.KeyValueEntity;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TemporalType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component("rdm_RFluentValueLoader")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RFluentValueLoader<T> extends AbstractRFluentValueLoader {

    private final Class<T> valueClass;

    private final static String PROP_NAME = "p1";

    public RFluentValueLoader(String queryString, Class<T> valueClass) {
        super(queryString);
        this.valueClass = valueClass;
    }

    protected ValueLoadContext createLoadContext() {
        ValueLoadContext loadContext = super.createLoadContext();
        loadContext.addProperty(PROP_NAME);
        return loadContext;
    }

    @SuppressWarnings("unchecked")
    T castValue(Object value) {
        if (value != null && !value.getClass().equals(valueClass) && Number.class.isAssignableFrom(valueClass)) {
            if (valueClass.equals(Integer.class)) {
                return (T) Integer.valueOf(((Number) value).intValue());
            }
            if (valueClass.equals(Long.class)) {
                return (T) Long.valueOf(((Number) value).longValue());
            }
            if (valueClass.equals(Double.class)) {
                return (T) Double.valueOf(((Number) value).doubleValue());
            }
            if (valueClass.equals(Float.class)) {
                return (T) Float.valueOf(((Number) value).floatValue());
            }
            if (valueClass.equals(Short.class)) {
                return (T) Short.valueOf(((Number) value).shortValue());
            }
            if (valueClass.equals(BigDecimal.class)) {
                return (T) BigDecimal.valueOf(((Number) value).doubleValue());
            }
            if (valueClass.equals(BigInteger.class)) {
                return (T) BigInteger.valueOf(((Number) value).longValue());
            }
        }
        return (T) value;
    }

    /**
     * Loads a list of entities.
     */
    public Flux<T> list() {
        ValueLoadContext loadContext = createLoadContext();
        return dataManager.loadValues(loadContext)
                .map(e -> castValue(e.getValue(PROP_NAME)));
    }

    /**
     * Loads a single instance and wraps it in Optional.
     */
    public Mono<T> optional() {
        ValueLoadContext loadContext = createLoadContext();
        loadContext.getQuery().setMaxResults(1);
        Flux<KeyValueEntity> list = dataManager.loadValues(loadContext);
        return list.map(e -> castValue(e.getValue(PROP_NAME))).singleOrEmpty();
    }

    /**
     * Loads a single instance.
     *
     * @throws IllegalStateException if nothing was loaded
     */
    public Mono<T> one() {
        ValueLoadContext loadContext = createLoadContext();
        loadContext.getQuery().setMaxResults(1);
        Flux<KeyValueEntity> list = dataManager.loadValues(loadContext);
        return list.map(e -> castValue(e.getValue(PROP_NAME)))
                .singleOrEmpty().switchIfEmpty(
                        Mono.error(new IllegalStateException("No results")));
    }

    @Override
    public RFluentValueLoader<T> store(String store) {
        super.store(store);
        return this;
    }

    @Override
    public RFluentValueLoader<T> hint(String hintName, Serializable value) {
        super.hint(hintName, value);
        return this;
    }

    @Override
    public RFluentValueLoader<T> hints(Map<String, Serializable> hints) {
        super.hints(hints);
        return this;
    }

    @Override
    public RFluentValueLoader<T> parameter(String name, Object value) {
        super.parameter(name, value);
        return this;
    }

    @Override
    public RFluentValueLoader<T> parameter(String name, Date value, TemporalType temporalType) {
        super.parameter(name, value, temporalType);
        return this;
    }

    @Override
    public RFluentValueLoader<T> parameter(String name, Object value, boolean implicitConversion) {
        super.parameter(name, value, implicitConversion);
        return this;
    }

    @Override
    public RFluentValueLoader<T> setParameters(Map<String, Object> parameters) {
        super.setParameters(parameters);
        return this;
    }

    @Override
    public RFluentValueLoader<T> firstResult(int firstResult) {
        super.firstResult(firstResult);
        return this;
    }

    @Override
    public RFluentValueLoader<T> maxResults(int maxResults) {
        super.maxResults(maxResults);
        return this;
    }

    /**
     * Sets a lock mode to be used when executing query.
     */
    @Override
    public RFluentValueLoader<T> lockMode(LockModeType lockMode) {
        super.lockMode(lockMode);
        return this;
    }
}
