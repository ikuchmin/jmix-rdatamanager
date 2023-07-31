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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component("rdm_RFluentValuesLoader")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RFluentValuesLoader extends AbstractRFluentValueLoader {

    private List<String> properties = new ArrayList<>();

    public RFluentValuesLoader(String queryString) {
        super(queryString);
    }

    protected ValueLoadContext createLoadContext() {
        ValueLoadContext loadContext = super.createLoadContext();
        loadContext.setProperties(properties);
        return loadContext;
    }

    /**
     * Loads a list of entities.
     */
    public Flux<KeyValueEntity> list() {
        ValueLoadContext loadContext = createLoadContext();
        return dataManager.loadValues(loadContext);
    }

    /**
     * Loads a single instance and wraps it in Optional.
     */
    public Mono<KeyValueEntity> optional() {
        ValueLoadContext loadContext = createLoadContext();
        loadContext.getQuery().setMaxResults(1);
        return dataManager.loadValues(loadContext).singleOrEmpty();
    }

    /**
     * Loads a single instance.
     *
     * @throws IllegalStateException if nothing was loaded
     */
    public Mono<KeyValueEntity> one() {
        ValueLoadContext loadContext = createLoadContext();
        loadContext.getQuery().setMaxResults(1);
        return dataManager.loadValues(loadContext).singleOrEmpty()
                .switchIfEmpty(Mono.error(new IllegalStateException("No results")));
    }

    /**
     * Adds a key of a returned key-value pair. The sequence of adding properties must conform to the sequence of
     * result fields in the query "select" clause.
     * <p>For example, if the query is <code>select e.id, e.name from sample$Customer</code>
     * and you executed <code>property("customerId").property("customerName")</code>, the returned KeyValueEntity
     * will contain customer identifiers in "customerId" property and names in "customerName" property.
     */
    public RFluentValuesLoader property(String name) {
        properties.add(name);
        return this;
    }

    /**
     * The same as invoking {@link #property(String)} multiple times.
     */
    public RFluentValuesLoader properties(List<String> properties) {
        this.properties.clear();
        this.properties.addAll(properties);
        return this;
    }

    /**
     * The same as invoking {@link #property(String)} multiple times.
     */
    public RFluentValuesLoader properties(String... properties) {
        return properties(Arrays.asList(properties));
    }

    @Override
    public RFluentValuesLoader store(String store) {
        super.store(store);
        return this;
    }

    @Override
    public RFluentValuesLoader hint(String hintName, Serializable value) {
        super.hint(hintName, value);
        return this;
    }

    @Override
    public RFluentValuesLoader hints(Map<String, Serializable> hints) {
        super.hints(hints);
        return this;
    }

    @Override
    public RFluentValuesLoader parameter(String name, Object value) {
        super.parameter(name, value);
        return this;
    }

    @Override
    public RFluentValuesLoader parameter(String name, Date value, TemporalType temporalType) {
        super.parameter(name, value, temporalType);
        return this;
    }

    @Override
    public RFluentValuesLoader parameter(String name, Object value, boolean implicitConversion) {
        super.parameter(name, value, implicitConversion);
        return this;
    }

    @Override
    public RFluentValuesLoader setParameters(Map<String, Object> parameters) {
        super.setParameters(parameters);
        return this;
    }

    @Override
    public RFluentValuesLoader firstResult(int firstResult) {
        super.firstResult(firstResult);
        return this;
    }

    @Override
    public RFluentValuesLoader maxResults(int maxResults) {
        super.maxResults(maxResults);
        return this;
    }

    @Override
    public RFluentValuesLoader lockMode(LockModeType lockMode) {
        super.lockMode(lockMode);
        return this;
    }
}
