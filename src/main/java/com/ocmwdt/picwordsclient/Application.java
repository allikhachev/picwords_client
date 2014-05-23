package com.ocmwdt.picwordsclient;

import com.ocmwdt.picwordsclient.answerclient.AnswerClient;
import com.ocmwdt.picwordsclient.answerclient.LoopyRuAnswerClient;
import com.ocmwdt.picwordsclient.exceptions.ClientException;
import com.ocmwdt.picwordsclient.gameclient.PicWordClient;
import com.ocmwdt.picwordsclient.gameclient.PicWordClientImpl;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

import com.ocmwdt.picwordsclient.funclient.FunClient;
import com.ocmwdt.picwordsclient.funclient.PoroshokuhodiRuClient;

/**
 * Game client controller.
 *
 * @author alexey.likhachev
 */
public class Application {

    private static final String URL = "http://picwords.ru";
    private static final String DEFAULT_PASSWORD = "663399";
    private static final String DEFAULT_EMAIL = "some85one@mail.ru";
    private static final int ACTIVITY_DELAY_SEC = 1;
    private static final long MIN_WAIT_BEFORE_ANSWER_SEC = 6;
    private static final long WAIT_BEFORE_ANSWER_RANGE_SEC = 5;
    private static final int MAX_FUN_DELAY = 120;
    private static final Random RANDOM_GENERATOR = new Random();

    public static void main(String[] args) throws IOException, ClientException, InterruptedException {
        String oldQuestion = null;
        Queue<String> answers = new ArrayDeque<>();

        initLogger();

        String email = DEFAULT_EMAIL;
        String password = DEFAULT_PASSWORD;
        if (args.length == 2) {
            email = args[0];
            password = args[1];
        }

        try (PicWordClient pwClient = new PicWordClientImpl(URL);
            AnswerClient ansClient = new LoopyRuAnswerClient();
            FunClient poroshok = new PoroshokuhodiRuClient()) {

            pwClient.authorization(email, password);

            pwClient.toGameTab();

            int funWait = 0;
            for (;;) {
                // задержка между запросом вопроса
                TimeUnit.SECONDS.sleep(ACTIVITY_DELAY_SEC);
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

                    answers.clear();
                    answers.addAll(ansClient.getAnswers(question));

                    randomWaitBeforeFirstAnswer();
                } else {
                    //если старый, то пробуем вывести один из полученных ответов
                    if (!answers.isEmpty()) {
                        String answer = answers.poll();
                        pwClient.postMessage(answer);
                    }
                }
                //если таймер веселого сообщения превысил порог, выводим веселое сообщение
                funWait++;
                if (funWait > MAX_FUN_DELAY) {
                    postSomeFun(pwClient, poroshok);
                    funWait = 0;
                }
                //end!!!
            }
        }
    }

    private static void randomWaitBeforeFirstAnswer() throws InterruptedException {
        long range = (long) RANDOM_GENERATOR.nextDouble() * WAIT_BEFORE_ANSWER_RANGE_SEC;

        TimeUnit.SECONDS.sleep(MIN_WAIT_BEFORE_ANSWER_SEC + range);
    }

    private static void postSomeFun(final PicWordClient pwClient, final FunClient poroshok) throws ClientException {
        List<String> funLines = poroshok.getNextFunnyText();
        for (String line : funLines) {
            pwClient.postMessage(line);
        }
    }

    private static void initLogger() {
        try {
            LogManager.getLogManager().readConfiguration(
                Application.class.getResourceAsStream("/META-INF/logging.properties"));
        } catch (IOException ioe) {
            System.err.println("Could not setup logger configuration: " + ioe.toString());
        }
    }
}
