/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at license/ESCIDOC.LICENSE
 * or http://www.escidoc.de/license.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at license/ESCIDOC.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright 2006-2009 Fachinformationszentrum Karlsruhe Gesellschaft
 * fuer wissenschaftlich-technische Information mbH and Max-Planck-
 * Gesellschaft zur Foerderung der Wissenschaft e.V.
 * All rights reserved.  Use is subject to license terms.
 */
package de.escidoc.core.adm.business.admin;

import de.escidoc.core.common.business.fedora.resources.ResourceType;

import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

/**
 * Basic class for all singletons which contain all information about a running or
 * finished process.
 *
 * @author sche
 */
public abstract class AdminMethodStatus implements Map<ResourceType, Integer> {

    private Date completionDate = new Date();
    private boolean fillingComplete = false;
    private final Semaphore semaphore = new Semaphore(1);
    protected final Map<ResourceType, Integer> treeMap = new EnumMap<ResourceType, Integer>(ResourceType.class);

    protected boolean isFillingComplete() {
        return fillingComplete;
    }

    protected void setFillingComplete(boolean fillingComplete) {
        this.fillingComplete = fillingComplete;
    }

    /**
     * This method must be called if the admin method has been finished.
     */
    public void finishMethod() {
        completionDate = new Date();
        semaphore.release();
    }

    /**
     * Get the completion date of this process or null, if the process is still
     * running.
     *
     * @return completion date
     */
    public Date getCompletionDate() {
        return completionDate;
    }

    /**
     * Start a new admin method. The return value is true if the is no other
     * process running the same method at the moment.
     *
     * @return true if the method is allowed to be started
     */
    public boolean startMethod() {
        boolean result = false;

        if (semaphore.tryAcquire()) {
            completionDate = null;
            fillingComplete = false;
            result = true;
        }
        return result;
    }

    public boolean isEmpty() {
        return treeMap.isEmpty();
    }

    public boolean equals(Object o) {
        return treeMap.equals(o);
    }

    public int hashCode() {
        return treeMap.hashCode();
    }

    public void clear() {
        treeMap.clear();
    }

    public boolean containsKey(Object key) {
        return treeMap.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return treeMap.containsValue(value);
    }

    public Set<Map.Entry<ResourceType, Integer>> entrySet() {
        return treeMap.entrySet();
    }

    public Integer get(Object key) {
        return treeMap.get(key);
    }

    @Override
    public Integer put(ResourceType key, Integer value) {
        return this.treeMap.put(key, value);
    }

    public Set<ResourceType> keySet() {
        return treeMap.keySet();
    }

    public Integer remove(Object key) {
        return treeMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends ResourceType, ? extends Integer> m) {
        this.treeMap.putAll(m);
    }

    public int size() {
        return treeMap.size();
    }

    public Collection<Integer> values() {
        return treeMap.values();
    }
}
