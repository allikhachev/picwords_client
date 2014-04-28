package com.ocmwdt.picwordsclient.funclient;

import com.ocmwdt.picwordsclient.WebDriverUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Funny client for poroshokuhodi.
 *
 * @author alexey.likhachev
 */
public class PoroshokuhodiRuClient implements FunClient {

    private static final String SITE = "http://poroshokuhodi.ru";

    private static final String LINES_PATH = "//article[@class='hero clearfix center']/div/div";
    private static final String NEXT_PATH = "//article[@class='hero clearfix center']/p/a[text()='ТЫЦ']";

    private static final String SOME_TAG_REGEXP = "<[^<>]+>";

    private static Logger LOG = Logger.getLogger(PoroshokuhodiRuClient.class.getName());

    WebDriver driver;

    public PoroshokuhodiRuClient() {
        init();
    }

    @Override
    public void close() throws IOException {
        if (driver != null) {
            LOG.log(Level.INFO, PoroshokuhodiRuClient.class.getSimpleName() + " closed");
            driver.close();
        }
    }

    @Override
    public List<String> getNextFunnyText() {
        try {
            //get funny text
            List<String> lines = new ArrayList<>();
            List<WebElement> answerElements = driver.findElements(By.xpath(LINES_PATH));
            for (WebElement element : answerElements) {
                lines.add(element.getText().replaceAll(SOME_TAG_REGEXP, ""));
                LOG.log(Level.INFO, "text line: {0}", element.getText());
            }
            //go to next text block
            WebElement nextButton = driver.findElement(By.xpath(NEXT_PATH));
            nextButton.click();

            return lines;
        } catch (NoSuchElementException | TimeoutException ex) {
            LOG.log(Level.INFO, "no text block");
            return Collections.<String>emptyList();
        }
    }

    /**
     * initiates the client.
     */
    private void init() {
        driver = WebDriverUtil.getHtmlUnitDriver(true);
        driver.get(SITE);
        LOG.log(Level.INFO, PoroshokuhodiRuClient.class.getSimpleName() + " closed");
    }

}
