Feature: Prueba para intentar leer archivo

Scenario: Get All Tests
	* def hello = 'hola'
	* print 'Se imprime variable hello: ' + hello
	* def doStorage = 
	"""
	function(args) { 
		var DataStorage = Java.type('com.bazdig.qa.utils.DataStorage'); 
		var dS = new DataStorage(); 
		var strRead = dS.read(args); 
		return strRead;
	}
	"""

	
	* def result = call doStorage 'hola'
	* print 'Se imprime variable result: ' + result