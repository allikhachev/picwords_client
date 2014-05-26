package com.ocmwdt.picwordsclient;

/**
 * @author alexey.likhachev
 */
public interface GameController extends Runnable {

    String URL = "http://picwords.ru";
    long ACTIVITY_DELAY_SEC = 1;
    long MIN_WAIT_BEFORE_ANSWER_SEC = 6;
    long WAIT_BEFORE_ANSWER_RANGE_SEC = 5;
    long MIN_FUN_DELAY = 120;

    /**
     * 
     * @param user login; may be null
     * @param passwd password; may be null
     * @param getQuestionDelay if 0 then using default
     * @param minWaitBeforeAnswer if 0 then using default
     * @param waitBeforeAnswerRange if 0 then using default
     */
    public void run(String user, String passwd, long getQuestionDelay,
        long minWaitBeforeAnswer, long waitBeforeAnswerRange);
}
