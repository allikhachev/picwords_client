package com.ocmwdt.picwordsclient;

import com.ocmwdt.picwordsclient.answerclient.AnswerClient;
import com.ocmwdt.picwordsclient.answerclient.LoopyRuAnswerClient;
import com.ocmwdt.picwordsclient.exceptions.ClientException;
import com.ocmwdt.picwordsclient.gameclient.PicWordClient;
import com.ocmwdt.picwordsclient.gameclient.PicWordClientImpl;
import java.io.IOException;
import java.util.List;
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
    private static final int ACTIVITY_DELAY = 7;
    private static final int MAX_ANSWERS = 7;
    private static final int MAX_MISS_COUNT = 6;

    public static void main(String[] args) throws IOException, ClientException, InterruptedException {
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

            int missCount = 0;

            for (;;) {
                TimeUnit.SECONDS.sleep(ACTIVITY_DELAY);

                String question = pwClient.getCurrentQuestion();
                if (question == null) {
                    try {
                        pwClient.startNewGame();
                    } catch (ClientException ce) {
                    }
                    continue;
                }

                List<String> answers = ansClient.getAnswers(question);
                if (!answers.isEmpty()) {
                    postAnswers(pwClient, answers);
                } else if (missCount > MAX_MISS_COUNT) {
                    postSomeFun(pwClient, poroshok);
                    missCount = 0;
                }
                missCount++;
            }
        }
    }

    private static void postAnswers(PicWordClient client, List<String> answers) throws ClientException {
        int i = 0;
        for (String answer : answers) {
            client.postMessage(answer);
            if (client.IsAnswerRight(answer) || i >= MAX_ANSWERS) {
                break;
            }
            i++;
        }
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
