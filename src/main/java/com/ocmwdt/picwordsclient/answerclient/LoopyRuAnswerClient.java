package com.ocmwdt.picwordsclient.answerclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Answer clent for loopy.ru.
 *
 * @author alexey.likhachev
 */
public class LoopyRuAnswerClient implements AnswerClient {

    private static final String SITE = "http://loopy.ru/";
    private static final int SCRIPT_TIMEOUT = 7;
    private static final int PAGE_LOAD_TIMEOUT = 7;
    private static final int ANSWER_LOAD_TIMEOUT = 3;

    private static final String QUESTION_ELEMENT_PATH = "//input[@id='d' and @type='text']";
    private static final String FIND_BUTTON_PATH = "//form[@class='cf']/div[@class='sb']/em/input[@type='submit']";
    private static final String ANSWERS_FOUND_PATH = "//div[@class='lft']/div[@class='cm' and contains(., 'Всего найдено')]/p";
    private static final String ANSWERS_PATH = "//div[@class='cm']/div[@class='cf']/div[contains(@class, 'wd') ]/h3/a";

    private static Logger LOG = Logger.getLogger(LoopyRuAnswerClient.class.getName());

    WebDriver driver;

    public LoopyRuAnswerClient() {
        init();
    }

    @Override
    public void close() throws IOException {
        if (driver != null) {
            driver.close();
        }
    }

    @Override
    public List<String> getAnswers(final String question) {
        if (question == null || question.isEmpty()) {
            return Collections.<String>emptyList();
        }
        try {
            //input question
            WebElement qeInput = driver.findElement(By.xpath(QUESTION_ELEMENT_PATH));
            qeInput.clear();
            qeInput.sendKeys(question);
            WebElement findButton = driver.findElement(By.xpath(FIND_BUTTON_PATH));
            findButton.click();
            //wait for answers finding
            new WebDriverWait(driver, ANSWER_LOAD_TIMEOUT).
                    until(ExpectedConditions.presenceOfElementLocated(By.xpath(ANSWERS_FOUND_PATH)));
            List<WebElement> answerElements = driver.findElements(By.xpath(ANSWERS_PATH));
            //
            List<String> answers = new ArrayList<>();
            for (WebElement element : answerElements) {
                answers.add(element.getText());
            }
            return answers;
        } catch (NoSuchElementException | TimeoutException ex) {
            return Collections.<String>emptyList();
        }
    }

    /**
     * initiates the client.
     */
    private void init() {
        driver = new FirefoxDriver();
        driver.manage().timeouts().
                pageLoadTimeout(PAGE_LOAD_TIMEOUT, TimeUnit.SECONDS).
                setScriptTimeout(SCRIPT_TIMEOUT, TimeUnit.SECONDS);
        driver.get(SITE);
    }

}
