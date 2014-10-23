package com.ocmwdt.picwordsclient.gameclient;

import com.ocmwdt.picwordsclient.exceptions.ClientException;
import java.io.Closeable;

/**
 *
 * @author alexey.likhachev
 */
public interface PicWordClient extends Closeable {

    void startNewGame() throws ClientException;

    void authorization(String login, String passw) throws ClientException;

    void toGameTab() throws ClientException;

    void postMessage(String message) throws ClientException;

    /**
     * @param question
     * @return answer for the question or null if answer not found
     */
    String getRightAnswer(String question);

    String getCurrentQuestion();
}
