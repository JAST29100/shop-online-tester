package com.pruebas.pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CatalogoPage extends BasePage {
	By productos = By.cssSelector("main > section article");
	By filasCarrito = By.cssSelector("main > div > div > section article");	By aviso = By.cssSelector("p[role='status']");
	By tituloProducto = By.cssSelector("h4");
	By tituloFilaCarrito = By.cssSelector("h2");
	By campoCantidad = By.cssSelector("input[type='number']");
	By botonAgregar = By.xpath(".//button[normalize-space()='Agregar al carrito']");
	By botonEliminar = By.xpath(".//button[normalize-space()='Eliminar']");
	By textoExistencias = By.xpath(".//p[contains(., 'Disponibles:')]/strong");
	By textoPrecio = By.xpath(".//p[contains(., '$')]");
	By textoSubtotalFila = By.xpath(".//div[p[normalize-space()='Subtotal']]/p[2]");

	public CatalogoPage(WebDriver driver) {
		super(driver);
	}

	public void ingresarCantidad(String nombre, String cantidad) {
		WebElement input = producto(nombre).findElement(campoCantidad);
		input.clear();
		input.sendKeys(cantidad);
	}

	public void agregar(String nombre) {
		producto(nombre).findElement(botonAgregar).click();
		wait(1);
	}

	public int obtenerExistencias(String nombre) {
		WebElement articulo = producto(nombre);
		String texto = articulo.findElement(textoExistencias).getText();
		return Integer.parseInt(texto.replaceAll("[^0-9]", ""));
	}

	public int obtenerPrecioUnitario(String nombre) {
		WebElement articulo = producto(nombre);
		String texto = articulo.findElement(textoPrecio).getText();
		return Integer.parseInt(texto.replaceAll("[^0-9]", ""));
	}

	public String obtenerAviso() {
		wait(1);
		return driver.findElement(aviso).getText();
	}

	public String obtenerAvisoExistencias(int existencias) {
		return "No hay existencias suficientes. Disponibles: " + existencias + ".";
	}

	public boolean carritoVacio() {
		irAlCarrito();
		return driver.findElements(filasCarrito).isEmpty();
	}

	public void vaciarCarrito() {
		irAlCarrito();
		while (!driver.findElements(filasCarrito).isEmpty()) {
			driver.findElement(filasCarrito).findElement(botonEliminar).click();
			wait(1);
		}
	}

	public int obtenerCantidadFila(String nombre) {
		irAlCarrito();
		WebElement fila = filaCarrito(nombre);
		return Integer.parseInt(fila.findElement(campoCantidad).getAttribute("value"));
	}

	public int obtenerSubtotalFila(String nombre) {
		irAlCarrito();
		WebElement fila = filaCarrito(nombre);
		String texto = fila.findElement(textoSubtotalFila).getText();
		return Integer.parseInt(texto.replaceAll("[^0-9]", ""));
	}

	public void irAlCarrito() {
		driver.get("https://shoponline-app.vercel.app/cart");
		wait(1);
	}

	public void irAlCatalogo() {
		driver.get("https://shoponline-app.vercel.app/catalog");
		wait(1);
	}

	WebElement filaCarrito(String nombre) {
		for (WebElement fila : driver.findElements(filasCarrito)) {
			if (fila.findElement(tituloFilaCarrito).getText().trim().equals(nombre)) return fila;
		}
		return null;
	}
	
	WebElement producto(String nombre) {
		List<WebElement> articulos = driver.findElements(productos);
		for (WebElement articulo : articulos) {
			if (articulo.findElement(tituloProducto).getText().equals(nombre)) {
				return articulo;
			}
		}
		return null;
	}
}