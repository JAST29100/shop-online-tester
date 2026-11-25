package com.pruebas.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pruebas.pages.CatalogoPage;
import com.pruebas.pages.LoginPage;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CatalogoSteps extends BaseSteps {
	private LoginPage loginPage;
	private CatalogoPage catalogoPage;
	private int existencias;
	private int precioUnitario;

	@Given("inicia sesion con {string} y el carrito esta vacio")
	public void iniciaSesionConCarritoVacio(String usuario) {
		startWebDriver();
		loginPage = new LoginPage(driver);
		loginPage.irALogin();
		loginPage.ingresarUser(usuario);
		loginPage.ingresarPass("ShopQA2026!");
		loginPage.seleccionarLogin();
		wait(2);
		catalogoPage = new CatalogoPage(driver);
		existencias = catalogoPage.obtenerExistencias("Teclado");
		precioUnitario = catalogoPage.obtenerPrecioUnitario("Teclado");
		catalogoPage.vaciarCarrito();
		assertTrue("El carrito debe iniciar vacio", catalogoPage.carritoVacio());
		catalogoPage.irAlCatalogo();
	}

	@When("agrega {string} unidades de {string} al carrito")
	public void agregaUnidades(String cantidad, String producto) {
		seleccionarProducto(producto);
		catalogoPage.ingresarCantidad(producto, cantidad);
		catalogoPage.agregar(producto);
	}

	@When("agrega una unidad mas que las existencias de {string}")
	public void agregaUnaMasQueExistencias(String producto) {
		seleccionarProducto(producto);
		catalogoPage.ingresarCantidad(producto, String.valueOf(existencias + 1));
		catalogoPage.agregar(producto);
	}

	@When("agrega todas las existencias de {string} al carrito")
	public void agregaTodasLasExistencias(String producto) {
		seleccionarProducto(producto);
		catalogoPage.ingresarCantidad(producto, String.valueOf(existencias));
		catalogoPage.agregar(producto);
	}

	@When("agrega desde el catalogo una unidad mas que las existencias restantes de {string}")
	public void agregaUnaMasDeExistenciasAcumuladas(String producto) {
		seleccionarProducto(producto);
		catalogoPage.ingresarCantidad(producto, String.valueOf(existencias - 1));
		catalogoPage.agregar(producto);
	}

	@Given("agrega {string} unidades de {string} al carrito y valida que se agregue correctamente")
	public void preparaCarrito(String cantidad, String producto) {
		seleccionarProducto(producto);
		catalogoPage.ingresarCantidad(producto, cantidad);
		catalogoPage.agregar(producto);
		assertEquals("Producto agregado correctamente.", catalogoPage.obtenerAviso());
	}

	@Then("el catalogo muestra el aviso {string}")
	public void validaAviso(String aviso) {
		assertEquals(aviso, catalogoPage.obtenerAviso());
	}

	@Then("el catalogo indica que solo hay existencias disponibles")
	public void validaAvisoExistencias() {
		assertEquals(catalogoPage.obtenerAvisoExistencias(existencias), catalogoPage.obtenerAviso());
	}

	@Then("el carrito permanece vacio")
	public void validaCarritoVacio() {
		assertTrue("El carrito debe permanecer vacio", catalogoPage.carritoVacio());
		stopWebDriver();
	}

	@Then("el carrito muestra {string} con cantidad {string} y subtotal calculado")
	public void validaCarritoCantidadYSubtotal(String producto, String cantidadTexto) {
		validaCantidadYSubtotal(producto, Integer.parseInt(cantidadTexto));
	}

	@Then("el carrito muestra {string} con cantidad igual a las existencias y subtotal calculado")
	public void validaCarritoConExistencias(String producto) {
		validaCantidadYSubtotal(producto, existencias);
	}

	@Then("el carrito conserva {string} con cantidad {string} y subtotal calculado")
	public void validaCarritoConservado(String producto, String cantidadTexto) {
		wait(1);
		validaCantidadYSubtotal(producto, Integer.parseInt(cantidadTexto));
	}

	private void validaCantidadYSubtotal(String producto, int cantidadEsperada) {
		assertEquals(cantidadEsperada, catalogoPage.obtenerCantidadFila(producto));
		assertEquals(cantidadEsperada * precioUnitario, catalogoPage.obtenerSubtotalFila(producto));
		stopWebDriver();
	}

	private void seleccionarProducto(String producto) {
		existencias = catalogoPage.obtenerExistencias(producto);
		precioUnitario = catalogoPage.obtenerPrecioUnitario(producto);
	}
}
