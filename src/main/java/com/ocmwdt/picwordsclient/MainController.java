package com.ocmwdt.picwordsclient;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ocmwdt.picwordsclient.answerclient.AnswerClient;
import com.ocmwdt.picwordsclient.answerclient.LoopyRuAnswerClient;
import com.ocmwdt.picwordsclient.exceptions.ClientException;
import com.ocmwdt.picwordsclient.funclient.FunClient;
import com.ocmwdt.picwordsclient.gameclient.PicWordClient;
import com.ocmwdt.picwordsclient.gameclient.PicWordClientImpl;

/**
 *
 * @author alexey.likhachev
 */
public class MainController implements GameController {

    private static final String PRE_DECORATOR = "<i>";
    private static final String POST_DECORATOR = "</i>";

    private static final Random RANDOM_GENERATOR = new Random();
    private static Logger LOG = Logger.getLogger(MainController.class.getName());

    private long _getQuestionDelay;
    private long _minWaitBeforeAnswer;
    private long _waitBeforeAnswerRange;

    @Override
    public void run() {
        run(null, null, 0, 0, 0);
    }

    @Override
    public void run(String user, String passwd, long getQuestionDelay,
            long minWaitBeforeAnswer, long waitBeforeAnswerRange) {

        setParams(getQuestionDelay, minWaitBeforeAnswer, waitBeforeAnswerRange);

        try (PicWordClient pwClient = new PicWordClientImpl(URL);
                AnswerClient ansClient = new LoopyRuAnswerClient()) {

            if (user != null && passwd != null && !user.isEmpty() && !passwd.isEmpty()) {
                pwClient.authorization(user, passwd);
            }

            pwClient.toGameTab();

            String oldQuestion = null;
            Queue<String> answers = new ArrayDeque<>();

            for (;;) {
                // delay before question getting
                TimeUnit.SECONDS.sleep(_getQuestionDelay);
                String question = pwClient.getCurrentQuestion();
                //if new queston is empty then try to restart game
                if (question == null) {
                    try {
                        pwClient.startNewGame();
                    } catch (ClientException ce) {
                    }
                    continue;
                }
                //if question is not empty and is new
                if (!Objects.equals(question, oldQuestion)) {
                    String rirghtAnswer = pwClient.getRightAnswer(oldQuestion);
                    LOG.log(Level.INFO, "right answer: {0}", rirghtAnswer);
                    //если новый, то запоминаем вопрос, получаем список ответов
                    oldQuestion = question;
                    LOG.log(Level.INFO, "current question: {0}", question);

                    answers.clear();
                    answers.addAll(ansClient.getAnswers(question));

                    randomWaitBeforeFirstAnswer();
                } else {
                    //if question is not empty and is old
                    if (!answers.isEmpty()) {
                        if (isAlone(pwClient)) {
                            continue;
                        }
                        String answer = answers.poll();
                        pwClient.postMessage(answer);
                        LOG.log(Level.INFO, "posted answer: {0}", answer);
                    }
                }
                //end!!!
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error on close client \n", ex);
        } catch (ClientException ex) {
            LOG.log(Level.SEVERE, "Error on some operation \n", ex);
        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, "Error on main loop \n", ex);
        }
    }

    private void setParams(long getQuestionDelay, long minWaitBeforeAnswer, long waitBeforeAnswerRange) {

        _getQuestionDelay = getQuestionDelay != 0 ? getQuestionDelay : ACTIVITY_DELAY_SEC;

        _minWaitBeforeAnswer = minWaitBeforeAnswer != 0 ? minWaitBeforeAnswer : MIN_WAIT_BEFORE_ANSWER_SEC;

        _waitBeforeAnswerRange = waitBeforeAnswerRange != 0 ? waitBeforeAnswerRange : WAIT_BEFORE_ANSWER_RANGE_SEC;

    }

    private boolean isAlone(PicWordClient pwClient) {
        int gamersAmount = pwClient.getAmountOfGamers();
        return gamersAmount == 1;
    }

    private void randomWaitBeforeFirstAnswer() throws InterruptedException {

        long range = (long) RANDOM_GENERATOR.nextDouble() * _waitBeforeAnswerRange;
        TimeUnit.SECONDS.sleep(_minWaitBeforeAnswer + range);
    }

    private void postSomeFun(final PicWordClient pwClient, final FunClient poroshok) throws ClientException {
        List<String> funLines = poroshok.getNextFunnyText();
        for (String line : funLines) {
            pwClient.postMessage(decorateString(line));
            LOG.log(Level.INFO, "posted message: {0}", line);
        }
    }

    private String decorateString(final String plainText) {
        return PRE_DECORATOR + plainText + POST_DECORATOR;
    }
}
