Feature: Login en ShopOnline
	Scenario: CP-001 - Login exitoso con credenciales correctas
		Given esta en la pagina de login
		When ingresa el usuario "standard_user" y la contrasena "ShopQA2026!"
		And selecciona el boton Login
		Then el sistema muestra la pagina "https://shoponline-app.vercel.app/catalog"
	Scenario: CP-002 - Login fallido con contraseña incorrecta
		Given esta en la pagina de login
		When ingresa el usuario "standard_user" y la contrasena "SecretSauce"
		And selecciona el boton Login
		Then el sistema muestra el mensaje de error "Usuario o contraseña incorrectos."
	Scenario: CP-003 - Login fallido con usuario incorrecto
		Given esta en la pagina de login
		When ingresa el usuario "MasterQA" y la contrasena "ShopQA2026!"
		And selecciona el boton Login
		Then el sistema muestra el mensaje de error "Usuario o contraseña incorrectos."
	Scenario: CP-006 - Login fallido por falta de credenciales
		Given esta en la pagina de login
		When selecciona el boton Login
		Then el sistema muestra el pop-up "Please fill out this field."