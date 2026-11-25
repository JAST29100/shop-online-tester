package com.pruebas.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.pruebas.pages.CarritoPage;
import com.pruebas.pages.CatalogoPage;
import com.pruebas.pages.LoginPage;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CarritoSteps extends BaseSteps {
	private LoginPage loginPage;
	private CatalogoPage catalogo;
	private CarritoPage carrito;
	private Map<String, Integer> precios = new LinkedHashMap<String, Integer>();
	private Map<String, Integer> stocks = new LinkedHashMap<String, Integer>();
	private String productoActual;
	private int cantidadAnterior;
	private int subtotalAnterior;
	private int pedidosAntes;
	private int subtotalAntesConfirmar;
	private int totalAntesConfirmar;

	@Given("inicia una sesion de carrito con {string} y carrito vacio")
	public void iniciarSesionConCarritoVacio(String usuario) {
		startWebDriver();
		loginPage = new LoginPage(driver);
		loginPage.irALogin();
		loginPage.ingresarUser(usuario);
		loginPage.ingresarPass("ShopQA2026!");
		loginPage.seleccionarLogin();
		new WebDriverWait(driver, 10).until(ExpectedConditions.urlContains("/catalog"));
		catalogo = new CatalogoPage(driver);
		carrito = new CarritoPage(driver);
		leerCatalogo();
		catalogo.vaciarCarrito();
		assertTrue("El carrito debe iniciar vacio", carritoVacioEnRutaCarrito());
		catalogo.irAlCatalogo();
	}

	@Given("cambia a la cuenta {string} y vacia su carrito")
	public void cambiarCuenta(String usuario) {
		catalogo.irAlCatalogo();
		driver.findElement(By.cssSelector("form[action='/api/auth/logout'] button")).click();
		new WebDriverWait(driver, 10).until(ExpectedConditions.urlToBe("https://shoponline-app.vercel.app/"));
		loginPage.ingresarUser(usuario);
		loginPage.ingresarPass("ShopQA2026!");
		loginPage.seleccionarLogin();
		new WebDriverWait(driver, 10).until(ExpectedConditions.urlContains("/catalog"));
		leerCatalogo();
		catalogo.vaciarCarrito();
		assertTrue(carritoVacioEnRutaCarrito());
		catalogo.irAlCatalogo();
	}

	@Given("el carrito tiene una unidad valida de {string}")
	public void agregarUnaUnidad(String producto) { agregarAlCarrito(producto, 1); }

	@Given("el carrito tiene dos unidades validas de {string}")
	public void agregarDosUnidades(String producto) { agregarAlCarrito(producto, 2); }

	@When("cambia la cantidad de {string} a {string} y pulsa actualizar")
	public void cambiarCantidad(String producto, String cantidad) {
		capturarCantidadYSubtotal(producto);
		carrito.cambiarCantidad(producto, cantidad);
		productoActual = producto;
	}

	@When("cambia la cantidad de {string} a una unidad mas que sus existencias y pulsa actualizar")
	public void cambiarAStockMasUno(String producto) {
		capturarCantidadYSubtotal(producto);
		carrito.cambiarCantidad(producto, String.valueOf(stocks.get(producto) + 1));
		productoActual = producto;
	}

	@When("cambia la cantidad de {string} a todas sus existencias y pulsa actualizar")
	public void cambiarATodasExistencias(String producto) {
		capturarCantidadYSubtotal(producto);
		carrito.cambiarCantidad(producto, String.valueOf(stocks.get(producto)));
		productoActual = producto;
	}

	@Then("el carrito muestra el error {string}")
	public void validarError(String esperado) { assertEquals(esperado, carrito.obtenerAlerta()); }

	@Then("el carrito muestra el aviso {string}")
	public void validarAviso(String esperado) { assertEquals(esperado, carrito.obtenerAlerta()); }

	@Then("el carrito rechaza la cantidad por superar las existencias")
	public void validarErrorStock() {
		assertEquals("No hay existencias suficientes. Disponibles: " + stocks.get(productoActual) + ".", carrito.obtenerAlerta());
	}

	@Then("conserva la cantidad y subtotal previos de {string}")
	public void conservarValores(String producto) {
		assertEquals(cantidadAnterior, carrito.obtenerUnidadesResumen());
		assertEquals(cantidadAnterior * precios.get(producto), carrito.subtotalFila(producto));
		assertEquals(subtotalAnterior, carrito.subtotalFila(producto));
		assertEquals(subtotalAnterior, carrito.subtotalResumen());
		stopWebDriver();
	}

	@When("prepara el carrito con al menos veinte unidades y subtotal maximo inferior al minimo")
	public void preparaSubtotalMenor() { prepararCarritoMenorAlMinimo(); }

	@When("prepara el carrito con al menos veinte unidades y subtotal exacto de un millon")
	public void preparaSubtotalExacto() { prepararCarritoExactoUnMillon(); }

	@When("prepara el carrito con exactamente diecinueve unidades y subtotal minimo alcanzado")
	public void preparaDiecinueveUnidades() { prepararCarritoDiecinueveUnidades(); }

	@When("prepara el carrito con menos de veinte unidades y subtotal inferior al minimo")
	public void preparaNoElegible() { prepararCarritoNoElegible(); }

	@When("confirma el pedido del carrito")
	public void confirmaPedido() {
		carrito.irPedidos();
		pedidosAntes = carrito.cantidadPedidos();
		driver.navigate().back();
		carrito.esperarCarrito();
		subtotalAntesConfirmar = carrito.subtotalResumen();
		totalAntesConfirmar = carrito.totalResumen();
		carrito.confirmar();
	}

	@Then("el carrito informa el rechazo de minimo de compra")
	public void validarAlertaMinimoCompra() {
		assertFalse("Debe mostrarse un rechazo de negocio", carrito.obtenerAlerta().trim().isEmpty());
	}

	@Then("el carrito informa el rechazo de minimo de unidades")
	public void validarAlertaMinimoUnidades() {
		assertFalse("Debe mostrarse un rechazo de negocio", carrito.obtenerAlerta().trim().isEmpty());
	}

	@Then("no se registra pedido y se conserva el carrito")
	public void noPedidoYCarritoConservado() {
		Map<String, Integer> cantidades = leerCantidadesCarrito();
		Map<String, Integer> subtotales = leerSubtotalesCarrito();
		carrito.irPedidos();
		assertEquals(pedidosAntes, carrito.cantidadPedidos());
		driver.navigate().back();
		carrito.esperarCarrito();
		for (String producto : cantidades.keySet()) {
			assertEquals(cantidades.get(producto).intValue(), carrito.obtenerCantidadFila(producto));
			assertEquals(subtotales.get(producto).intValue(), carrito.subtotalFila(producto));
		}
		stopWebDriver();
	}

	@Then("el pedido queda registrado sin descuento y total igual al subtotal")
	public void pedidoSinDescuento() {
		assertEquals(1000000, subtotalAntesConfirmar);
		assertEquals(subtotalAntesConfirmar, totalAntesConfirmar);
		if (!carrito.url().contains("/orders")) carrito.irPedidos();
		assertEquals(pedidosAntes + 1, carrito.cantidadPedidos());
		assertEquals(1000000, carrito.totalUltimoPedido());
		stopWebDriver();
	}

	@Then("el resumen muestra descuento de {string} y total de {string}")
	public void validaDescuento(String descuento, String total) {
		assertEquals(1000000, subtotalAntesConfirmar);
		assertEquals(Integer.parseInt(descuento), subtotalAntesConfirmar - totalAntesConfirmar);
		assertEquals(Integer.parseInt(total), totalAntesConfirmar);
		String confirmacion = carrito.textoVisibleNumerico();
		if (carrito.url().contains("/cart")) assertTrue(confirmacion.contains("1000000"));
		assertTrue(confirmacion.contains(descuento));
		assertTrue(confirmacion.contains(total));
		stopWebDriver();
	}

	@Then("se muestra el beneficio disponible y no se registra pedido")
	public void beneficioSinPedido() {
		assertTrue(carrito.textoBeneficio().toLowerCase().contains("descuento"));
		carrito.irPedidos();
		assertEquals(pedidosAntes, carrito.cantidadPedidos());
		stopWebDriver();
	}

	@When("elimina el unico producto {string} del carrito")
	public void eliminarUnico(String producto) {
		productoActual = producto;
		carrito.irPedidos();
		pedidosAntes = carrito.cantidadPedidos();
		driver.navigate().back();
		carrito.esperarCarrito();
		carrito.eliminar(producto);
	}

	@Then("el carrito informa la eliminacion y queda sin productos")
	public void validarEliminacionVacia() {
		assertFalse(carrito.obtenerAlerta().trim().isEmpty());
		assertTrue(carrito.carritoVacio());
	}

	@Then("no permite confirmar un carrito vacio")
	public void noConfirmarVacio() {
		assertTrue(carrito.confirmarDisponible());
		carrito.confirmar();
		assertTrue("Debe rechazarse la confirmacion del carrito vacio", !carrito.obtenerAlerta().trim().isEmpty());
		assertTrue("Debe permanecer en el carrito", carrito.url().contains("/cart"));
		carrito.irPedidos();
		assertEquals(pedidosAntes, carrito.cantidadPedidos());
		stopWebDriver();
	}

	@Then("el producto {string} queda con cantidad {string} y subtotal recalculado")
	public void validaCantidadActualizada(String producto, String cantidad) {
		validaCantidadActualizada(producto, Integer.parseInt(cantidad));
	}

	@Then("el producto {string} queda con cantidad igual a sus existencias y subtotal recalculado")
	public void validaCantidadIgualExistencias(String producto) {
		validaCantidadActualizada(producto, stocks.get(producto));
	}

	@When("agrega dos productos distintos al carrito desde el catalogo")
	public void agregarDosProductos() {
		agregarAlCarrito("Teclado", 2);
		catalogo.irAlCatalogo();
		agregarAlCarrito("Mouse", 3);
	}

	@When("actualiza {string} a una cantidad valida y elimina {string} en el carrito")
	public void actualizarYEliiminar(String producto, String productoEliminar) {
		catalogo.irAlCarrito();
		capturarCantidadYSubtotal(producto);
		carrito.cambiarCantidad(producto, "4");
		assertEquals("Cantidad modificada correctamente.", carrito.obtenerAlerta());
		carrito.eliminar(productoEliminar);
	}

	@Then("solo queda {string} con la cantidad actualizada y el subtotal recalculado")
	public void validarE2E(String producto) {
		assertEquals(1, carrito.nombresFilas().size());
		assertEquals(producto, carrito.nombresFilas().get(0));
		assertEquals(4, carrito.obtenerCantidadFila(producto));
		assertEquals(4 * precios.get(producto), carrito.subtotalFila(producto));
		stopWebDriver();
	}

	private void agregarAlCarrito(String producto, int cantidad) {
		catalogo.irAlCatalogo();
		catalogo.ingresarCantidad(producto, String.valueOf(cantidad));
		catalogo.agregar(producto);
		assertEquals("Producto agregado correctamente.", catalogo.obtenerAviso());
		catalogo.irAlCarrito();
		carrito.esperarCarrito();
	}

	private void capturarCantidadYSubtotal(String producto) {
		productoActual = producto;
		cantidadAnterior = carrito.obtenerCantidadFila(producto);
		subtotalAnterior = carrito.subtotalFila(producto);
		stocks.put(producto, carrito.obtenerStock(producto));
		precios.put(producto, carrito.obtenerPrecio(producto));
	}

	private void validaCantidadActualizada(String producto, int cantidadEsperada) {
		wait(1);
		assertEquals(cantidadEsperada, carrito.obtenerCantidadFila(producto));
		assertEquals(cantidadEsperada, carrito.obtenerUnidadesResumen());
		assertEquals(cantidadEsperada * precios.get(producto), carrito.subtotalFila(producto));
		assertEquals(cantidadEsperada * precios.get(producto), carrito.subtotalResumen());
		stopWebDriver();
	}

	private void leerCatalogo() {
		precios.clear(); stocks.clear();
		precios.put("Teclado", catalogo.obtenerPrecioUnitario("Teclado"));
		precios.put("Mouse", catalogo.obtenerPrecioUnitario("Mouse"));
		precios.put("Monitor", catalogo.obtenerPrecioUnitario("Monitor"));
		stocks.put("Teclado", catalogo.obtenerExistencias("Teclado"));
		stocks.put("Mouse", catalogo.obtenerExistencias("Mouse"));
		stocks.put("Monitor", catalogo.obtenerExistencias("Monitor"));
	}

	private boolean carritoVacioEnRutaCarrito() {
		carrito.esperarCarrito();
		return carrito.carritoVacio();
	}

	private Map<String, Integer> leerCantidadesCarrito() {
		Map<String, Integer> datos = new LinkedHashMap<String, Integer>();
		for (String producto : carrito.nombresFilas()) datos.put(producto, carrito.obtenerCantidadFila(producto));
		return datos;
	}

	private Map<String, Integer> leerSubtotalesCarrito() {
		Map<String, Integer> datos = new LinkedHashMap<String, Integer>();
		for (String producto : carrito.nombresFilas()) datos.put(producto, carrito.subtotalFila(producto));
		return datos;
	}

	private void agregarCantidades(int teclados, int mouse, int monitores) {
		if (teclados > 0) agregarAlCarrito("Teclado", teclados);
		if (mouse > 0) agregarAlCarrito("Mouse", mouse);
		if (monitores > 0) agregarAlCarrito("Monitor", monitores);
		assertTrue("La preparacion debe incluir productos", carrito.nombresFilas().size() > 0);
	}

	private void prepararCarritoMenorAlMinimo() {
		int teclados = 19;
		int mouse = 1;
		assertTrue(stocks.get("Teclado") >= teclados && stocks.get("Mouse") >= mouse);
		assertTrue(teclados * precios.get("Teclado") + mouse * precios.get("Mouse") < 1000000);
		agregarCantidades(teclados, mouse, 0);
	}

	private void prepararCarritoExactoUnMillon() {
		int precioTeclado = precios.get("Teclado");
		int teclados = 1000000 / precioTeclado;
		assertEquals(0, 1000000 % precioTeclado);
		assertTrue(teclados >= 20 && teclados <= stocks.get("Teclado"));
		agregarCantidades(teclados, 0, 0);
	}

	private void prepararCarritoDiecinueveUnidades() {
		int precioTeclado = precios.get("Teclado");
		int diferenciaPrecio = precios.get("Monitor") - precioTeclado;
		int subtotalBase = 19 * precioTeclado;
		assertTrue(diferenciaPrecio > 0);
		int monitores = subtotalBase >= 1000000 ? 0
				: (1000000 - subtotalBase + diferenciaPrecio - 1) / diferenciaPrecio;
		assertTrue(monitores <= stocks.get("Monitor") && monitores <= 19);
		int teclados = 19 - monitores;
		assertTrue(teclados <= stocks.get("Teclado"));
		assertTrue(teclados * precioTeclado + monitores * precios.get("Monitor") >= 1000000);
		agregarCantidades(teclados, 0, monitores);
	}

	private void prepararCarritoNoElegible() {
		assertTrue(stocks.get("Teclado") > 0 && precios.get("Teclado") < 1000000);
		agregarCantidades(1, 0, 0);
	}
}
