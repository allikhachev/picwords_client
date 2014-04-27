package com.ocmwdt.picwordsclient.answerclient;

import java.io.Closeable;
import java.util.List;

/**
 *
 * @author alexey.likhachev
 */
public interface AnswerClient extends Closeable {

    /**
     * Returns all answers for the question.
     *
     * @param question
     * @return list of answers; empty if no answers
     */
    List<String> getAnswers(String question);
}
