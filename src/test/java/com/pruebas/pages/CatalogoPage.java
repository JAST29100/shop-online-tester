package com.pruebas.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CatalogoPage {
	private WebDriver driver;
	
	private By productos = By.cssSelector("main > section article");
	private By filasCarrito = By.cssSelector("main > div > div > section article");
	private By aviso = By.cssSelector("p[role='status']");

	public CatalogoPage(WebDriver driver) {
		this.driver = driver;
	}

	private WebElement producto(String nombre) {
		new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfAllElementsLocatedBy(productos));
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
	}

	public int obtenerExistencias(String nombre) {
		WebElement articulo = producto(nombre);
		String texto = articulo.findElement(By.xpath(".//p[contains(., 'Disponibles:')]/strong")).getText();
		return Integer.parseInt(texto.replaceAll("[^0-9]", ""));
	}

	public int obtenerPrecioUnitario(String nombre) {
		WebElement articulo = producto(nombre);
		String texto = articulo.findElement(By.xpath(".//p[contains(., '$')]")).getText();
		return Integer.parseInt(texto.replaceAll("[^0-9]", ""));
	}

	public String obtenerAviso() {
		return new WebDriverWait(driver, 5)
				.until(ExpectedConditions.visibilityOfElementLocated(aviso)).getText();
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
			int filasAntes = driver.findElements(filasCarrito).size();
			driver.findElement(filasCarrito).findElement(By.xpath(".//button[normalize-space()='Eliminar']")).click();
			new WebDriverWait(driver, 10).until(d -> d.findElements(filasCarrito).size() < filasAntes);
		}
	}

	public void irAlCatalogo() {
		if (!driver.getCurrentUrl().endsWith("/catalog")) {
			driver.findElement(By.cssSelector("a[href='/catalog']")).click();
			new WebDriverWait(driver, 10).until(ExpectedConditions.urlContains("/catalog"));
		}
		new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfAllElementsLocatedBy(productos));
	}

	public int[] obtenerCantidadYSubtotal(String nombre) {
		irAlCarrito();
		new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("main > div > div > section")));
		List<WebElement> filas = driver.findElements(filasCarrito);
		for (WebElement fila : filas) {
			if (fila.findElement(By.cssSelector("h2")).getText().trim().equals(nombre)) {
				int cantidad = Integer.parseInt(fila.findElement(By.cssSelector("input[type='number']")).getAttribute("value"));
				String subtotal = fila.findElement(By.xpath(".//div[p[normalize-space()='Subtotal']]/p[2]")).getText();
				return new int[] { cantidad, Integer.parseInt(subtotal.replaceAll("[^0-9]", "")) };
			}
		}
		throw new AssertionError("No se encontro " + nombre + " en el carrito");
	}

	public void irAlCarrito() {
		if (!driver.getCurrentUrl().endsWith("/cart")) {
			driver.findElement(By.cssSelector("a[href='/cart']")).click();
			new WebDriverWait(driver, 5).until(ExpectedConditions.urlContains("/cart"));
		}
		new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("main > div > div > section")));
	}
}
