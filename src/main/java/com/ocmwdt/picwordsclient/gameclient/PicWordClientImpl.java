package com.ocmwdt.picwordsclient.gameclient;

import com.ocmwdt.picwordsclient.WebDriverUtil;
import java.io.IOException;
import java.util.logging.Logger;
import org.openqa.selenium.WebDriver;
import com.ocmwdt.picwordsclient.exceptions.ClientException;
import java.util.logging.Level;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 *
 * @author alexey.likhachev
 */
public class PicWordClientImpl implements PicWordClient {

    private static final String MES_INPUT_PATH = "//div[@id='main']//input[@id='message']";
    private static final String SEND_BUTTON_PATH = "//div[@id='main']//input[@id='sendButton']";
    private static final String LAST_QUESTION_PATH = ".//*[@id='text']/span[@class='serverMessage' "
            + "and contains(.,'опрос:') and position()=last()]";
    private static final String EMAIL_INPUT_PATH = "//*[@id='authEmail']";
    private static final String PASSW_INPUT_PATH = "//*[@id='authPassword']";
    private static final String LOGIN_BUTTON_PATH = "//*[@id='authButton']";

    private static final String TO_NEW_GAME_PATH = "//*[@id='newGame']";
    private static final String TO_GAME_TAB_PATH = "//*[@id='toGame']";
    private static final String TO_AUTH_TAB_PATH = "//*[@id='toAuth']";

    private static final String MSG_AUTH_EL_NOT_FOUND = "Some element is not found on the Authorization page";
    private static final String MSG_GAME_EL_NOT_FOUND = "Some element is not found on the Game page";

    private static Logger LOG = Logger.getLogger(PicWordClientImpl.class.getName());

    WebDriver driver;
    String site;

    public PicWordClientImpl(String site) {
        this.site = site;
        init();
    }

    @Override
    public void startNewGame() throws ClientException {
        goToTab(TO_NEW_GAME_PATH, "New Game tab not found!");
    }

    @Override
    public void authorization(final String login, final String passw) throws ClientException {
        LOG.log(Level.FINE, "Start authorization");
        toAuthTab();
        try {
            //set login
            WebElement email = driver.findElement(By.xpath(EMAIL_INPUT_PATH));
            email.clear();
            email.sendKeys(login);
            LOG.log(Level.FINE, "email: %0", email);
            //set password
            WebElement pasElement = driver.findElement(By.xpath(PASSW_INPUT_PATH));
            pasElement.clear();
            pasElement.sendKeys(passw);
            LOG.log(Level.FINE, "passw: %0", passw);
            //perform input
            WebElement loginButton = driver.findElement(By.xpath(LOGIN_BUTTON_PATH));
            loginButton.click();
        } catch (NoSuchElementException nsee) {
            LOG.log(Level.SEVERE, MSG_AUTH_EL_NOT_FOUND, nsee);
            throw new ClientException(MSG_AUTH_EL_NOT_FOUND, nsee);
        }
    }

    @Override
    public void toGameTab() throws ClientException {
        goToTab(TO_GAME_TAB_PATH, "Game tab not found!");
    }

    @Override
    public void postMessage(String message) throws ClientException {
        try {
            WebElement messageInput = driver.findElement(By.xpath(MES_INPUT_PATH));
            WebElement sendButton = driver.findElement(By.xpath(SEND_BUTTON_PATH));
            messageInput.clear();
            messageInput.sendKeys(message);
            sendButton.click();
            LOG.log(Level.FINE, "posted message: %0", message);
        } catch (NoSuchElementException nsee) {
            LOG.log(Level.SEVERE, MSG_GAME_EL_NOT_FOUND, nsee);
            throw new ClientException(MSG_GAME_EL_NOT_FOUND, nsee);
        }
    }

    @Override
    public String getCurrentQuestion() {
        try {
            WebElement questionElement = driver.findElement(By.xpath(LAST_QUESTION_PATH));
            String text = trimQuestion(questionElement.getText());
            LOG.log(Level.FINE, "current question: %0", text);
            return text;
        } catch (NoSuchElementException nsee) {
            LOG.log(Level.FINE, "question not found");
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        if (driver != null) {
            driver.close();
            LOG.log(Level.FINE, PicWordClientImpl.class.getSimpleName() + " closed");
        }
    }

    private void init() {
        driver = WebDriverUtil.getFirefoxDriver();
        driver.get(site);
        LOG.log(Level.FINE, PicWordClientImpl.class.getSimpleName() + " created");
    }

    private String trimQuestion(String question) {
        String pureQuestion = question.replaceFirst("^.+опрос:", "");
        int lastBrace = pureQuestion.lastIndexOf('(');
        if (lastBrace > 0) {
            return pureQuestion.substring(0, lastBrace);
        }
        return pureQuestion;
    }

    private void toAuthTab() throws ClientException {
        goToTab(TO_AUTH_TAB_PATH, "Authorization tab not found!");
    }

    private void goToTab(final String path, final String errMessage) throws ClientException {
        try {
            WebElement element = driver.findElement(By.xpath(path));
            element.click();
        } catch (NoSuchElementException nsee) {
            LOG.log(Level.SEVERE, errMessage, nsee);
            throw new ClientException(errMessage, nsee);
        }
    }
}
