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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.fenixedu.academic.domain.Attends;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.GradeScale;
import org.fenixedu.academic.domain.Mark;
import org.fenixedu.academic.domain.onlineTests.DistributedTest;
import org.fenixedu.academic.domain.onlineTests.OnlineTest;
import org.fenixedu.academic.domain.onlineTests.Question;
import org.fenixedu.academic.domain.onlineTests.StudentTestLog;
import org.fenixedu.academic.domain.onlineTests.StudentTestQuestion;
import org.fenixedu.academic.domain.onlineTests.utils.ParseSubQuestion;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.dto.InfoStudent;
import org.fenixedu.academic.service.filter.ExecutionCourseLecturingTeacherAuthorizationFilter;
import org.fenixedu.academic.service.services.exceptions.FenixServiceException;
import org.fenixedu.academic.service.services.exceptions.NotAuthorizedException;
import org.fenixedu.academic.service.strategy.tests.IQuestionCorrectionStrategyFactory;
import org.fenixedu.academic.service.strategy.tests.QuestionCorrectionStrategyFactory;
import org.fenixedu.academic.service.strategy.tests.strategys.IQuestionCorrectionStrategy;
import org.fenixedu.academic.util.Bundle;
import org.fenixedu.academic.util.EvaluationType;
import org.fenixedu.academic.util.tests.TestQuestionStudentsChangesType;
import org.fenixedu.academic.util.tests.TestType;
import org.fenixedu.bennu.core.i18n.BundleUtil;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

public class ChangeStudentTestQuestionValue {
    protected void run(String executionCourseId, String distributedTestId, Double newValue, String questionId, String studentId,
            TestQuestionStudentsChangesType studentsType) throws FenixServiceException {

        DistributedTest distributedTest = FenixFramework.getDomainObject(distributedTestId);
        Question question = distributedTest.findQuestionByOID(questionId);

        List<StudentTestQuestion> studentsTestQuestionList = new ArrayList<StudentTestQuestion>();
        if (studentsType.getType().intValue() == TestQuestionStudentsChangesType.THIS_STUDENT) {
            final Registration registration = FenixFramework.getDomainObject(studentId);
            studentsTestQuestionList.add(StudentTestQuestion.findStudentTestQuestion(question, registration, distributedTest));
        } else if (studentsType.getType().intValue() == TestQuestionStudentsChangesType.STUDENTS_FROM_TEST_VARIATION) {
            studentsTestQuestionList.addAll(StudentTestQuestion.findStudentTestQuestions(question, distributedTest));
        } else if (studentsType.getType().intValue() == TestQuestionStudentsChangesType.STUDENTS_FROM_TEST) {
            final Registration registration = FenixFramework.getDomainObject(studentId);
            final StudentTestQuestion studentTestQuestion = StudentTestQuestion.findStudentTestQuestion(question, registration, distributedTest);
            studentsTestQuestionList.addAll(distributedTest.findStudentTestQuestionsByTestQuestionOrder(studentTestQuestion.getTestQuestionOrder()));
        } else if (studentsType.getType().intValue() == TestQuestionStudentsChangesType.ALL_STUDENTS) {
            studentsTestQuestionList.addAll(question.getStudentTestsQuestionsSet());
        }
        for (StudentTestQuestion studentTestQuestion : studentsTestQuestionList) {
            List<InfoStudent> group = new ArrayList<InfoStudent>();

            if (!group.contains(studentTestQuestion.getStudent().getPerson())) {
                group.add(InfoStudent.newInfoFromDomain(studentTestQuestion.getStudent()));
            }

            if (studentTestQuestion.getResponse() != null
                    && studentTestQuestion.getDistributedTest().getTestType().equals(new TestType(TestType.EVALUATION))) {
                studentTestQuestion.setTestQuestionMark(getNewQuestionMark(studentTestQuestion, newValue));

                OnlineTest onlineTest = studentTestQuestion.getDistributedTest().getOnlineTest();
                ExecutionCourse executionCourse = FenixFramework.getDomainObject(executionCourseId);
                Attends attend = studentTestQuestion.getStudent().readAttendByExecutionCourse(executionCourse);
                Mark mark = onlineTest.getMarkByAttend(attend);
                if (mark != null) {
                    mark.setMark(getNewStudentMark(studentTestQuestion.getDistributedTest(), studentTestQuestion.getStudent()));
                }
            }
            studentTestQuestion.setTestQuestionValue(newValue);
           
            if (studentTestQuestion.getDistributedTest().getTestType().getType().equals(TestType.EVALUATION)) {
                Double maxiumEvaluationMark =
                        StudentTestQuestion.findStudentTestQuestions(studentTestQuestion.getStudent(), distributedTest).stream()
                                .map(stq -> stq.getTestQuestionValue()).reduce(0.0, Double::sum);
                if (!GradeScale.TYPE20.isValid(maxiumEvaluationMark.toString(), EvaluationType.ONLINE_TEST_TYPE)) {
                    throw new FenixServiceException("error.testDistribution.invalidMark");
                }
            }

            
            String event = BundleUtil.getString(Bundle.APPLICATION, "message.changeStudentValueLogMessage", newValue.toString());
            new StudentTestLog(studentTestQuestion.getDistributedTest(), studentTestQuestion.getStudent(), event, null);
        }
    }

    private String getNewStudentMark(DistributedTest dt, Registration s) {
        double totalMark = 0;
        Set<StudentTestQuestion> studentTestQuestionList = StudentTestQuestion.findStudentTestQuestions(s, dt);
        for (StudentTestQuestion studentTestQuestion : studentTestQuestionList) {
            totalMark += studentTestQuestion.getTestQuestionMark().doubleValue();
        }
        DecimalFormat df = new DecimalFormat("#0.##");
        DecimalFormatSymbols decimalFormatSymbols = df.getDecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(decimalFormatSymbols);
        return (df.format(Math.max(0, totalMark)));
    }

    private Double getNewQuestionMark(StudentTestQuestion studentTestQuestion, Double newValue) throws FenixServiceException {
        Double newMark = new Double(0);
        if (studentTestQuestion.getResponse() != null && !newValue.equals(Double.parseDouble("0"))) {
            if (studentTestQuestion.getTestQuestionValue().equals(Double.parseDouble("0"))) {
                ParseSubQuestion parse = new ParseSubQuestion();
                try {
                    studentTestQuestion = parse.parseStudentTestQuestion(studentTestQuestion);
                } catch (Exception e) {
                    throw new FenixServiceException(e);
                }
                IQuestionCorrectionStrategyFactory questionCorrectionStrategyFactory = QuestionCorrectionStrategyFactory.getInstance();
                IQuestionCorrectionStrategy questionCorrectionStrategy =
                        questionCorrectionStrategyFactory.getQuestionCorrectionStrategy(studentTestQuestion);
                studentTestQuestion = (StudentTestQuestion) questionCorrectionStrategy.getMark(studentTestQuestion);
                return studentTestQuestion.getTestQuestionMark();
            } else if (!studentTestQuestion.getTestQuestionMark().equals(Double.parseDouble("0"))) {
                newMark =
                        (newValue * studentTestQuestion.getTestQuestionMark()) * (java.lang.Math.pow(studentTestQuestion.getTestQuestionValue(), -1));
            }

        }
        return newMark;
    }

    // Service Invokers migrated from Berserk

    private static final ChangeStudentTestQuestionValue serviceInstance = new ChangeStudentTestQuestionValue();

    @Atomic
    public static void runChangeStudentTestQuestionValue(String executionCourseId, String distributedTestId, Double newValue, String questionId,
            String studentId, TestQuestionStudentsChangesType studentsType) throws FenixServiceException, NotAuthorizedException {
        ExecutionCourseLecturingTeacherAuthorizationFilter.instance.execute(executionCourseId);
        serviceInstance.run(executionCourseId, distributedTestId, newValue, questionId, studentId, studentsType);
    }

}