package com.pruebas.runners;

import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features/Carrito.feature",
    glue = "com.pruebas.steps", 
    plugin = { 
        "pretty", 
        "html:target/cucumber-reports/reporte-carrito.html" 
    }
)

public class CarritoRunner {}