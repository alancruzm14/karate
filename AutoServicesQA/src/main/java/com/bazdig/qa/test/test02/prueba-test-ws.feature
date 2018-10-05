Feature: Prueba para consumir servicio del servidor de archivos.

Background:

* def wsUploadUrl = 'http://10.51.58.253:8080/WsUploadFile/rest/json/folder/'

Scenario: Me regresa una lista de archivos en el folder enviado en el ws y se valida incorrecta

# list wrong
Given url wsUploadUrl
And path 'get'
And param folder = 'AppAndroid'
When method get
Then status 200
And match response == {"noFolders":8,"nameFolders":["Aperturas","APK-02032017","APK-10032017","APK-16032017","APK-28032017","APKs-Dev","APKs-Prod-1","FirmaAztecaAPK","Migraciones"],"descripcion":"Consulta Existosa"}

Scenario: Me regresa una lista de archivos en el folder enviado en el ws y se valida correcta
# list ok
Given url wsUploadUrl
And path 'get'
And param folder = 'AppAndroid'
When method get
Then status 200
And match response == {"noFolders":9,"nameFolders":["Aperturas","APK-02032017","APK-10032017","APK-16032017","APK-28032017","APKs-Dev","APKs-Prod-1","FirmaAztecaAPK","Migraciones"],"descripcion":"Consulta Existosa"}
