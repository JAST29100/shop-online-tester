package com.pruebas.pages;

import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CarritoPage {
	WebDriver driver;
	
	By filas = By.cssSelector("main > div > div > section article");
	By alerta = By.cssSelector("div[role='alert']");

	public CarritoPage(WebDriver driver) {
		this.driver = driver;
	}

	public void cambiarCantidad(String producto, String cantidad) {
		WebElement fila = fila(producto);
		WebElement input = fila.findElement(By.cssSelector("input[type='number']"));
		input.clear();
		input.sendKeys(cantidad);
		fila.findElement(By.xpath(".//button[normalize-space()='Actualizar']")).click();
		wait(1);
	}

	public void eliminar(String producto) {
		fila(producto).findElement(By.xpath(".//button[normalize-space()='Eliminar']")).click();
		wait(1);
	}

	public String obtenerAlerta() {
		wait(1);
		return driver.findElement(alerta).getText().trim();
	}

	public int obtenerCantidadFila(String producto) {
		return Integer.parseInt(fila(producto).findElement(By.cssSelector("input[type='number']")).getAttribute("value"));
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
		return valorResumen("Subtotal");
	}

	public int totalResumen() {
		return valorResumen("Total a pagar");
	}

	public int obtenerUnidadesResumen() {
		return valorResumen("Unidades");
	}

	public String textoVisible() { return driver.findElement(By.cssSelector("main")).getText(); }
	public String textoVisibleNumerico() { return soloDigitos(textoVisible()); }

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
		driver.findElement(By.xpath("//button[normalize-space()='Confirmar pedido']")).click();
		wait(2);
	}

	public String url() { return driver.getCurrentUrl(); }

	public void irPedidos() {
		driver.findElement(By.cssSelector("a[href='/orders']")).click();
		wait(2);
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

	private int valorResumen(String etiqueta) {
		WebElement valor = bloqueResumen().findElement(By.xpath(
				".//div[span[normalize-space()='" + etiqueta + "']]/strong"));
		return numero(valor.getText());
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

	private int numero(String texto) { return Integer.parseInt(soloDigitos(texto)); }

	private String soloDigitos(String texto) { return texto.replaceAll("[^0-9]", ""); }

	private void wait(int seconds) {
		try {
			Thread.sleep(seconds * 1000L);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
