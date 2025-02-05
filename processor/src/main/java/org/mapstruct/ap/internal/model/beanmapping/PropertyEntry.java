/*
 * Copyright MapStruct Authors.
 *
 * Licensed under the Apache License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.mapstruct.ap.internal.model.beanmapping;

import java.util.Arrays;

import org.mapstruct.ap.internal.model.common.Type;
import org.mapstruct.ap.internal.util.Strings;
import org.mapstruct.ap.internal.util.accessor.PresenceCheckAccessor;
import org.mapstruct.ap.internal.util.accessor.ReadAccessor;

/**
 * A PropertyEntry contains information on the name, readAccessor and presenceCheck (for source)
 * and return type of a property.
 */
public class PropertyEntry {

    private final String[] fullName;
    private final ReadAccessor readAccessor;
    private final PresenceCheckAccessor presenceChecker;
    private final Type type;

    /**
     * Constructor used to create {@link TargetReference} property entries from a mapping
     *
     * @param fullName
     * @param readAccessor
     * @param type
     */
    private PropertyEntry(String[] fullName, ReadAccessor readAccessor, PresenceCheckAccessor presenceChecker,
                          Type type) {
        this.fullName = fullName;
        this.readAccessor = readAccessor;
        this.presenceChecker = presenceChecker;
        this.type = type;
    }

    /**
     * Constructor used to create {@link SourceReference} property entries from a mapping
     *
     * @param name name of the property (dot separated)
     * @param readAccessor its read accessor
     * @param presenceChecker its presence Checker
     * @param type type of the property
     * @return the property entry for given parameters.
     */
    public static PropertyEntry forSourceReference(String[] name, ReadAccessor readAccessor,
                                                   PresenceCheckAccessor presenceChecker, Type type) {
        return new PropertyEntry( name, readAccessor, presenceChecker, type );
    }

    public String getName() {
        return fullName[fullName.length - 1];
    }

    public ReadAccessor getReadAccessor() {
        return readAccessor;
    }

    public PresenceCheckAccessor getPresenceChecker() {
        return presenceChecker;
    }

    public Type getType() {
        return type;
    }

    public String getFullName() {
        return Strings.join( Arrays.asList(  fullName ), "." );
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Arrays.deepHashCode( this.fullName );
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final PropertyEntry other = (PropertyEntry) obj;
        return Arrays.deepEquals( this.fullName, other.fullName );
    }

    @Override
    public String toString() {
        return type + " " + Strings.join( Arrays.asList( fullName ), "." );
    }
}
