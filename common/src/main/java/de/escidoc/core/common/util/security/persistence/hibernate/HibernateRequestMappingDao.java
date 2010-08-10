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
 * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
 * fuer wissenschaftlich-technische Information mbH and Max-Planck-
 * Gesellschaft zur Foerderung der Wissenschaft e.V.  
 * All rights reserved.  Use is subject to license terms.
 */
package de.escidoc.core.common.util.security.persistence.hibernate;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import de.escidoc.core.common.util.security.persistence.MethodMapping;
import de.escidoc.core.common.util.security.persistence.RequestMappingDaoInterface;

/**
 * Implementation of a request mapping data access objects using hibernate.
 * 
 * @author TTE
 * @spring.bean id="persistence.HibernateRequestMappingDao"
 */
public class HibernateRequestMappingDao extends HibernateDaoSupport
    implements RequestMappingDaoInterface {

    /**
     * Wrapper of setSessionFactory to enable bean stuff generation for this
     * bean.
     * 
     * @param requestMappingSessionFactory
     *            The sessionFactory to set.
     * @spring.property ref="common.SessionFactory"
     * 
     * @aa
     */
    public final void setRequestMappingSessionFactory(
        final SessionFactory requestMappingSessionFactory) {

        super.setSessionFactory(requestMappingSessionFactory);
    }

    // CHECKSTYLE:JAVADOC-OFF

    /**
     * See Interface for functional description.
     * 
     * The database tables used are <code>method_mappings</code> and
     * <code>invocation_mappings</code>.
     * 
     * 
     * @param className
     * @param methodName
     * @return
     * @see RequestMappingDaoInterface#retrieveMethodMappings(java.lang.String)
     * 
     * @aa
     */
    @SuppressWarnings("unchecked")
    public List<MethodMapping> retrieveMethodMappings(
        final String className, final String methodName) {

        if (methodName == null) {
            return null;
        }

        List<MethodMapping> methodMappings = null;
        try {
            DetachedCriteria criteria =
                DetachedCriteria.forClass(MethodMapping.class).add(
                    Restrictions.eq("className", className)).add(
                    Restrictions.eq("methodName", methodName)).addOrder(
                    Order.desc("execBefore"));
            methodMappings = getHibernateTemplate().findByCriteria(criteria);
        }
        catch (HibernateException e) {
            throw e;
        }

        // initialize the invocation mappings (as they are always needed but
        // middlegen does not allow to specify it as lazy="false" because it
        // does not store this option in the properties file.
        for (MethodMapping methodMapping : methodMappings) {
            getSession().lock(methodMapping, LockMode.NONE);
            getHibernateTemplate().initialize(
                methodMapping.getInvocationMappings());
        }
        return methodMappings;
    }
    // CHECKSTYLE:JAVADOC-ON

}
