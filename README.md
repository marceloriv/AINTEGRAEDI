# AINTEGRAEDI - Proyecto Oracle Database con Wallet

Proyecto Java para conectarse a Oracle Autonomous Database usando Oracle Wallet (SSL).

## Requisitos

- Java JDK 21 o superior
- Maven (opcional, recomendado)
- Oracle Wallet descargado desde Oracle Cloud

## Estructura del Proyecto

```
AINTEGRAEDI/
├── src/main/java/
│   ├── bd/
│   │   ├── WalletConnection.java    # Clase de conexión reutilizable
│   │   └── TestWalletConnection.java # Clase de prueba
│   └── wallet/                       # Archivos del Oracle Wallet
│       ├── cwallet.sso
│       ├── ewallet.p12
│       ├── tnsnames.ora
│       ├── sqlnet.ora
│       └── ...
└── pom.xml
```

## Dependencias (en pom.xml)

El proyecto requiere las siguientes dependencias de Oracle:

- **ojdbc11** (23.9.0.25.07) - Driver JDBC de Oracle
- **oraclepki** (23.9.0.25.07) - Oracle PKI para soporte de wallets
- **osdt_cert** (21.19.0.0) - Oracle Security Developer Tools - Certificados
- **osdt_core** (21.19.0.0) - Oracle Security Developer Tools - Core

## Configuración

### 1. Configurar el Wallet

Edita `WalletConnection.java` y ajusta estos valores:

```java
String walletPath = "C:\\ruta\\absoluta\\a\\tu\\wallet";
String serviceName = "lab1_high";  // Alias del tnsnames.ora
String user = "TU_USUARIO";
String password = "TU_PASSWORD";
```

### 2. Verificar tnsnames.ora

Asegúrate de que el archivo `tnsnames.ora` en la carpeta wallet contenga el alias que usarás:

```
lab1_high = (description= (retry_count=20)...(service_name=...)...)
```

## Compilar y Ejecutar

### Opción 1: Con Maven

```powershell
# Compilar el proyecto
mvn clean compile

# Ejecutar la prueba
mvn exec:java -Dexec.mainClass=bd.TestWalletConnection
```

### Opción 2: Con javac/java directamente

```powershell
# Compilar
javac -d target/classes -cp "C:\Users\<USER>\.m2\repository\com\oracle\database\jdbc\ojdbc11\23.9.0.25.07\ojdbc11-23.9.0.25.07.jar;C:\Users\<USER>\.m2\repository\com\oracle\database\security\oraclepki\23.9.0.25.07\oraclepki-23.9.0.25.07.jar;C:\Users\<USER>\.m2\repository\com\oracle\database\security\osdt_cert\21.19.0.0\osdt_cert-21.19.0.0.jar;C:\Users\<USER>\.m2\repository\com\oracle\database\security\osdt_core\21.19.0.0\osdt_core-21.19.0.0.jar" src/main/java/bd/WalletConnection.java src/main/java/bd/TestWalletConnection.java

# Ejecutar
java -cp "target/classes;C:\Users\<USER>\.m2\repository\com\oracle\database\jdbc\ojdbc11\23.9.0.25.07\ojdbc11-23.9.0.25.07.jar;C:\Users\<USER>\.m2\repository\com\oracle\database\security\oraclepki\23.9.0.25.07\oraclepki-23.9.0.25.07.jar;C:\Users\<USER>\.m2\repository\com\oracle\database\security\osdt_cert\21.19.0.0\osdt_cert-21.19.0.0.jar;C:\Users\<USER>\.m2\repository\com\oracle\database\security\osdt_core\21.19.0.0\osdt_core-21.19.0.0.jar" bd.TestWalletConnection
```

## Uso de WalletConnection en tu código

```java
import bd.WalletConnection;
import java.sql.*;

public class MiClase {
    public static void main(String[] args) {
        Connection conexion = WalletConnection.conectar();
        
        if (conexion != null) {
            try {
                // Tu código aquí
                Statement stmt = conexion.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM mi_tabla");
                // ...
                conexion.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
```

## Solución de Problemas

### Error: "Missing artifact com.oracle.database.jdbc:ojdbc11-production"

Maven intentó descargar un artefacto que no existe. Verifica que tu `pom.xml` use las coordenadas correctas como se muestran arriba.

### Error: "ORA-12263: Fallo al acceder a tnsnames.ora"

- Verifica que la ruta al wallet sea **absoluta** y **correcta**
- Asegúrate de que los archivos `tnsnames.ora` y `sqlnet.ora` existan en la carpeta wallet

### Error: "ORA-12261: Invalid character"

- Usa el **alias TNS** (`lab1_high`) en vez del service name completo
- Configura `oracle.net.tns_admin` como propiedad del sistema (ya está implementado en el código)

### Error: "ORA-17957: No se ha podido inicializar el almacén de claves"

Faltan las dependencias de seguridad de Oracle (`oraclepki`, `osdt_cert`, `osdt_core`). Asegúrate de que estén en tu `pom.xml` y ejecuta `mvn clean install`.

### Error: "SSO KeyStore not available"

Las librerías de Oracle PKI no están en el classpath. Verifica que todas las dependencias estén incluidas al compilar y ejecutar.

## Información del Wallet

- **Descargado:** 2025-03-17
- **Expiración SSL:** 2030-03-16
- **Región:** Santiago (sa-santiago-1)
- **Servicios disponibles:** lab1_high, lab1_medium, lab1_low, lab1_tp, lab1_tpurgent

## Notas de Seguridad

⚠️ **IMPORTANTE:** 
- No subas credenciales (usuario/password) a repositorios públicos
- No subas los archivos del wallet a control de versiones
- Considera usar variables de entorno para credenciales sensibles

## Autor

Marcelo-HP
