/**
 * Copyright © 2002 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Core.
 *
 * FenixEdu Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Core.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.academic.service.services.teacher.onlineTests;

import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.onlineTests.Test;
import org.fenixedu.academic.domain.onlineTests.TestScope;
import org.fenixedu.academic.service.filter.ExecutionCourseLecturingTeacherAuthorizationFilter;
import org.fenixedu.academic.service.services.exceptions.NotAuthorizedException;

import pt.ist.fenixframework.Atomic;

public class InsertTest {

    protected String run(ExecutionCourse executionCourse, String title, String information) {
        TestScope testScope = executionCourse.getTestScope();
        if (testScope == null) {
            testScope = new TestScope(executionCourse);
        }
        Test test = new Test(title, information, testScope);
        return test.getExternalId();
    }

    // Service Invokers migrated from Berserk

    private static final InsertTest serviceInstance = new InsertTest();

    @Atomic
    public static String runInsertTest(ExecutionCourse executionCourse, String title, String information)
            throws NotAuthorizedException {
        ExecutionCourseLecturingTeacherAuthorizationFilter.instance.execute(executionCourse);
        return serviceInstance.run(executionCourse, title, information);
    }

}