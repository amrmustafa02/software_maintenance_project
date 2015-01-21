/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.model;

import java.util.HashMap;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.unitime.timetable.model.base.BaseAcademicClassification;
import org.unitime.timetable.model.dao.AcademicClassificationDAO;




/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class AcademicClassification extends BaseAcademicClassification {
	private static final long serialVersionUID = 1L;

	private static HashMap academicClassifications = new HashMap(40);

/*[CONSTRUCTOR MARKER BEGIN]*/
	public AcademicClassification () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public AcademicClassification (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	/** Request Attribute name for Academic Classification **/
    public static final String ACAD_CLASS_REQUEST_ATTR = "academicClassifications";

	/**
	 * Retrieves all academic classifications in the database for the academic session
	 * ordered by name
	 * @param sessionId academic session
	 * @return Vector of AcademicClassification objects
	 */
 	public static List getAcademicClassificationList(Long sessionId) 
 			throws HibernateException {
	    
 	    AcademicClassificationDAO adao = new AcademicClassificationDAO();
	    Session hibSession = adao.getSession();
	    List l = hibSession.createQuery(
	    		"select c from AcademicClassification as c where c.session.uniqueId=:sessionId " +
	    		"order by c.name").
	    	setLong("sessionId",sessionId.longValue()).setCacheable(true).list();
		return l;
	}

    /**
     * Creates label of the format Name - Code
     * @return
     */
    public String getLabelNameCode() {
        return this.getName() + " - " + this.getCode();
    }

    /**
     * Creates label of the format Code - Name
     * @return
     */
    public String getLabelCodeName() {
        return this.getCode() + " - " + this.getName();
    }

	/**
	 * Load Academic Classifications
	 */
	public static void loadAcademicClassifications(Long sessionId) {
		
		List acadClasses = getAcademicClassificationList(sessionId);
		
		for(int i = 0; i < acadClasses.size(); i++) {
			AcademicClassification acadClass = 
				(AcademicClassification)acadClasses.get(i);
			String code = acadClass.getCode();
			academicClassifications.put(code, acadClass);
		}
	}

	/**
	 * Get the Academic Classification
	 * @param academicClass
	 */
	public static AcademicClassification getAcademicClassification(
			String academicClass) {
		
		return (AcademicClassification)academicClassifications
					.get(academicClass);
	}
	
	public Long getSessionId(){
		if (getSession() != null){
			return(getSession().getUniqueId());
		} else {
			return(null);
		}
	}
    
    public static AcademicClassification findByCode(Long sessionId, String code) {
    	return (findByCode(new AcademicClassificationDAO().getSession(), sessionId, code));
    }
    
    public static AcademicClassification findByCode(Session hibSession, Long sessionId, String code) {
        return (AcademicClassification)hibSession.
        createQuery(
                "select a from AcademicClassification a where "+
                "a.session.uniqueId=:sessionId and "+
                "a.code=:code").
         setLong("sessionId", sessionId.longValue()).
         setString("code", code).
         setCacheable(true).
         uniqueResult(); 
    }

    public static AcademicClassification findByExternalId(Long sessionId, String externalId) {
    	return(findByExternalId(new AcademicClassificationDAO().getSession(), sessionId, externalId));
    }

    public static AcademicClassification findByExternalId(Session hibSession, Long sessionId, String externalId) {
        return (AcademicClassification)hibSession.
        createQuery(
                "select a from AcademicClassification a where "+
                "a.session.uniqueId=:sessionId and "+
                "a.externalUniqueId=:externalUniqueId").
         setLong("sessionId", sessionId.longValue()).
         setString("externalUniqueId", externalId).
         setCacheable(true).
         uniqueResult(); 
    }

    public Object clone() {
    	AcademicClassification c = new AcademicClassification();
    	c.setExternalUniqueId(getExternalUniqueId());
    	c.setCode(getCode());
    	c.setName(getName());
    	return c;
    }
}
