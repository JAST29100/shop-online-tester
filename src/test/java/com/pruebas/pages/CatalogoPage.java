package com.pruebas.pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CatalogoPage {
	WebDriver driver;
	
	By productos = By.cssSelector("main > section article");
	By filasCarrito = By.cssSelector("main > div > div > section article");
	By aviso = By.cssSelector("p[role='status']");

	public CatalogoPage(WebDriver driver) {
		this.driver = driver;
	}

	private WebElement producto(String nombre) {
		wait(1);
		List<WebElement> articulos = driver.findElements(productos);
		for (WebElement articulo : articulos) {
			if (articulo.findElement(By.cssSelector("h4")).getText().equals(nombre)) {
				return articulo;
			}
		}
		throw new AssertionError("No se encontro el producto: " + nombre);
	}

	public void ingresarCantidad(String nombre, String cantidad) {
		WebElement input = producto(nombre).findElement(By.cssSelector("input[type='number']"));
		input.clear();
		input.sendKeys(cantidad);
	}

	public void agregar(String nombre) {
		producto(nombre).findElement(By.xpath(".//button[normalize-space()='Agregar al carrito']")).click();
		wait(1);
	}

	public int obtenerExistencias(String nombre) {
		WebElement articulo = producto(nombre);
		String texto = articulo.findElement(By.xpath(".//p[contains(., 'Disponibles:')]/strong")).getText();
		return Integer.parseInt(soloDigitos(texto));
	}

	public int obtenerPrecioUnitario(String nombre) {
		WebElement articulo = producto(nombre);
		String texto = articulo.findElement(By.xpath(".//p[contains(., '$')]")).getText();
		return Integer.parseInt(soloDigitos(texto));
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
			driver.findElement(filasCarrito).findElement(By.xpath(".//button[normalize-space()='Eliminar']")).click();
			wait(1);
		}
	}

	public void irAlCatalogo() {
		if (!driver.getCurrentUrl().endsWith("/catalog")) {
			driver.findElement(By.cssSelector("a[href='/catalog']")).click();
			wait(2);
		}
		wait(1);
	}

	public int obtenerCantidadFila(String nombre) {
		irAlCarrito();
		WebElement fila = filaCarrito(nombre);
		return Integer.parseInt(fila.findElement(By.cssSelector("input[type='number']")).getAttribute("value"));
	}

	public int obtenerSubtotalFila(String nombre) {
		irAlCarrito();
		WebElement fila = filaCarrito(nombre);
		String texto = fila.findElement(By.xpath(".//div[p[normalize-space()='Subtotal']]/p[2]")).getText();
		return Integer.parseInt(soloDigitos(texto));
	}

	public void irAlCarrito() {
		driver.get("https://shoponline-app.vercel.app/cart");
	}

	private void wait(int seconds) {
		try {
			Thread.sleep(seconds * 1000L);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	private String soloDigitos(String texto) { return texto.replaceAll("[^0-9]", ""); }

	private WebElement filaCarrito(String nombre) {
		for (WebElement fila : driver.findElements(filasCarrito)) {
			if (fila.findElement(By.cssSelector("h2")).getText().trim().equals(nombre)) return fila;
		}
		throw new AssertionError("No se encontro " + nombre + " en el carrito");
	}
}
