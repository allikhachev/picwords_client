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

    boolean IsAnswerRight(final String answer);

    void toGameTab() throws ClientException;

    void postMessage(String message) throws ClientException;
    
    String getCurrentQuestion();
}
