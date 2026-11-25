package com.pruebas.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage {
    WebDriver driver;
    
    By cajaUser = By.id("username");
    By cajaPass = By.id("password");
    By msjError = By.cssSelector("[role='alert']");
    By btnLogin = By.cssSelector("[type='submit']");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }
    
    public void irALogin() {
        driver.get("https://www.shoponline-app.vercel.app");
    }
    
    public void ingresarUser(String user) {
    	driver.findElement(cajaUser).sendKeys(user);
    }
    
    public void ingresarPass(String pass) {
    	driver.findElement(cajaPass).sendKeys(pass);
    }
    
    public void seleccionarLogin() {
    	driver.findElement(btnLogin).click();
    }
    
    public String obtenerError() {
    	return driver.findElement(msjError).getText();
    }
    
    public String obtenerPopUpUser() {
    	return driver.findElement(cajaUser).getAttribute("validationMessage");
    }
    
    public String obtenerUrl() {
    	return driver.getCurrentUrl();
    }
}