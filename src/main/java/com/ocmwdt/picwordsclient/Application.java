package com.ocmwdt.picwordsclient;

import com.ocmwdt.picwordsclient.answerclient.AnswerClient;
import com.ocmwdt.picwordsclient.answerclient.LoopyRuAnswerClient;
import com.ocmwdt.picwordsclient.exceptions.ClientException;
import com.ocmwdt.picwordsclient.gameclient.PicWordClient;
import com.ocmwdt.picwordsclient.gameclient.PicWordClientImpl;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Main application class.
 *
 * @author alexey.likhachev
 */
public class Application {

    private static final String URL = "http://picwords.ru";
    private static final int ACTIVITY_DELAY = 7;

    public static void main(String[] args) throws IOException, ClientException, InterruptedException {
        try (PicWordClient pwClient = new PicWordClientImpl(URL);
                AnswerClient ansClient = new LoopyRuAnswerClient()) {

            pwClient.authorization("some85one@mail.ru", "663399");

            pwClient.toGameTab();

            String testQuestion = pwClient.getCurrentQuestion();
            if (testQuestion == null) {
                pwClient.startNewGame();
            }

            for (int i = 0; i < 100; i++) {
                TimeUnit.SECONDS.sleep(ACTIVITY_DELAY);

                String question = pwClient.getCurrentQuestion();
                if (question == null) {
                    continue;
                }
                List<String> answers = ansClient.getAnswers(question);
                if (!answers.isEmpty()) {
                    pwClient.postMessage(answers.get(0));
                }
            }
        }
    }
}
