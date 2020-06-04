# Trabajo Práctico Android

###### AUTOR: Docentes Sistemas Operativos Avanzado EDICIÓN: BUENOS AIRES, 2020

# Enunciado del Trabajo Práctico de Android

#### Modalidad de trabajo:

-  El trabajo podrá ser realizado en forma individual o en grupo de 2 integrantes.

El trabajo práctico consistirá en proponer una idea de aplicación que involucre los diferentes conceptos vistos en clase. Dicha aplicación deberá cumplir con los siguientes requisitos:

1) Generar un repositorio público en Github para la entrega del trabajo práctico. El repositorio deberá estar compuesto por los siguientes directorios:
     - CODIGO
     - EJECUTABLE

En el directorio **_CODIGO_** se deberá colocar el código fuente de la aplicación. Luego, en el directorio **_EJECUTABLE_** se deberá guardar el apk de la aplicación (archivo binario del programa)

2) La aplicación Android a desarrollar deberá cumplir con los siguientes requisitos:

- a. Se deberá implementar un sistema con Registro de usuario y Login haciendo peticiones a un servidor, a través de la API desarrollada por la cátedra.
-  b. Se deberá validar el estado de la conexión de internet, en las pantallas de Registro y Login. En caso de que no haya conexión, se deberá mostrar mensaje de error al usuario.
- c. Se deberá implementar alguna de las ejecuciones en Background vistas en clase.
- d. Se deberán hacer uso de al menos dos sensores propios del dispositivo móvil.
- e. La aplicación deberá registrar en el servidor diferentes eventos ocurridos durante su ejecución, como por ejemplo: Actividad de sensores, Login, Ejecuciones en Background, Broadcast, etc. Para ello se deberá utilizar la API desarrollada.
- f. Al escuchar los distintos eventos de un determinado sensor, o conjunto de eventos, los datos capturados deberán ser mostrados en un listado por pantalla. Esta información deberá ser guardada en forma persistente, usando SharedPreferences. Posteriormente los datos mostrados deberán volverse a cargar en el listado, en cada inicio de sesión.
- g. La aplicación deberá tolerar diferentes fallos:
  - i. Pruebas de respuesta fluida.
  - ii. Liberación de recursos.
  - iii. Cambios de estado.
  - iv. Errores de conexión.
  - h. Opcionalmente podrán implementar servicios externos como Firebase o desarrollar uno propio.

##### Aclaraciones:

- El alumno deberá informar previamente la idea de negocio a realizar, con el objetivo de validar que cumpla con los requisitos previos al desarrollo.
- La aplicación debe funcionar correctamente. Además, se va a poder comprobar que no este Harcodeado el post, chequeando el código fuente del tp de los alumnos.
- Se dará un plazo hasta el 13 de mayo para entregar las propuestas del programa (negocio) que va a desarrollar cada grupo.
- Para la entrega final tendrá tiempo de entregar el trabajo práctico hasta la 1ra semana de junio.
- Versiones de Android Studio recomendadas con las que se puede trabajar son 2.4 o 3.5.

------------------

# Estructura de peticiones API REST

En el trabajo práctico se deberán enviar mensajes al servidor usando peticiones API que cumplen el protocolo REST. En este sentido existen tres tipos de peticiones de mensajes POST, que se deberán ser utilizadas para poder realizar lo siguiente.
- a. Registrar un usuario/alumno en la base de datos del Web services
- b. Verificar los datos de los alumnos, que se encuentran registrados en la base de datos del web services, para poder hacer un Login al sistema
- c. Registrar la ocurrencia de distintos eventos en el servidor.

La utilización de estos mensajes ya fue explicado anteriormente. No obstante a continuación se describen el formato, y los datos que se deben enviar y recibir en cada una de las distintas peticiones REST al servidor.

## Ambientes de desarrollo:

En el servidor van existir dos ambientes de desarrollo:

- Uno de Testeo denominado **TEST**
- Otro de Desarrollo denominado **DEV**

El alumno deberá trabajar siempre en el ambiente DEV, solamente podrá usar el ambiente TEST para realizar pruebas en la base de datos.


**La entrega del TP deberá ser realizada en el ambiente DEV**


En el ambiente TEST solamente se podrá probar el registro de los alumnos en la base de datos y el registro de eventos ocurridos en el Smartphone. En TEST no se podrá hacer Login

**NOTA**: Para poder probar la registración de eventos en el entorno TEST, deberá obtener el token del usuario, para eso primero deberá registrar al alumno en el entorno DEV.


### 1) REGISTRO DE USUARIO/ALUMNO


- URI: http://so-unlam.net.ar/api/api/register
- Tipo de Petición: POST
- Estructura de datos (content-type): application/json
- Datos del Request:

```json
{
  "env": "TEST" // o "DEV"
  "name": String(255),
  "lastname": String(255),
  "dni": Int(20),
  "email": String(255) Email,
  "password": String(255), MIN(8), //Debe tener al menos 8 caracteres
  "commission": Int(20),
  "group": Int(20) //el número se la asignará los docentes
}
```

- Datos del response (ambiente **DEV**)

_Success:_

```json
  "state": "success",
  "env": "DEV",
  "token": "$2y$10$FHkKBS/E37cMyf6N9lTCkuaJJoJH4Z1hy5o.fb8ZSNzuBvEsRSIKC"
```


- Datos del response (ambiente **TEST**)

_Success:_

```json
  "state": "success",
  "env": "TEST",
  "user": {
    "env": "TEST",
    "name": String(255),
    "lastname": String(255),
    "dni": Int(20),
    "email": String(255),
    "password": String(255) // Min 8 caracteres
    "comission": Int(20),
    "group": Int(20)
  }
```

_Error:_

```json
  "state": "error",
  "env": "TEST",
  "msg": "Error en los parámetros enviados"
```


### 2) LOGIN DE USUARIO/ALUMNO

- URI: http://so-unlam.net.ar/api/api/login
- Tipo de Petición: POST
- Estructura de datos (content-type): application/json
- Datos del Request:

```json
  {
    "env": "TEST" // o "DEV"
    "name": String(255),
    "lastname": String(255),
    "dni": Int(20),
    "email": String(255) Email, //se valida solamente el mail y el password
    "password": String(255), MIN(8), //se valida solamente el mail y el password
    "commission": Int(20),
    "group": Int(20) //el número se la asignará los docentes
  }
```

- Datos del response (ambiente **TEST** y **DEV**)

_Success:_

```json
  {
    "state": "success",
    "env": "TEST",
    "token": "$2y$10$FHkKBS/E37cMyf6N9lTCkuaJJoJH4Z1hy5o.fb8ZSNzuBvEsRSIKC"
  }
```

_Error:_

```json
  "state": "error",
  "env": "TEST",
  "msg": "Error de autenticación"
```

**ACLARACIÓN: PARA PODER REALIZAR EL LOGIN, ES NECESARIO HABER REALIZADO EL REGISTRO EN UN AMBIENTE DE DESARROLLO.**

### 3) REGISTRAR EVENTO

- URI: [http://so-unlam.net.ar/api/api/event](http://so-unlam.net.ar/api/api/event)
- Tipo de Petición: POST
_ Estructura de datos (content-type): application/json
- Datos del Request:

_Header:_

```json
  "token": "$2y$10$FHkKBS/E37cMyf6N9lTCkuaJJoJH4Z1hy5o.fb8ZSNzuBvEsRSIKC"
```

_Body:_

```json
  {
    “env”: **“TEST”,”DEV”** ,
    "type_events": String(255), //Debe indicarse si es un evento de un sensor, Login, Service, Broadcat, etc
    "state":"ACTIVO/INACTIVO",
    "description": String(255) //Poner un descripción de lo sucedido en el evento registrado
  }
```

- Datos del response (ambiente **TEST** y **DEV**)

_Success:_

```json
  {
    "state": "success",
    "env": "TEST/DEV",
    "event": {
      "type_events": "Broadcast Receiver",
      "state": "Activo",
      "description": "Broadcast que detecta la desconexión 3G",
      "group": 123
    }
  }
```

_Error:_

```json
  {
    "state": "error",
    "env": "TEST/DEV",
    "msg": "Error de autenticación"
  }
```

```json
  {
    "state": "error",
    "env": "TEST/DEV",
    "msg": "Error en los parámetros enviados"
  }
```
