package com.ocmwdt.picwordsclient.gameclient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import com.ocmwdt.picwordsclient.exceptions.ClientException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 *
 * @author alexey.likhachev
 */
public class PicWordClientImpl implements PicWordClient {

    private static final int SCRIPT_TIMEOUT = 7;
    private static final int PAGE_LOAD_TIMEOUT = 7;
    private static final String MES_INPUT_PATH = "//div[@id='main']//input[@id='message']";
    private static final String SEND_BUTTON_PATH = "//div[@id='main']//input[@id='sendButton']";
    private static final String LAST_QUESTION_PATH = ".//*[@id='text']/span[@class='serverMessage' "
            + "and contains(.,'опрос:') and position()=last()]";

    private static Logger LOG = Logger.getLogger(PicWordClientImpl.class.getName());

    WebDriver driver;
    String site;

    public PicWordClientImpl(String site) {
        this.site = site;
        init();
    }

    @Override
    public void authorization(String login, String passw) throws ClientException {

    }

    @Override
    public void toGameTab() throws ClientException {
        driver.get(site);
    }

    @Override
    public void postMessage(String message) throws ClientException {
        WebElement messageInput = driver.findElement(By.xpath(MES_INPUT_PATH));
        WebElement sendButton = driver.findElement(By.xpath(SEND_BUTTON_PATH));
        messageInput.clear();
        messageInput.sendKeys(message);
        sendButton.click();
    }

    @Override
    public String getCurrentQuestion() {
        try {
            WebElement questionElement = driver.findElement(By.xpath(LAST_QUESTION_PATH));
            String text = trimQuestion(questionElement.getText());
            return text;
        } catch (NoSuchElementException nsee) {
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        if (driver != null) {
            driver.close();
        }
    }

    private void init() {
        driver = new FirefoxDriver();
        driver.manage().timeouts().
                pageLoadTimeout(PAGE_LOAD_TIMEOUT, TimeUnit.SECONDS).
                setScriptTimeout(SCRIPT_TIMEOUT, TimeUnit.SECONDS);
    }

    private String trimQuestion(String question) {
        String pureQuestion = question.replaceFirst("^.+опрос:", "");
        int lastBrace = pureQuestion.lastIndexOf('(');
        if (lastBrace > 0) {
            return pureQuestion.substring(0, lastBrace);
        }
        return pureQuestion;
    }

}
