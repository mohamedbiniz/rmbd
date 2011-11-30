/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.diagnosis.tree.exceptions;

import java.util.Collection;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: student99
 * Date: 10.08.2009
 * Time: 11:44:20
 * To change this template use File | Settings | File Templates.
 */
public class NonMinimalHittingSetException extends Exception {
    private final Collection<Set> diagnosis;

    public NonMinimalHittingSetException(Collection<Set> diagnosis) {
        super();
        this.diagnosis = diagnosis;
    }

    public Collection<Set> getHittingSet() {
        return this.diagnosis;
    }
}
