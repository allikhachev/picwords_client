package com.ocmwdt.picwordsclient.gameclient;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.ocmwdt.picwordsclient.WebDriverUtil;
import com.ocmwdt.picwordsclient.exceptions.ClientException;

/**
 *
 * @author alexey.likhachev
 */
public class PicWordClientImpl implements PicWordClient {

    private static final String MES_INPUT_PATH = "//div[@class='row send-current-message']//input";
    private static final String SEND_BUTTON_PATH = "//div[@class='row send-current-message']//button";
    private static final String LAST_QUESTION_PATH = "//ul[@class='messageList']/li[@class='' "
            + "and contains(.,'опрос:') and position()=last()]";
    private static final String EMAIL_INPUT_PATH = "//input[@formcontrolname='email']";
    private static final String PASSW_INPUT_PATH = "//input[@formcontrolname='password']";
    private static final String LOGIN_BUTTON_PATH = "//button[@type='submit']";
    private static final String RIGHT_ANSWER_PATH = "//ul[@class='messageList']/li[@class='' "
            + "and contains(.,'опрос.') and contains(., 'ответ:')]";
    private static final String GAMERS_ROW_PATH = "//*[@id='userList']/tr[position()>1]";

    private static final String TO_NEW_GAME_PATH = "//a[@class='start-game']";
    private static final String TO_GAME_TAB_PATH = "//a[@class='start-game']";
    private static final String TO_AUTH_TAB_PATH = "//button[@routerlink='signin']";

    private static final String MSG_AUTH_EL_NOT_FOUND = "Some element is not found on the Authorization page";
    private static final String MSG_GAME_EL_NOT_FOUND = "Some element is not found on the Game page";

    private static final Logger LOG = Logger.getLogger(PicWordClientImpl.class.getName());

    WebDriver driver;
    String site;

    public PicWordClientImpl(String site) {
        this.site = site;
        init();
    }

    @Override
    public void close() throws IOException {
        if (driver != null) {
            driver.close();
            LOG.log(Level.INFO, "{0} closed", PicWordClientImpl.class.getSimpleName());
        }
    }

    @Override
    public void startNewGame() throws ClientException {
        goToTab(TO_NEW_GAME_PATH, "New Game tab not found!");
    }

    @Override
    public void authorization(final String login, final String passw) throws ClientException {
        LOG.log(Level.INFO, "Start authorization");
        toAuthTab();
        try {
            //set login
            WebElement email = driver.findElement(By.xpath(EMAIL_INPUT_PATH));
            email.clear();
            email.sendKeys(login);
            LOG.log(Level.INFO, "email: {0}", login);
            //set password
            WebElement pasElement = driver.findElement(By.xpath(PASSW_INPUT_PATH));
            pasElement.clear();
            pasElement.sendKeys(passw);
            LOG.log(Level.INFO, "passw: {0}", passw);
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
        } catch (NoSuchElementException nsee) {
            LOG.log(Level.SEVERE, MSG_GAME_EL_NOT_FOUND, nsee);
            throw new ClientException(MSG_GAME_EL_NOT_FOUND, nsee);
        }
    }

    @Override
    public String getRightAnswer(String question) {
        List<WebElement> elements = driver.findElements(By.xpath(RIGHT_ANSWER_PATH));
        String answer = elements.isEmpty() ? "" : elements.get(elements.size() - 1).getText();
        return trimAnswer(answer);
    }

    @Override
    public String getCurrentQuestion() {
        try {
            String text = driver.findElement(By.xpath(LAST_QUESTION_PATH)).getText();
            return trimQuestion(text);
        } catch (NoSuchElementException nsee) {
            LOG.log(Level.INFO, "question not found");
            return null;
        }
    }

    @Override
    public int getAmountOfGamers() {
        List<WebElement> gamers = driver.findElements(By.xpath(GAMERS_ROW_PATH));
        return gamers.size();
    }

    private void init() {
        driver = WebDriverUtil.getFirefoxDriver();
        driver.get(site);
        LOG.log(Level.INFO, "{0} created", PicWordClientImpl.class.getSimpleName());
    }

    private String trimQuestion(String question) {
        String pureQuestion = question.replaceFirst("^.+опрос:", "");
        int lastBrace = pureQuestion.lastIndexOf('(');
        if (lastBrace > 0) {
            return pureQuestion.substring(0, lastBrace);
        }
        return pureQuestion;
    }

    private String trimAnswer(String answer) {
        String pureAnswer = answer.replaceFirst("^.*:", "");
        return pureAnswer;
    }

    private void toAuthTab() throws ClientException {
        goToTab(TO_AUTH_TAB_PATH, "Authorization tab not found!");
    }

    private void goToTab(final String path, final String errMessage) throws ClientException {
        try {
            WebElement element = driver.findElement(By.xpath(path));
            element.click();
        } catch (NoSuchElementException | ElementNotVisibleException ex) {
            LOG.log(Level.SEVERE, errMessage, ex);
            throw new ClientException(errMessage, ex);
        }
    }
}
