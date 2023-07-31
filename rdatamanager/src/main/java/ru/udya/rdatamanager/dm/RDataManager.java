package ru.udya.rdatamanager.dm;

public interface RDataManager extends UnconstrainedRDataManager {

    /**
     * A convenience method that returns {@code UnconstrainedDataManager} that doesn't perform authorization.
     */
    UnconstrainedRDataManager unconstrained();

}
