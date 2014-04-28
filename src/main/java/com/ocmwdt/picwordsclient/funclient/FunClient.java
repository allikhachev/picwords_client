package com.ocmwdt.picwordsclient.funclient;

import java.io.Closeable;
import java.util.List;

/**
 *
 * @author alexey.likhachev
 */
public interface FunClient extends Closeable {

    /**
     * @return list of funny strings
     */
    List<String> getNextFunnyText();
}
