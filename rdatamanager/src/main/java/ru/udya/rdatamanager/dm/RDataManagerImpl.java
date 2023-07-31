package ru.udya.rdatamanager.dm;

import io.jmix.core.AccessConstraintsRegistry;
import io.jmix.core.constraint.AccessConstraint;
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
