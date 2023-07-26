package ru.udya.rdatamanager;

import io.jmix.core.DataManager;
import io.jmix.core.EntitySet;
import io.jmix.core.FluentLoader;
import io.jmix.core.FluentValueLoader;
import io.jmix.core.FluentValuesLoader;
import io.jmix.core.Id;
import io.jmix.core.LoadContext;
import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.ValueLoadContext;
import io.jmix.core.entity.KeyValueEntity;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RDataManager extends UnconstrainedRDataManager {

    /**
     * A convenience method that returns {@code UnconstrainedDataManager} that doesn't perform authorization.
     */
    UnconstrainedRDataManager unconstrained();

}
