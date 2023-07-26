package ru.udya.rdatamanager;

import io.jmix.core.AccessConstraintsRegistry;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.constraint.AccessConstraint;
import io.jmix.core.impl.DataManagerImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class RDataManagerImpl extends UnconstrainedRDataManagerImpl implements RDataManager {
    @Autowired
    protected AccessConstraintsRegistry accessConstraintsRegistry;
    @Autowired
    protected UnconstrainedRDataManager unconstrainedRDataManager;

    @Override
    public UnconstrainedRDataManager unconstrained() {
        return unconstrainedRDataManager;
    }

//    @Override
    protected List<AccessConstraint<?>> getAppliedConstraints() {
        return accessConstraintsRegistry.getConstraints();
    }
}
