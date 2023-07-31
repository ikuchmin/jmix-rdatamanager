package ru.udya.rdatamanager.store;

import io.jmix.core.DataStore;
import io.jmix.core.impl.DataStoreFactory;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("JmixInternalElementUsage")
public class RDataStoreFactory {

    protected Map<String, RDataStore> rDataStores = new ConcurrentHashMap<>();

    protected Map<Class<? extends DataStore>, String> storeMapping;
    protected DataStoreFactory dataStoreFactory;

    protected ApplicationContext applicationContext;

    public RDataStoreFactory(Map<Class<? extends DataStore>, String> storeMapping,
                             DataStoreFactory dataStoreFactory, ApplicationContext applicationContext) {
        this.storeMapping = storeMapping;
        this.dataStoreFactory = dataStoreFactory;
        this.applicationContext = applicationContext;
    }

    public RDataStore get(String name) {
        var dataStore = dataStoreFactory.get(name);
        var beanName = storeMapping.get(dataStore.getClass());

        return rDataStores.computeIfAbsent(name, n -> {
            RDataStore rDataStore = (RDataStore) applicationContext.getBean(beanName);
            rDataStore.wrapDelegate(dataStore);

            return rDataStore;
        });
    }
}
