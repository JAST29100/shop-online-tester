package com.pruebas.pages;

import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CarritoPage extends BasePage {
	By filas = By.cssSelector("main > div > div > section article");
	By alerta = By.cssSelector("div[role='alert']");
	By tituloFila = By.cssSelector("h2");
	By campoCantidad = By.cssSelector("input[type='number']");
	By botonActualizar = By.xpath(".//button[normalize-space()='Actualizar']");
	By botonEliminar = By.xpath(".//button[normalize-space()='Eliminar']");
	By textoSubtotalFila = By.xpath(".//div[p[normalize-space()='Subtotal']]/p[2]");
	By textoExistencias = By.xpath(".//p[contains(., 'Existencias:')]");
	By textoPrecio = By.xpath(".//p[contains(., 'Precio unitario:')]");
	By bloqueResumen = By.xpath("//aside//div[h2[normalize-space()='Resumen del pedido']]");
	By botonConfirmar = By.xpath("//button[normalize-space()='Confirmar pedido']");
	By enlacePedidos = By.cssSelector("a[href='/orders']");
	By pedidos = By.cssSelector("main section article");
	By textoTotalPedido = By.xpath(".//p[normalize-space()='Total del pedido']/following-sibling::p[1]");

	public CarritoPage(WebDriver driver) {
		super(driver);
	}

	public void cambiarCantidad(String producto, String cantidad) {
		WebElement articulo = fila(producto);
		WebElement input = articulo.findElement(campoCantidad);
		input.clear();
		input.sendKeys(cantidad);
		articulo.findElement(botonActualizar).click();
		wait(1);
	}

	public void eliminar(String producto) {
		fila(producto).findElement(botonEliminar).click();
		wait(1);
	}

	public String obtenerAlerta() {
		wait(1);
		return driver.findElement(alerta).getText().trim();
	}

	public int obtenerCantidadFila(String producto) {
		return Integer.parseInt(fila(producto).findElement(campoCantidad).getAttribute("value"));
	}

	public int subtotalFila(String producto) {
		return numero(fila(producto).findElement(textoSubtotalFila).getText());
	}

	public int obtenerStock(String producto) {
		return numero(fila(producto).findElement(textoExistencias).getText());
	}

	public int obtenerPrecio(String producto) {
		return numero(fila(producto).findElement(textoPrecio).getText());
	}

	public List<String> nombresFilas() {
		List<String> nombres = new ArrayList<String>();
		for (WebElement articulo : driver.findElements(filas)) {
			nombres.add(articulo.findElement(tituloFila).getText().trim());
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

	public String textoVisibleNumerico() {
		return driver.findElement(By.cssSelector("main")).getText().replaceAll("[^0-9]", "");
	}

	public String textoBeneficio() {
		String resultado = "";
		for (WebElement texto : driver.findElement(bloqueResumen).findElements(By.cssSelector("p"))) {
			String contenido = texto.getText().trim();
			if (contenido.toLowerCase().contains("descuento")) resultado += contenido + " ";
		}
		return resultado;
	}

	public boolean confirmarDisponible() {
		List<WebElement> botones = driver.findElements(botonConfirmar);
		return !botones.isEmpty() && botones.get(0).isDisplayed() && botones.get(0).isEnabled();
	}

	public void confirmar() {
		driver.findElement(botonConfirmar).click();
		wait(1);
	}

	public String url() {
		return driver.getCurrentUrl();
	}

	public void irPedidos() {
		driver.findElement(enlacePedidos).click();
		wait(1);
	}

	public int cantidadPedidos() {
		return driver.findElements(pedidos).size();
	}

	public int totalUltimoPedido() {
		List<WebElement> lista = driver.findElements(pedidos);
		if (lista.isEmpty()) return 0;
		return numero(lista.get(0).findElement(textoTotalPedido).getText());
	}

	WebElement fila(String nombre) {
		for (WebElement articulo : driver.findElements(filas)) {
			if (articulo.findElement(tituloFila).getText().trim().equals(nombre)) return articulo;
		}
		return null;
	}
	
	int valorResumen(String etiqueta) {
		WebElement valor = driver.findElement(bloqueResumen).findElement(
				By.xpath(".//div[span[normalize-space()='" + etiqueta + "']]/strong"));
		return numero(valor.getText());
	}

	int numero(String texto) {
		return Integer.parseInt(texto.replaceAll("[^0-9]", ""));
	}
}