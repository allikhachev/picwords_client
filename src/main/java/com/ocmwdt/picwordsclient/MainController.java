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
import com.ocmwdt.picwordsclient.funclient.PoroshokuhodiRuClient;
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
            AnswerClient ansClient = new LoopyRuAnswerClient();
            FunClient poroshok = new PoroshokuhodiRuClient()) {

            if (user != null && passwd != null && !user.isEmpty() && !passwd.isEmpty()) {
                pwClient.authorization(user, passwd);
            }

            pwClient.toGameTab();

            String oldQuestion = null;
            Queue<String> answers = new ArrayDeque<>();

            int funWait = 0;
            for (;;) {
                // задержка между запросом вопроса
                TimeUnit.SECONDS.sleep(_getQuestionDelay);
                String question = pwClient.getCurrentQuestion();
                //проверка на отсутствующий вопрос, попытка перезауска
                if (question == null) {
                    try {
                        pwClient.startNewGame();
                    } catch (ClientException ce) {
                    }
                    continue;
                }
                //если вопрос не пустой, то проверяем его на новый/старый
                if (!Objects.equals(question, oldQuestion)) {
                    //если новый, то запоминаем вопрос, получаем список ответов
                    oldQuestion = question;
                    LOG.log(Level.INFO, "current question: {0}", question);

                    answers.clear();
                    answers.addAll(ansClient.getAnswers(question));

                    randomWaitBeforeFirstAnswer();
                } else {
                    //если старый, то пробуем вывести один из полученных ответов
                    if (!answers.isEmpty()) {
                        String answer = answers.poll();
                        pwClient.postMessage(answer);
                        LOG.log(Level.INFO, "posted answer: {0}", answer);
                    }
                }
                //если таймер веселого сообщения превысил порог, выводим веселое сообщение
                funWait++;
                if (funWait > MIN_FUN_DELAY) {
                    postSomeFun(pwClient, poroshok);
                    funWait = 0;
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

        if (getQuestionDelay != 0) {
            _getQuestionDelay = getQuestionDelay;
        } else {
            _getQuestionDelay = ACTIVITY_DELAY_SEC;
        }
        if (minWaitBeforeAnswer != 0) {
            _minWaitBeforeAnswer = minWaitBeforeAnswer;
        } else {
            _minWaitBeforeAnswer = MIN_WAIT_BEFORE_ANSWER_SEC;
        }
        if (waitBeforeAnswerRange != 0) {
            _waitBeforeAnswerRange = waitBeforeAnswerRange;
        } else {
            _waitBeforeAnswerRange = WAIT_BEFORE_ANSWER_RANGE_SEC;
        }
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
