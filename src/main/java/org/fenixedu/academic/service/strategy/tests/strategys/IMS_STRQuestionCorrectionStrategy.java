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
/*
 * Created on 23/Set/2004
 *
 */
package org.fenixedu.academic.service.strategy.tests.strategys;

import java.util.List;

import org.fenixedu.academic.dto.onlineTests.IStudentTestQuestion;
import org.fenixedu.academic.util.tests.CardinalityType;
import org.fenixedu.academic.util.tests.QuestionType;
import org.fenixedu.academic.util.tests.ResponseProcessing;
import org.fenixedu.academic.util.tests.ResponseSTR;

/**
 * @author Susana Fernandes
 * 
 */
public class IMS_STRQuestionCorrectionStrategy extends QuestionCorrectionStrategy {

    @Override
    public IStudentTestQuestion getMark(IStudentTestQuestion studentTestQuestion) {
        if ((studentTestQuestion.getSubQuestionByItem().getQuestionType().getType().intValue() == QuestionType.STR)
                || (studentTestQuestion.getSubQuestionByItem().getQuestionType().getType().intValue() == QuestionType.LID && (studentTestQuestion
                        .getSubQuestionByItem().getQuestionType().getCardinalityType().getType().intValue() == CardinalityType.SINGLE))) {
            List<ResponseProcessing> questionCorrectionList =
                    studentTestQuestion.getSubQuestionByItem().getResponseProcessingInstructions();
            for (ResponseProcessing responseProcessing : questionCorrectionList) {
                if (isCorrectSTR(responseProcessing.getResponseConditions(),
                        new String(((ResponseSTR) studentTestQuestion.getResponse()).getResponse()))) {
                    return setStudentTestQuestionResponse(studentTestQuestion, responseProcessing);
                }
            }
            ResponseProcessing responseProcessing =
                    getOtherResponseProcessing(studentTestQuestion.getSubQuestionByItem().getResponseProcessingInstructions());
            if (responseProcessing != null) {
                return setStudentTestQuestionResponse(studentTestQuestion, responseProcessing);
            }
        }
        studentTestQuestion.setTestQuestionMark(new Double(0));
        return studentTestQuestion;
    }

    private IStudentTestQuestion setStudentTestQuestionResponse(IStudentTestQuestion studentTestQuestion,
            ResponseProcessing responseProcessing) {
        studentTestQuestion.setTestQuestionMark(responseProcessing.getResponseValue());
        ResponseSTR r = (ResponseSTR) studentTestQuestion.getResponse();
        r.setResponseProcessingIndex(studentTestQuestion.getSubQuestionByItem().getResponseProcessingInstructions()
                .indexOf(responseProcessing));
        studentTestQuestion.setResponse(r);
        studentTestQuestion.getSubQuestionByItem().setNextItemId(responseProcessing.getNextItem());
        return studentTestQuestion;
    }
}