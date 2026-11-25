package com.pruebas.steps;

import static org.junit.Assert.assertEquals;

import com.pruebas.pages.LoginPage;

import io.cucumber.java.en.*;

public class LoginSteps extends BaseSteps {
	private LoginPage loginPage;
	
	@Given("esta en la pagina de login")
	public void esta_en_la_pagina_de_login() {
	    startWebDriver();
		loginPage = new LoginPage(driver);
		loginPage.irALogin();
	}
	
	@When("ingresa el usuario {string} y la contrasena {string}")
	public void ingresa_el_usuario_y_la_contrasena(String usuario, String clave) {
	    loginPage.ingresarUser(usuario);
	    loginPage.ingresarPass(clave);
	}
	
	@When("selecciona el boton Login")
	public void selecciona_el_boton_Login() {
		loginPage.seleccionarLogin();
	}
	
	@Then("el sistema muestra la pagina {string}")
	public void el_sistema_muestra_la_pagina(String url) {
		wait(2);
		assertEquals(loginPage.obtenerUrl(), url);
		stopWebDriver();
	}
	
	@Then("el sistema muestra el mensaje de error {string}")
	public void el_sistema_muestra_el_mensaje_de_error(String error_msg) {
		wait(1);
		assertEquals(loginPage.obtenerError(), error_msg);
		stopWebDriver();
	}
	
	@Then("el sistema muestra el pop-up {string}")
	public void el_sistema_muestra_el_mensaje_popup_de_error(String error_popup) {
		assertEquals(loginPage.obtenerPopUpUser(), error_popup);
		stopWebDriver();
	}
}
