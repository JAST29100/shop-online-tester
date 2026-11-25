package com.pruebas.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.openqa.selenium.By;

import com.pruebas.pages.*;

import io.cucumber.java.en.*;

public class CarritoSteps extends BaseSteps {
	LoginPage loginPage;
	CatalogoPage catalogoPage;
	CarritoPage carritoPage;

	String productoActual;
	int cantidadAnterior;
	int subtotalAnterior;
	int pedidosAntes;
	int subtotalAntesConfirmar;
	int totalAntesConfirmar;
	
	String[] productosBase = { "Teclado", "Mouse", "Monitor" };
	int[] cantidadesGuardadas = new int[3];
	int[] subtotalesGuardados = new int[3];

	@Given("inicia una sesion de carrito con {string} y carrito vacio")
	public void iniciarSesionConCarritoVacio(String usuario) {
		startWebDriver();
		loginPage = new LoginPage(driver);
		loginPage.irALogin();
		loginPage.ingresarUser(usuario);
		loginPage.ingresarPass("ShopQA2026!");
		loginPage.seleccionarLogin();
		wait(2);
		catalogoPage = new CatalogoPage(driver);
		carritoPage = new CarritoPage(driver);
		catalogoPage.vaciarCarrito();
		assertTrue("El carritoPage debe iniciar vacio", carritoPage.carritoVacio());
		catalogoPage.irAlCatalogo();
	}

	@Given("cambia a la cuenta {string} y vacia su carrito")
	public void cambiarCuenta(String usuario) {
		catalogoPage.irAlCatalogo();
		driver.findElement(By.cssSelector("form[action='/api/auth/logout'] button")).click();
		wait(1);
		loginPage.ingresarUser(usuario);
		loginPage.ingresarPass("ShopQA2026!");
		loginPage.seleccionarLogin();
		wait(2);
		catalogoPage.vaciarCarrito();
		assertTrue(carritoPage.carritoVacio());
		catalogoPage.irAlCatalogo();
	}

	@Given("el carrito tiene una unidad valida de {string}")
	public void agregarUnaUnidad(String producto) {
		agregarAlCarrito(producto, 1);
	}

	@Given("el carrito tiene dos unidades validas de {string}")
	public void agregarDosUnidades(String producto) {
		agregarAlCarrito(producto, 2);
	}

	@When("cambia la cantidad de {string} a {string} y pulsa actualizar")
	public void cambiarCantidad(String producto, String cantidad) {
		capturarCantidadYSubtotal(producto);
		carritoPage.cambiarCantidad(producto, cantidad);
		productoActual = producto;
	}

	@When("cambia la cantidad de {string} a una unidad mas que sus existencias y pulsa actualizar")
	public void cambiarAStockMasUno(String producto) {
		capturarCantidadYSubtotal(producto);
		carritoPage.cambiarCantidad(producto, String.valueOf(carritoPage.obtenerStock(producto) + 1));
		productoActual = producto;
	}

	@When("cambia la cantidad de {string} a todas sus existencias y pulsa actualizar")
	public void cambiarATodasExistencias(String producto) {
		capturarCantidadYSubtotal(producto);
		carritoPage.cambiarCantidad(producto, String.valueOf(carritoPage.obtenerStock(producto)));
		productoActual = producto;
	}

	@Then("el carrito muestra el error {string}")
	public void validarError(String esperado) {
		assertEquals(esperado, carritoPage.obtenerAlerta());
	}

	@Then("el carrito muestra el aviso {string}")
	public void validarAviso(String esperado) {
		assertEquals(esperado, carritoPage.obtenerAlerta());
	}

	@Then("el carrito rechaza la cantidad por superar las existencias")
	public void validarErrorStock() {
		assertEquals("No hay existencias suficientes. Disponibles: " + carritoPage.obtenerStock(productoActual) + ".",
				carritoPage.obtenerAlerta());
	}

	@Then("conserva la cantidad y subtotal previos de {string}")
	public void conservarValores(String producto) {
		assertEquals(cantidadAnterior, carritoPage.obtenerUnidadesResumen());
		assertEquals(cantidadAnterior * carritoPage.obtenerPrecio(producto), carritoPage.subtotalFila(producto));
		assertEquals(subtotalAnterior, carritoPage.subtotalFila(producto));
		assertEquals(subtotalAnterior, carritoPage.subtotalResumen());
		stopWebDriver();
	}

	@When("prepara el carrito con al menos veinte unidades y subtotal maximo inferior al minimo")
	public void preparaSubtotalMenor() {
		int teclados = 19;
		int mouse = 1;
		int precioTeclado = catalogoPage.obtenerPrecioUnitario("Teclado");
		int precioMouse = catalogoPage.obtenerPrecioUnitario("Mouse");
		assertTrue(catalogoPage.obtenerExistencias("Teclado") >= teclados && catalogoPage.obtenerExistencias("Mouse") >= mouse);
		assertTrue(teclados * precioTeclado + mouse * precioMouse < 1000000);
		agregarCantidades(teclados, mouse, 0);
	}

	@When("prepara el carrito con al menos veinte unidades y subtotal exacto de un millon")
	public void preparaSubtotalExacto() {
		int precioTeclado = catalogoPage.obtenerPrecioUnitario("Teclado");
		int teclados = 1000000 / precioTeclado;
		assertEquals(0, 1000000 % precioTeclado);
		assertTrue(teclados >= 20 && teclados <= catalogoPage.obtenerExistencias("Teclado"));
		agregarCantidades(teclados, 0, 0);
	}

	@When("prepara el carrito con exactamente diecinueve unidades y subtotal minimo alcanzado")
	public void preparaDiecinueveUnidades() {
		int precioTeclado = catalogoPage.obtenerPrecioUnitario("Teclado");
		int precioMonitor = catalogoPage.obtenerPrecioUnitario("Monitor");
		int diferencia = precioMonitor - precioTeclado;
		int subtotalBase = 19 * precioTeclado;
		int monitores = subtotalBase >= 1000000 ? 0 : (1000000 - subtotalBase + diferencia - 1) / diferencia;
		int teclados = 19 - monitores;
		assertTrue(monitores <= catalogoPage.obtenerExistencias("Monitor") && teclados <= catalogoPage.obtenerExistencias("Teclado"));
		assertTrue(teclados * precioTeclado + monitores * precioMonitor >= 1000000);
		agregarCantidades(teclados, 0, monitores);
	}

	@When("prepara el carrito con menos de veinte unidades y subtotal inferior al minimo")
	public void preparaNoElegible() {
		assertTrue(catalogoPage.obtenerExistencias("Teclado") > 0 && catalogoPage.obtenerPrecioUnitario("Teclado") < 1000000);
		agregarCantidades(1, 0, 0);
	}

	@When("confirma el pedido del carrito")
	public void confirmaPedido() {
		carritoPage.irPedidos();
		pedidosAntes = carritoPage.cantidadPedidos();
		driver.navigate().back();
		wait(1);
		subtotalAntesConfirmar = carritoPage.subtotalResumen();
		totalAntesConfirmar = carritoPage.totalResumen();
		carritoPage.confirmar();
	}

	@Then("el carrito informa el rechazo de minimo de compra")
	public void validarAlertaMinimoCompra() {
		assertFalse("Debe mostrarse un rechazo de negocio", carritoPage.obtenerAlerta().trim().isEmpty());
	}

	@Then("el carrito informa el rechazo de minimo de unidades")
	public void validarAlertaMinimoUnidades() {
		assertFalse("Debe mostrarse un rechazo de negocio", carritoPage.obtenerAlerta().trim().isEmpty());
	}

	@Then("no se registra pedido y se conserva el carrito")
	public void noPedidoYCarritoConservado() {
		for (int i = 0; i < productosBase.length; i++) {
			boolean existe = carritoPage.nombresFilas().contains(productosBase[i]);
			cantidadesGuardadas[i] = existe ? carritoPage.obtenerCantidadFila(productosBase[i]) : 0;
			subtotalesGuardados[i] = existe ? carritoPage.subtotalFila(productosBase[i]) : 0;
		}
		carritoPage.irPedidos();
		assertEquals(pedidosAntes, carritoPage.cantidadPedidos());
		driver.navigate().back();
		wait(1);
		for (int i = 0; i < productosBase.length; i++) {
			if (cantidadesGuardadas[i] > 0) {
				assertEquals(cantidadesGuardadas[i], carritoPage.obtenerCantidadFila(productosBase[i]));
				assertEquals(subtotalesGuardados[i], carritoPage.subtotalFila(productosBase[i]));
			} else {
				assertFalse(carritoPage.nombresFilas().contains(productosBase[i]));
			}
		}
		stopWebDriver();
	}

	@Then("el pedido queda registrado sin descuento y total igual al subtotal")
	public void pedidoSinDescuento() {
		assertEquals(1000000, subtotalAntesConfirmar);
		assertEquals(subtotalAntesConfirmar, totalAntesConfirmar);
		if (!carritoPage.url().contains("/orders")) carritoPage.irPedidos();
		assertEquals(pedidosAntes + 1, carritoPage.cantidadPedidos());
		assertEquals(1000000, carritoPage.totalUltimoPedido());
		stopWebDriver();
	}

	@Then("el resumen muestra descuento de {string} y total de {string}")
	public void validaDescuento(String descuento, String total) {
		assertEquals(1000000, subtotalAntesConfirmar);
		assertEquals(Integer.parseInt(descuento), subtotalAntesConfirmar - totalAntesConfirmar);
		assertEquals(Integer.parseInt(total), totalAntesConfirmar);
		String confirmacion = carritoPage.textoVisibleNumerico();
		if (carritoPage.url().contains("/cart")) assertTrue(confirmacion.contains("1000000"));
		assertTrue(confirmacion.contains(descuento));
		assertTrue(confirmacion.contains(total));
		stopWebDriver();
	}

	@Then("se muestra el beneficio disponible y no se registra pedido")
	public void beneficioSinPedido() {
		assertTrue(carritoPage.textoBeneficio().toLowerCase().contains("descuento"));
		carritoPage.irPedidos();
		assertEquals(pedidosAntes, carritoPage.cantidadPedidos());
		stopWebDriver();
	}

	@When("elimina el unico producto {string} del carrito")
	public void eliminarUnico(String producto) {
		productoActual = producto;
		carritoPage.irPedidos();
		pedidosAntes = carritoPage.cantidadPedidos();
		driver.navigate().back();
		wait(1);
		carritoPage.eliminar(producto);
	}

	@Then("el carrito informa la eliminacion y queda sin productos")
	public void validarEliminacionVacia() {
		assertFalse(carritoPage.obtenerAlerta().trim().isEmpty());
		assertTrue(carritoPage.carritoVacio());
	}

	@Then("no permite confirmar un carrito vacio")
	public void noConfirmarVacio() {
		assertTrue(carritoPage.confirmarDisponible());
		carritoPage.confirmar();
		assertFalse("Debe rechazarse la confirmacion del carritoPage vacio", carritoPage.obtenerAlerta().trim().isEmpty());
		assertTrue("Debe permanecer en el carritoPage", carritoPage.url().contains("/cart"));
		carritoPage.irPedidos();
		assertEquals(pedidosAntes, carritoPage.cantidadPedidos());
		stopWebDriver();
	}

	@Then("el producto {string} queda con cantidad {string} y subtotal recalculado")
	public void validaCantidadActualizada(String producto, String cantidad) {
		validaCantidadActualizada(producto, Integer.parseInt(cantidad));
	}

	@Then("el producto {string} queda con cantidad igual a sus existencias y subtotal recalculado")
	public void validaCantidadIgualExistencias(String producto) {
		validaCantidadActualizada(producto, carritoPage.obtenerStock(producto));
	}

	@When("agrega dos productos distintos al carrito desde el catalogo")
	public void agregarDosProductos() {
		agregarAlCarrito("Teclado", 2);
		catalogoPage.irAlCatalogo();
		agregarAlCarrito("Mouse", 3);
	}

	@When("actualiza {string} a una cantidad valida y elimina {string} en el carrito")
	public void actualizarYEliiminar(String producto, String productoEliminar) {
		catalogoPage.irAlCarrito();
		capturarCantidadYSubtotal(producto);
		carritoPage.cambiarCantidad(producto, "4");
		assertEquals("Cantidad modificada correctamente.", carritoPage.obtenerAlerta());
		carritoPage.eliminar(productoEliminar);
	}

	@Then("solo queda {string} con la cantidad actualizada y el subtotal recalculado")
	public void validarE2E(String producto) {
		wait(1);
		assertEquals(1, carritoPage.nombresFilas().size());
		assertEquals(producto, carritoPage.nombresFilas().get(0));
		assertEquals(4, carritoPage.obtenerCantidadFila(producto));
		assertEquals(4 * carritoPage.obtenerPrecio(producto), carritoPage.subtotalFila(producto));
		stopWebDriver();
	}

	private void agregarAlCarrito(String producto, int cantidad) {
		catalogoPage.irAlCatalogo();
		catalogoPage.ingresarCantidad(producto, String.valueOf(cantidad));
		catalogoPage.agregar(producto);
		assertEquals("Producto agregado correctamente.", catalogoPage.obtenerAviso());
		catalogoPage.irAlCarrito();
	}

	private void capturarCantidadYSubtotal(String producto) {
		productoActual = producto;
		cantidadAnterior = carritoPage.obtenerCantidadFila(producto);
		subtotalAnterior = carritoPage.subtotalFila(producto);
	}

	private void validaCantidadActualizada(String producto, int cantidadEsperada) {
		wait(1);
		assertEquals(cantidadEsperada, carritoPage.obtenerCantidadFila(producto));
		assertEquals(cantidadEsperada, carritoPage.obtenerUnidadesResumen());
		assertEquals(cantidadEsperada * carritoPage.obtenerPrecio(producto), carritoPage.subtotalFila(producto));
		assertEquals(cantidadEsperada * carritoPage.obtenerPrecio(producto), carritoPage.subtotalResumen());
		stopWebDriver();
	}

	private void agregarCantidades(int teclados, int mouse, int monitores) {
		if (teclados > 0) agregarAlCarrito("Teclado", teclados);
		if (mouse > 0) agregarAlCarrito("Mouse", mouse);
		if (monitores > 0) agregarAlCarrito("Monitor", monitores);
		assertFalse("La preparacion debe incluir productos", carritoPage.carritoVacio());
	}
}