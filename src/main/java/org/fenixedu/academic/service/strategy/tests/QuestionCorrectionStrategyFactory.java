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
package org.fenixedu.academic.service.strategy.tests;

import org.fenixedu.academic.dto.onlineTests.IStudentTestQuestion;
import org.fenixedu.academic.service.strategy.tests.strategys.FENIX_LIDQuestionCorrectionStrategy;
import org.fenixedu.academic.service.strategy.tests.strategys.FENIX_NUMQuestionCorrectionStrategy;
import org.fenixedu.academic.service.strategy.tests.strategys.FENIX_STRQuestionCorrectionStrategy;
import org.fenixedu.academic.service.strategy.tests.strategys.IMS_LIDQuestionCorrectionStrategy;
import org.fenixedu.academic.service.strategy.tests.strategys.IMS_NUMQuestionCorrectionStrategy;
import org.fenixedu.academic.service.strategy.tests.strategys.IMS_STRQuestionCorrectionStrategy;
import org.fenixedu.academic.service.strategy.tests.strategys.IQuestionCorrectionStrategy;
import org.fenixedu.academic.util.tests.CorrectionFormula;
import org.fenixedu.academic.util.tests.QuestionType;

/**
 * @author Susana Fernandes
 * 
 */
public class QuestionCorrectionStrategyFactory implements IQuestionCorrectionStrategyFactory {
    private static QuestionCorrectionStrategyFactory instance = null;

    private QuestionCorrectionStrategyFactory() {
    }

    public static synchronized QuestionCorrectionStrategyFactory getInstance() {
        if (instance == null) {
            instance = new QuestionCorrectionStrategyFactory();
        }
        return instance;
    }

    public static synchronized void resetInstance() {
        if (instance != null) {
            instance = null;
        }
    }

    @Override
    public IQuestionCorrectionStrategy getQuestionCorrectionStrategy(IStudentTestQuestion studentTestQuestion) {
        IQuestionCorrectionStrategy questionCorrectionStrategy = null;
        if (studentTestQuestion.getCorrectionFormula().getFormula().equals(Integer.valueOf(CorrectionFormula.IMS))) {
            if (studentTestQuestion.getSubQuestionByItem().getQuestionType().getType().intValue() == QuestionType.LID) {
                questionCorrectionStrategy = new IMS_LIDQuestionCorrectionStrategy();
            }
            if (studentTestQuestion.getSubQuestionByItem().getQuestionType().getType().intValue() == QuestionType.STR) {
                questionCorrectionStrategy = new IMS_STRQuestionCorrectionStrategy();
            }
            if (studentTestQuestion.getSubQuestionByItem().getQuestionType().getType().intValue() == QuestionType.NUM) {
                questionCorrectionStrategy = new IMS_NUMQuestionCorrectionStrategy();
            }
        } else if (studentTestQuestion.getCorrectionFormula().getFormula().equals(Integer.valueOf(CorrectionFormula.FENIX))) {
            if (studentTestQuestion.getSubQuestionByItem().getQuestionType().getType().intValue() == QuestionType.LID) {
                questionCorrectionStrategy = new FENIX_LIDQuestionCorrectionStrategy();
            }
            if (studentTestQuestion.getSubQuestionByItem().getQuestionType().getType().intValue() == QuestionType.STR) {
                questionCorrectionStrategy = new FENIX_STRQuestionCorrectionStrategy();
            }
            if (studentTestQuestion.getSubQuestionByItem().getQuestionType().getType().intValue() == QuestionType.NUM) {
                questionCorrectionStrategy = new FENIX_NUMQuestionCorrectionStrategy();
            }
        }
        return questionCorrectionStrategy;
    }
}