package com.pruebas.pages;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CarritoPage {
	private final WebDriver driver;
	private final By filas = By.cssSelector("main > div > div > section article");
	private final By alerta = By.cssSelector("div[role='alert']");

	public CarritoPage(WebDriver driver) {
		this.driver = driver;
	}

	public void esperarCarrito() {
		new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("main > div > div > section")));
	}

	public void cambiarCantidad(String producto, String cantidad) {
		WebElement fila = fila(producto);
		WebElement input = fila.findElement(By.cssSelector("input[type='number']"));
		input.clear();
		input.sendKeys(cantidad);
		fila.findElement(By.xpath(".//button[normalize-space()='Actualizar']")).click();
	}

	public void actualizarA(String producto, int cantidad) {
		cambiarCantidad(producto, String.valueOf(cantidad));
	}

	public void cambiarAExistencias(String producto, int stock) {
		cambiarCantidad(producto, String.valueOf(stock));
	}

	public void eliminar(String producto) {
		fila(producto).findElement(By.xpath(".//button[normalize-space()='Eliminar']")).click();
		new WebDriverWait(driver, 10).until(_ -> buscarFila(producto) == null);
	}

	public String obtenerAlerta() {
		return new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(alerta)).getText().trim();
	}

	public int[] cantidadYSubtotal(String producto) {
		WebElement fila = fila(producto);
		int cantidad = Integer.parseInt(fila.findElement(By.cssSelector("input[type='number']")).getAttribute("value"));
		return new int[] { cantidad, subtotalFila(producto) };
	}

	public int subtotalFila(String producto) {
		String subtotal = fila(producto).findElement(By.xpath(".//div[p[normalize-space()='Subtotal']]/p[2]")).getText();
		return numero(subtotal);
	}

	public int obtenerStock(String producto) {
		String texto = fila(producto).findElement(By.xpath(".//p[contains(., 'Existencias:')]")).getText();
		return numero(texto);
	}

	public int obtenerPrecio(String producto) {
		String texto = fila(producto).findElement(By.xpath(".//p[contains(., 'Precio unitario:')]")).getText();
		return numero(texto);
	}

	public List<String> nombresFilas() {
		List<String> nombres = new ArrayList<String>();
		for (WebElement articulo : driver.findElements(filas)) {
			nombres.add(articulo.findElement(By.cssSelector("h2")).getText().trim());
		}
		return nombres;
	}

	public boolean carritoVacio() {
		return driver.findElements(filas).isEmpty();
	}

	public int subtotalResumen() {
		return numero(valorResumen("Subtotal"));
	}

	public int totalResumen() {
		return numero(valorResumen("Total a pagar"));
	}

	public int unidadesResumen() {
		return Integer.parseInt(valorResumen("Unidades").replaceAll("[^0-9]", ""));
	}

	public int obtenerUnidadesResumen() {
		String unidades = bloqueResumen()
				.findElement(By.xpath(".//span[normalize-space()='Unidades']/following-sibling::strong[1]"))
				.getText();
		return Integer.parseInt(unidades.trim());
	}

	public String textoVisible() { return driver.findElement(By.cssSelector("main")).getText(); }

	public String textoBeneficio() {
		List<WebElement> textos = bloqueResumen().findElements(By.cssSelector("p"));
		StringBuilder resultado = new StringBuilder();
		for (WebElement texto : textos) {
			String contenido = texto.getText().trim();
			if (contenido.toLowerCase().contains("descuento")) resultado.append(contenido).append(" ");
		}
		return resultado.toString();
	}

	public boolean confirmarDisponible() {
		List<WebElement> botones = driver.findElements(By.xpath("//button[normalize-space()='Confirmar pedido']"));
		return !botones.isEmpty() && botones.get(0).isDisplayed() && botones.get(0).isEnabled();
	}

	public void confirmar() {
		String alertaAnterior = "";
		List<WebElement> alertasVisibles = driver.findElements(alerta);
		if (!alertasVisibles.isEmpty() && alertasVisibles.get(0).isDisplayed()) alertaAnterior = alertasVisibles.get(0).getText().trim();
		final String textoAnterior = alertaAnterior;
		driver.findElement(By.xpath("//button[normalize-space()='Confirmar pedido']")).click();
		new WebDriverWait(driver, 10).until(d -> {
			if (d.getCurrentUrl().contains("/orders")) return true;
			List<WebElement> alertas = d.findElements(alerta);
			return !alertas.isEmpty() && alertas.get(0).isDisplayed() && !alertas.get(0).getText().trim().equals(textoAnterior);
		});
	}

	public String url() { return driver.getCurrentUrl(); }

	public void irPedidos() {
		driver.findElement(By.cssSelector("a[href='/orders']")).click();
		new WebDriverWait(driver, 10).until(ExpectedConditions.urlContains("/orders"));
	}

	public int cantidadPedidos() {
		return driver.findElements(By.cssSelector("main section article")).size();
	}

	public int totalUltimoPedido() {
		java.util.List<WebElement> pedidos = driver.findElements(By.cssSelector("main section article"));
		if (pedidos.isEmpty()) throw new AssertionError("No hay pedidos registrados");
		WebElement ultimo = pedidos.get(0);
		return numero(ultimo.findElement(By.xpath(".//p[normalize-space()='Total del pedido']/following-sibling::p[1]")).getText());
	}

	private String valorResumen(String etiqueta) {
		WebElement valor = bloqueResumen().findElement(By.xpath(
				".//div[span[normalize-space()='" + etiqueta + "']]/strong"));
		return valor.getText().replace("\u00a0", " ").replaceAll("[^0-9]", "");
	}

	private WebElement bloqueResumen() {
		return driver.findElement(By.xpath("//aside//div[h2[normalize-space()='Resumen del pedido']]"));
	}

	private WebElement fila(String nombre) {
		WebElement encontrada = buscarFila(nombre);
		if (encontrada == null) throw new AssertionError("No se encontro " + nombre + " en el carrito");
		return encontrada;
	}

	private WebElement buscarFila(String nombre) {
		for (WebElement articulo : driver.findElements(filas)) {
			if (articulo.findElement(By.cssSelector("h2")).getText().trim().equals(nombre)) return articulo;
		}
		return null;
	}

	private int numero(String texto) { return Integer.parseInt(texto.replace("\u00a0", " ").replaceAll("[^0-9]", "")); }
}
