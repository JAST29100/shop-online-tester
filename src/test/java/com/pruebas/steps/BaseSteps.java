package com.pruebas.steps;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class BaseSteps {
    protected WebDriver driver;

    protected void startWebDriver() {
        driver = new FirefoxDriver();
    }

    protected void stopWebDriver() {
        driver.quit();
    }

    protected void wait(int timeOutInSeconds) {
        try {
            Thread.sleep(timeOutInSeconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}