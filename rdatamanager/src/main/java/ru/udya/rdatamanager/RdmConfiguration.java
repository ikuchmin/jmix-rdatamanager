package ru.udya.rdatamanager;

import io.jmix.core.DataStore;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.DataStoreFactory;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.eclipselink.impl.JpaDataStore;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.sys.ActionsConfiguration;
import io.jmix.flowui.sys.ViewControllersConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import ru.udya.rdatamanager.store.RDataStoreFactory;

import java.util.Collections;
import java.util.Map;


@EnableAsync
@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = {EclipselinkConfiguration.class, FlowuiConfiguration.class})
@PropertySource(name = "ru.udya.rdatamanager", value = "classpath:/ru/udya/rdatamanager/module.properties")
public class RdmConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean("rdm_RdmViewControllers")
    public ViewControllersConfiguration screens(AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        final ViewControllersConfiguration viewControllers = new ViewControllersConfiguration(applicationContext, metadataReaderFactory);
        viewControllers.setBasePackages(Collections.singletonList("ru.udya.rdatamanager"));
        return viewControllers;
    }

    @Bean("rdm_RdmActions")
    public ActionsConfiguration actions(AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        final ActionsConfiguration actions = new ActionsConfiguration(applicationContext, metadataReaderFactory);
        actions.setBasePackages(Collections.singletonList("ru.udya.rdatamanager"));
        return actions;
    }

    @Bean("rdm_RDataStoreFactory")
    @ConditionalOnMissingBean(RDataStoreFactory.class)
    public RDataStoreFactory rDataStoreFactory(DataStoreFactory dataStoreFactory) {
        Map<Class<? extends DataStore>, String> storeMapping =
                Map.of(JpaDataStore.class, "rdm_JpaDataStore");

        return new RDataStoreFactory(storeMapping, dataStoreFactory, applicationContext);
    }
}
