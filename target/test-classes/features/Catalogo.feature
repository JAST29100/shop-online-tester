Feature: Validacion de cantidades en el catalogo de ShopOnline
	Scenario: CP-025 - Rechazar el valor limite negativo
		Given inicia sesion con "standard_user" y el carrito esta vacio
		When agrega "-1" unidades de "Teclado" al carrito
		Then el catalogo muestra el aviso "La cantidad debe ser un entero mayor que cero."
		And el carrito permanece vacio
	Scenario: CP-026 - Rechazar el valor limite cero
		Given inicia sesion con "standard_user" y el carrito esta vacio
		When agrega "0" unidades de "Teclado" al carrito
		Then el catalogo muestra el aviso "La cantidad debe ser un entero mayor que cero."
		And el carrito permanece vacio
	Scenario: CP-027 - Aceptar el valor limite uno
		Given inicia sesion con "standard_user" y el carrito esta vacio
		When agrega "1" unidades de "Teclado" al carrito
		Then el catalogo muestra el aviso "Producto agregado correctamente."
		And el carrito muestra "Teclado" con cantidad "1" y subtotal calculado
	Scenario: CP-029 - Aceptar la cantidad exacta de existencias
		Given inicia sesion con "standard_user" y el carrito esta vacio
		When agrega todas las existencias de "Teclado" al carrito
		Then el catalogo muestra el aviso "Producto agregado correctamente."
		And el carrito muestra "Teclado" con cantidad igual a las existencias y subtotal calculado
	Scenario: CP-030 - Rechazar una unidad por encima de las existencias
		Given inicia sesion con "standard_user" y el carrito esta vacio
		When agrega una unidad mas que las existencias de "Teclado"
		Then el catalogo indica que solo hay existencias disponibles
		And el carrito permanece vacio

	Scenario: CP-037 - Rechazar cantidad que supera las existencias acumuladas
		Given inicia sesion con "standard_user" y el carrito esta vacio
		When agrega "2" unidades de "Teclado" al carrito y valida que se agregue correctamente
		When agrega desde el catalogo una unidad mas que las existencias restantes de "Teclado"
		Then el catalogo indica que solo hay existencias disponibles
		And el carrito conserva "Teclado" con cantidad "2" y subtotal calculado
