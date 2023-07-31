package ru.udya.rdatamanager.store;

import io.jmix.core.impl.TransactionManagerLocator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Component("rdm_JpaDataStore")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JpaRDataStore extends AbstractRDataStore implements RDataStore {


    public JpaRDataStore(TransactionManagerLocator transactionManagerLocator) {
        super(transactionManagerLocator);
    }
}
