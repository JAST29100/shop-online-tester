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
	private final Map<String, Integer> precios = new LinkedHashMap<String, Integer>();
	private final Map<String, Integer> stocks = new LinkedHashMap<String, Integer>();
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
		carrito.cambiarAExistencias(producto, stocks.get(producto));
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
	public void preparaSubtotalMenor() { prepararPedido("below"); }

	@When("prepara el carrito con al menos veinte unidades y subtotal exacto de un millon")
	public void preparaSubtotalExacto() { prepararPedido("exact"); }

	@When("prepara el carrito con exactamente diecinueve unidades y subtotal minimo alcanzado")
	public void preparaDiecinueveUnidades() { prepararPedido("nineteen"); }

	@When("prepara el carrito con menos de veinte unidades y subtotal inferior al minimo")
	public void preparaNoElegible() { prepararPedido("small"); }

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
		Map<String, int[]> esperado = leerCantidadesCarrito();
		carrito.irPedidos();
		assertEquals(pedidosAntes, carrito.cantidadPedidos());
		driver.navigate().back();
		carrito.esperarCarrito();
		for (Map.Entry<String, int[]> item : esperado.entrySet()) {
			int[] actual = carrito.cantidadYSubtotal(item.getKey());
			assertEquals(item.getValue()[0], actual[0]);
			assertEquals(item.getValue()[1], actual[1]);
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
		if (carrito.url().contains("/cart")) {
			String confirmacion = carrito.textoVisible().replaceAll("[^0-9]", "");
			assertTrue(confirmacion.contains("1000000"));
			assertTrue(confirmacion.contains(descuento));
			assertTrue(confirmacion.contains(total));
		} else {
			String confirmacion = carrito.textoVisible().replaceAll("[^0-9]", "");
			assertTrue(confirmacion.contains(descuento));
			assertTrue(confirmacion.contains(total));
		}
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
		carrito.actualizarA(producto, 4);
		assertEquals("Cantidad modificada correctamente.", carrito.obtenerAlerta());
		carrito.eliminar(productoEliminar);
	}

	@Then("solo queda {string} con la cantidad actualizada y el subtotal recalculado")
	public void validarE2E(String producto) {
		assertEquals(1, carrito.nombresFilas().size());
		assertEquals(producto, carrito.nombresFilas().get(0));
		int[] valores = carrito.cantidadYSubtotal(producto);
		assertEquals(4, valores[0]);
		assertEquals(4 * precios.get(producto), valores[1]);
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
		int[] valores = carrito.cantidadYSubtotal(producto);
		cantidadAnterior = valores[0];
		subtotalAnterior = valores[1];
		stocks.put(producto, carrito.obtenerStock(producto));
		precios.put(producto, carrito.obtenerPrecio(producto));
	}

	private void validaCantidadActualizada(String producto, int cantidadEsperada) {
		int[] valores = carrito.cantidadYSubtotal(producto);
		wait(1);
		assertEquals(cantidadEsperada, valores[0]);
		assertEquals(cantidadEsperada, carrito.obtenerUnidadesResumen());
		assertEquals(cantidadEsperada * precios.get(producto), valores[1]);
		assertEquals(cantidadEsperada * precios.get(producto), carrito.subtotalResumen());
		stopWebDriver();
	}

	private void leerCatalogo() {
		precios.clear(); stocks.clear();
		for (String producto : new String[] { "Teclado", "Mouse", "Monitor" }) {
			precios.put(producto, catalogo.obtenerPrecioUnitario(producto));
			stocks.put(producto, catalogo.obtenerExistencias(producto));
		}
	}

	private boolean carritoVacioEnRutaCarrito() {
		carrito.esperarCarrito();
		return carrito.carritoVacio();
	}

	private Map<String, int[]> leerCantidadesCarrito() {
		Map<String, int[]> datos = new LinkedHashMap<String, int[]>();
		for (String producto : carrito.nombresFilas()) datos.put(producto, carrito.cantidadYSubtotal(producto));
		return datos;
	}

	private void prepararPedido(String tipo) {
		int[] cantidades = calcularCantidades(tipo);
		String[] productos = { "Teclado", "Mouse", "Monitor" };
		for (int i = 0; i < cantidades.length; i++) if (cantidades[i] > 0) agregarAlCarrito(productos[i], cantidades[i]);
		assertTrue("La preparacion debe incluir productos", carrito.nombresFilas().size() > 0);
	}

	private int[] calcularCantidades(String tipo) {
		String[] nombres = { "Teclado", "Mouse", "Monitor" };
		int[] p = { precios.get(nombres[0]), precios.get(nombres[1]), precios.get(nombres[2]) };
		int[] s = { stocks.get(nombres[0]), stocks.get(nombres[1]), stocks.get(nombres[2]) };
		int[] mejor = null;
		long mejorValor = tipo.equals("below") ? -1 : Long.MAX_VALUE;
		int max0 = Math.min(s[0], 1000000 / p[0]);
		int max1 = Math.min(s[1], 1000000 / p[1]);
		if (tipo.equals("small")) { max0 = Math.min(s[0], 19); max1 = Math.min(s[1], 19); }
		for (int a = 0; a <= max0; a++) for (int b = 0; b <= max1; b++) {
			int c;
			if (tipo.equals("exact")) {
				int restante = 1000000 - a * p[0] - b * p[1];
				if (restante < 0 || restante % p[2] != 0) continue;
				c = restante / p[2];
			} else if (tipo.equals("nineteen")) {
				c = 19 - a - b;
			} else if (tipo.equals("small")) {
				if (a + b >= 19) continue;
				c = Math.min(s[2], Math.min(18 - a - b, (999999 - a * p[0] - b * p[1]) / p[2]));
			} else {
				c = Math.min(s[2], (999999 - a * p[0] - b * p[1]) / p[2]);
			}
			if (c < 0 || c > s[2]) continue;
			int unidades = a + b + c;
			long subtotal = (long) a * p[0] + (long) b * p[1] + (long) c * p[2];
			boolean valido = tipo.equals("exact") ? subtotal == 1000000 && unidades >= 20
					: tipo.equals("below") ? subtotal < 1000000 && unidades >= 20
					: tipo.equals("nineteen") ? unidades == 19 && subtotal >= 1000000
					: unidades > 0 && unidades < 20 && subtotal < 1000000;
			if (!valido) continue;
			if (tipo.equals("below") ? subtotal > mejorValor : subtotal < mejorValor) {
				mejorValor = subtotal; mejor = new int[] { a, b, c };
			}
		}
		if (mejor == null) throw new AssertionError("No se encontro combinacion con el inventario visible para " + tipo);
		return mejor;
	}
}
