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
     * Returns current game question.
     */
    String getCurrentQuestion();

}
