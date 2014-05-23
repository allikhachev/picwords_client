package com.ocmwdt.picwordsclient;

import java.util.concurrent.TimeUnit;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 *
 * @author alexey.likhachev
 */
public final class WebDriverUtil {

    private static final int SCRIPT_TIMEOUT = 15;
    private static final int PAGE_LOAD_TIMEOUT = 15;

    private WebDriverUtil() {
    }

    public static final WebDriver getFirefoxDriver() {
        FirefoxDriver driver = new FirefoxDriver();
        driver.manage().timeouts().
                pageLoadTimeout(PAGE_LOAD_TIMEOUT, TimeUnit.SECONDS).
                setScriptTimeout(SCRIPT_TIMEOUT, TimeUnit.SECONDS);
        return driver;
    }

    public static final WebDriver getHtmlUnitDriver(final boolean jsEnable) {

        HtmlUnitDriver driver = new HtmlUnitDriver();
        driver.manage().timeouts().
                pageLoadTimeout(PAGE_LOAD_TIMEOUT, TimeUnit.SECONDS).
                setScriptTimeout(SCRIPT_TIMEOUT, TimeUnit.SECONDS);
        driver.setJavascriptEnabled(jsEnable);
        return driver;
    }

}
