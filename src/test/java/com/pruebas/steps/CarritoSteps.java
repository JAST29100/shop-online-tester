package com.pruebas.steps;

import com.pruebas.pages.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.openqa.selenium.By;

import io.cucumber.java.en.*;

public class CarritoSteps extends BaseSteps {
	LoginPage loginPage;
	CatalogoPage catalogo;
	CarritoPage carrito;
	
	String productoActual;
	int cantidadAnterior;
	int subtotalAnterior;
	int tecladoPresente;
	int mousePresente;
	int monitorPresente;
	int cantidadTeclado;
	int cantidadMouse;
	int cantidadMonitor;
	int subtotalTeclado;
	int subtotalMouse;
	int subtotalMonitor;
	int pedidosAntes;
	int subtotalAntesConfirmar;
	int totalAntesConfirmar;

	@Given("inicia una sesion de carrito con {string} y carrito vacio")
	public void iniciarSesionConCarritoVacio(String usuario) {
		startWebDriver();
		loginPage = new LoginPage(driver);
		loginPage.irALogin();
		loginPage.ingresarUser(usuario);
		loginPage.ingresarPass("ShopQA2026!");
		loginPage.seleccionarLogin();
		wait(2);
		catalogo = new CatalogoPage(driver);
		carrito = new CarritoPage(driver);
		catalogo.vaciarCarrito();
		assertTrue("El carrito debe iniciar vacio", carritoVacioEnRutaCarrito());
		catalogo.irAlCatalogo();
	}

	@Given("cambia a la cuenta {string} y vacia su carrito")
	public void cambiarCuenta(String usuario) {
		catalogo.irAlCatalogo();
		driver.findElement(By.cssSelector("form[action='/api/auth/logout'] button")).click();
		wait(1);
		loginPage.ingresarUser(usuario);
		loginPage.ingresarPass("ShopQA2026!");
		loginPage.seleccionarLogin();
		wait(2);
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
		carrito.cambiarCantidad(producto, String.valueOf(carrito.obtenerStock(producto) + 1));
		productoActual = producto;
	}

	@When("cambia la cantidad de {string} a todas sus existencias y pulsa actualizar")
	public void cambiarATodasExistencias(String producto) {
		capturarCantidadYSubtotal(producto);
		carrito.cambiarCantidad(producto, String.valueOf(carrito.obtenerStock(producto)));
		productoActual = producto;
	}

	@Then("el carrito muestra el error {string}")
	public void validarError(String esperado) { assertEquals(esperado, carrito.obtenerAlerta()); }

	@Then("el carrito muestra el aviso {string}")
	public void validarAviso(String esperado) { assertEquals(esperado, carrito.obtenerAlerta()); }

	@Then("el carrito rechaza la cantidad por superar las existencias")
	public void validarErrorStock() {
		assertEquals("No hay existencias suficientes. Disponibles: " + carrito.obtenerStock(productoActual) + ".", carrito.obtenerAlerta());
	}

	@Then("conserva la cantidad y subtotal previos de {string}")
	public void conservarValores(String producto) {
		assertEquals(cantidadAnterior, carrito.obtenerUnidadesResumen());
		assertEquals(cantidadAnterior * carrito.obtenerPrecio(producto), carrito.subtotalFila(producto));
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
		wait(1);
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
		guardarEstadoCarrito();
		carrito.irPedidos();
		assertEquals(pedidosAntes, carrito.cantidadPedidos());
		driver.navigate().back();
		wait(1);
		validarFilaConservada("Teclado", tecladoPresente, cantidadTeclado, subtotalTeclado);
		validarFilaConservada("Mouse", mousePresente, cantidadMouse, subtotalMouse);
		validarFilaConservada("Monitor", monitorPresente, cantidadMonitor, subtotalMonitor);
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
		wait(1);
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
		validaCantidadActualizada(producto, carrito.obtenerStock(producto));
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
		assertEquals(4 * carrito.obtenerPrecio(producto), carrito.subtotalFila(producto));
		stopWebDriver();
	}

	private void agregarAlCarrito(String producto, int cantidad) {
		catalogo.irAlCatalogo();
		catalogo.ingresarCantidad(producto, String.valueOf(cantidad));
		catalogo.agregar(producto);
		assertEquals("Producto agregado correctamente.", catalogo.obtenerAviso());
		catalogo.irAlCarrito();
		wait(1);
	}

	private void capturarCantidadYSubtotal(String producto) {
		productoActual = producto;
		cantidadAnterior = carrito.obtenerCantidadFila(producto);
		subtotalAnterior = carrito.subtotalFila(producto);
	}

	private void validaCantidadActualizada(String producto, int cantidadEsperada) {
		wait(1);
		assertEquals(cantidadEsperada, carrito.obtenerCantidadFila(producto));
		assertEquals(cantidadEsperada, carrito.obtenerUnidadesResumen());
		assertEquals(cantidadEsperada * carrito.obtenerPrecio(producto), carrito.subtotalFila(producto));
		assertEquals(cantidadEsperada * carrito.obtenerPrecio(producto), carrito.subtotalResumen());
		stopWebDriver();
	}

	private boolean carritoVacioEnRutaCarrito() {
		wait(1);
		return carrito.carritoVacio();
	}

	private void guardarEstadoCarrito() {
		tecladoPresente = carrito.nombresFilas().contains("Teclado") ? 1 : 0;
		mousePresente = carrito.nombresFilas().contains("Mouse") ? 1 : 0;
		monitorPresente = carrito.nombresFilas().contains("Monitor") ? 1 : 0;
		if (tecladoPresente == 1) {
			cantidadTeclado = carrito.obtenerCantidadFila("Teclado");
			subtotalTeclado = carrito.subtotalFila("Teclado");
		}
		if (mousePresente == 1) {
			cantidadMouse = carrito.obtenerCantidadFila("Mouse");
			subtotalMouse = carrito.subtotalFila("Mouse");
		}
		if (monitorPresente == 1) {
			cantidadMonitor = carrito.obtenerCantidadFila("Monitor");
			subtotalMonitor = carrito.subtotalFila("Monitor");
		}
	}

	private void validarFilaConservada(String producto, int estabaPresente, int cantidad, int subtotal) {
		if (estabaPresente == 1) {
			assertEquals(cantidad, carrito.obtenerCantidadFila(producto));
			assertEquals(subtotal, carrito.subtotalFila(producto));
		} else {
			assertFalse(carrito.nombresFilas().contains(producto));
		}
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
		int precioTeclado = catalogo.obtenerPrecioUnitario("Teclado");
		int precioMouse = catalogo.obtenerPrecioUnitario("Mouse");
		assertTrue(catalogo.obtenerExistencias("Teclado") >= teclados && catalogo.obtenerExistencias("Mouse") >= mouse);
		assertTrue(teclados * precioTeclado + mouse * precioMouse < 1000000);
		agregarCantidades(teclados, mouse, 0);
	}

	private void prepararCarritoExactoUnMillon() {
		int precioTeclado = catalogo.obtenerPrecioUnitario("Teclado");
		int teclados = 1000000 / precioTeclado;
		assertEquals(0, 1000000 % precioTeclado);
		assertTrue(teclados >= 20 && teclados <= catalogo.obtenerExistencias("Teclado"));
		agregarCantidades(teclados, 0, 0);
	}

	private void prepararCarritoDiecinueveUnidades() {
		int precioTeclado = catalogo.obtenerPrecioUnitario("Teclado");
		int precioMonitor = catalogo.obtenerPrecioUnitario("Monitor");
		int diferenciaPrecio = precioMonitor - precioTeclado;
		int subtotalBase = 19 * precioTeclado;
		assertTrue(diferenciaPrecio > 0);
		int monitores = subtotalBase >= 1000000 ? 0
				: (1000000 - subtotalBase + diferenciaPrecio - 1) / diferenciaPrecio;
		assertTrue(monitores <= catalogo.obtenerExistencias("Monitor") && monitores <= 19);
		int teclados = 19 - monitores;
		assertTrue(teclados <= catalogo.obtenerExistencias("Teclado"));
		assertTrue(teclados * precioTeclado + monitores * precioMonitor >= 1000000);
		agregarCantidades(teclados, 0, monitores);
	}

	private void prepararCarritoNoElegible() {
		assertTrue(catalogo.obtenerExistencias("Teclado") > 0 && catalogo.obtenerPrecioUnitario("Teclado") < 1000000);
		agregarCantidades(1, 0, 0);
	}
}
