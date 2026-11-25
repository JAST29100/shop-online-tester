Feature: Validacion del carrito de compras de ShopOnline

	Scenario: CP-014 - Rechazar cantidad negativa al actualizar
		Given inicia una sesion de carrito con "standard_user" y carrito vacio
		Given el carrito tiene una unidad valida de "Teclado"
		When cambia la cantidad de "Teclado" a "-1" y pulsa actualizar
		Then el carrito muestra el error "La cantidad debe ser mayor que cero."
		And conserva la cantidad y subtotal previos de "Teclado"

	Scenario: CP-015 - Rechazar cantidad decimal al actualizar
		Given inicia una sesion de carrito con "standard_user" y carrito vacio
		Given el carrito tiene una unidad valida de "Teclado"
		When cambia la cantidad de "Teclado" a "1.5" y pulsa actualizar
		Then el carrito muestra el error "La cantidad debe ser un número entero."
		And conserva la cantidad y subtotal previos de "Teclado"

	Scenario: CP-016 - Rechazar cantidad superior al stock al actualizar
		Given inicia una sesion de carrito con "standard_user" y carrito vacio
		Given el carrito tiene una unidad valida de "Teclado"
		When cambia la cantidad de "Teclado" a una unidad mas que sus existencias y pulsa actualizar
		Then el carrito rechaza la cantidad por superar las existencias
		And conserva la cantidad y subtotal previos de "Teclado"

	Scenario: CP-019 - Rechazar pedido con subtotal inferior al minimo
		Given inicia una sesion de carrito con "standard_user" y carrito vacio
		When prepara el carrito con al menos veinte unidades y subtotal maximo inferior al minimo
		And confirma el pedido del carrito
		Then el carrito informa el rechazo de minimo de compra
		And no se registra pedido y se conserva el carrito

	Scenario: CP-020 - Confirmar pedido con subtotal exacto de un millon
		Given inicia una sesion de carrito con "standard_user" y carrito vacio
		When prepara el carrito con al menos veinte unidades y subtotal exacto de un millon
		And confirma el pedido del carrito
		Then el pedido queda registrado sin descuento y total igual al subtotal

	Scenario: CP-022 - Rechazar pedido con diecinueve unidades
		Given inicia una sesion de carrito con "standard_user" y carrito vacio
		When prepara el carrito con exactamente diecinueve unidades y subtotal minimo alcanzado
		And confirma el pedido del carrito
		Then el carrito informa el rechazo de minimo de unidades
		And no se registra pedido y se conserva el carrito

	Scenario: CP-032 - Rechazar cantidad cero al actualizar
		Given inicia una sesion de carrito con "standard_user" y carrito vacio
		Given el carrito tiene una unidad valida de "Teclado"
		When cambia la cantidad de "Teclado" a "0" y pulsa actualizar
		Then el carrito muestra el error "La cantidad debe ser mayor que cero."
		And conserva la cantidad y subtotal previos de "Teclado"

	Scenario: CP-033 - Actualizar a una unidad
		Given inicia una sesion de carrito con "standard_user" y carrito vacio
		Given el carrito tiene dos unidades validas de "Teclado"
		When cambia la cantidad de "Teclado" a "1" y pulsa actualizar
		Then el carrito muestra el aviso "Cantidad modificada correctamente."
		And el producto "Teclado" queda con cantidad "1" y subtotal recalculado

	Scenario: CP-035 - Actualizar a todas las existencias
		Given inicia una sesion de carrito con "standard_user" y carrito vacio
		Given el carrito tiene una unidad valida de "Teclado"
		When cambia la cantidad de "Teclado" a todas sus existencias y pulsa actualizar
		Then el carrito muestra el aviso "Cantidad modificada correctamente."
		And el producto "Teclado" queda con cantidad igual a sus existencias y subtotal recalculado

	Scenario: CP-036 - Rechazar stock mas una unidad al actualizar
		Given inicia una sesion de carrito con "standard_user" y carrito vacio
		Given el carrito tiene una unidad valida de "Teclado"
		When cambia la cantidad de "Teclado" a una unidad mas que sus existencias y pulsa actualizar
		Then el carrito rechaza la cantidad por superar las existencias
		And conserva la cantidad y subtotal previos de "Teclado"

	Scenario: CP-038 - Aplicar descuento de primera compra
		Given inicia una sesion de carrito con "discount_user" y carrito vacio
		When prepara el carrito con al menos veinte unidades y subtotal exacto de un millon
		And confirma el pedido del carrito
		Then el resumen muestra descuento de "100000" y total de "900000"

	Scenario: CP-041 - Rechazar carrito no elegible y mostrar beneficio
		Given inicia una sesion de carrito con "discount_user" y carrito vacio
		When prepara el carrito con menos de veinte unidades y subtotal inferior al minimo
		And confirma el pedido del carrito
		Then el carrito informa el rechazo de minimo de compra
		And se muestra el beneficio disponible y no se registra pedido

	Scenario: CP-052 - Eliminar el unico producto del carrito
		Given inicia una sesion de carrito con "standard_user" y carrito vacio
		Given el carrito tiene una unidad valida de "Teclado"
		When elimina el unico producto "Teclado" del carrito
		Then el carrito informa la eliminacion y queda sin productos
		And no permite confirmar un carrito vacio

	Scenario: CP-054 - Actualizar y eliminar productos en el flujo de carrito
		Given inicia una sesion de carrito con "standard_user" y carrito vacio
		When agrega dos productos distintos al carrito desde el catalogo
		And actualiza "Teclado" a una cantidad valida y elimina "Mouse" en el carrito
		Then solo queda "Teclado" con la cantidad actualizada y el subtotal recalculado
