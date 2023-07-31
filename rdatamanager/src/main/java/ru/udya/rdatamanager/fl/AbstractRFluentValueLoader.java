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

import io.jmix.core.TemporalValue;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.ValueLoadContext;
import io.jmix.core.constraint.AccessConstraint;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TemporalType;
import ru.udya.rdatamanager.dm.UnconstrainedRDataManager;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class AbstractRFluentValueLoader {

    protected UnconstrainedRDataManager dataManager;

    private boolean joinTransaction = true;
    private String store;
    private String queryString;
    private Map<String, Serializable> hints;
    private Map<String, Object> parameters = new HashMap<>();
    private Set<String> noConversionParams = new HashSet<>();
    private List<AccessConstraint<?>> accessConstraints;
    private int firstResult;
    private int maxResults;
    private LockModeType lockMode;

    AbstractRFluentValueLoader(String queryString) {
        this.queryString = queryString;
    }

    public void setDataManager(UnconstrainedRDataManager dataManager) {
        this.dataManager = dataManager;
    }

    protected ValueLoadContext createLoadContext() {
        ValueLoadContext loadContext = instantiateValueLoadContext();
        if (store != null)
            loadContext.setStoreName(store);

        loadContext.setHints(hints);

        ValueLoadContext.Query query = ValueLoadContext.createQuery(queryString);
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if (noConversionParams.contains(entry.getKey()))
                query.setParameter(entry.getKey(), entry.getValue(), false);
            else
                query.setParameter(entry.getKey(), entry.getValue());
        }
        loadContext.setQuery(query);

        loadContext.getQuery().setFirstResult(firstResult);
        loadContext.getQuery().setMaxResults(maxResults);
        loadContext.setAccessConstraints(accessConstraints);

        loadContext.setJoinTransaction(joinTransaction);
        loadContext.setLockMode(lockMode);

        return loadContext;
    }

    public AbstractRFluentValueLoader joinTransaction(boolean join) {
        this.joinTransaction = join;
        return this;
    }

    /**
     * Sets DataStore name.
     */
    public AbstractRFluentValueLoader store(String store) {
        this.store = store;
        return this;
    }

    /**
     * Sets custom hint that should be used by the query.
     */
    public AbstractRFluentValueLoader hint(String hintName, Serializable value) {
        if (this.hints == null) {
            this.hints = new HashMap<>();
        }
        this.hints.put(hintName, value);
        return this;
    }

    /**
     * Sets custom hints that should be used by the query.
     */
    public AbstractRFluentValueLoader hints(Map<String, Serializable> hints) {
        this.hints = hints;
        return this;
    }

    /**
     * Sets access constraints.
     */
    public AbstractRFluentValueLoader accessConstraints(List<AccessConstraint<?>> accessConstraints) {
        this.accessConstraints = accessConstraints;
        return this;
    }

    /**
     * Sets value for a query parameter.

     * @param name  parameter name
     * @param value parameter value
     */
    public AbstractRFluentValueLoader parameter(String name, Object value) {
        parameters.put(name, value);
        return this;
    }

    /**
     * Sets value for a parameter of {@code java.util.Date} type.

     * @param name  parameter name
     * @param value parameter value
     * @param temporalType  how to interpret the value
     */
    public AbstractRFluentValueLoader parameter(String name, Date value, TemporalType temporalType) {
        parameters.put(name, new TemporalValue(value, temporalType));
        return this;
    }

    /**
     * Sets value for a query parameter.

     * @param name  parameter name
     * @param value parameter value
     * @param implicitConversion whether to do parameter value conversions, e.g. convert an entity to its ID
     */
    public AbstractRFluentValueLoader parameter(String name, Object value, boolean implicitConversion) {
        parameters.put(name, value);
        if (!implicitConversion) {
            noConversionParams.add(name);
        }
        return this;
    }

    /**
     * Sets the map of query parameters.
     */
    public AbstractRFluentValueLoader setParameters(Map<String, Object> parameters) {
        this.parameters.putAll(parameters);
        return this;
    }

    /**
     * Sets results offset.
     */
    public AbstractRFluentValueLoader firstResult(int firstResult) {
        this.firstResult = firstResult;
        return this;
    }

    /**
     * Sets results limit.
     */
    public AbstractRFluentValueLoader maxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    /**
     * Sets a lock mode to be used when executing query.
     */
    public AbstractRFluentValueLoader lockMode(LockModeType lockMode) {
        this.lockMode = lockMode;
        return this;
    }

    protected ValueLoadContext instantiateValueLoadContext() {
        return new ValueLoadContext();
    }
}
