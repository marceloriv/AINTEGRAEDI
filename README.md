# AINTEGRAEDI - Sistema de Consultas Oracle Database

Proyecto Java con interfaz gráfica (Swing) para conectarse a Oracle Autonomous Database usando Oracle Wallet y consultar tablas dinámicamente.

## 🚀 Características

- ✅ Conexión segura a Oracle Autonomous Database con Wallet SSL
- ✅ Arquitectura MVC (Modelo-Vista-Controlador)
- ✅ Interfaz gráfica con Swing
- ✅ Carga dinámica de tablas desde la base de datos
- ✅ Visualización de datos en JTable con columnas automáticas
- ✅ Soporte completo para UTF-8 y caracteres especiales (Ñ, tildes)
- ✅ Configuración externalizada (database.properties)

## 📋 Requisitos

- **Java JDK 21** o superior
- **Maven** (recomendado) o compilación manual
- **Oracle Wallet** descargado desde Oracle Cloud

## 📁 Estructura del Proyecto

```
AINTEGRAEDI/
├── src/main/
│   ├── java/
│   │   ├── bd/
│   │   │   ├── DatabaseManager.java      # Gestor de conexiones
│   │   │   └── TestCharset.java          # Test de codificación
│   │   ├── modelo/
│   │   │   └── ConsultaDAO.java          # Acceso a datos (DAO)
│   │   ├── controlador/
│   │   │   └── Controlador.java          # Controlador MVC
│   │   └── vista/
│   │       ├── VentanaPrincipal.java     # GUI principal (Swing)
│   │       └── VentanaPrincipal.form     # Diseño del formulario
│   └── resource/
│       ├── database.properties           # Configuración (no subir a Git)
│       ├── database.properties.example   # Plantilla de configuración
│       └── wallet/                       # Oracle Wallet (no subir a Git)
│           ├── cwallet.sso
│           ├── ewallet.p12
│           ├── tnsnames.ora
│           └── sqlnet.ora
├── pom.xml                               # Configuración Maven
└── .gitignore                            # Archivos ignorados por Git
```

## ⚙️ Configuración

### 1. Clonar el repositorio

```bash
git clone https://github.com/marceloriv/AINTEGRAEDI.git
cd AINTEGRAEDI
```

### 2. Configurar credenciales

Copia el archivo de ejemplo y edítalo con tus datos:

```bash
cp src/main/resource/database.properties.example src/main/resource/database.properties
```

Edita `src/main/resource/database.properties`:

```properties
# Ruta al wallet de Oracle (relativa al directorio raíz del proyecto)
wallet.path=src\\main\\resource\\wallet

# Nombre del servicio TNS (consulta tnsnames.ora en tu wallet)
service.name=lab1_high

# Usuario de la base de datos
db.user=TU_USUARIO

# Contraseña de la base de datos
db.password=TU_CONTRASEÑA
```

### 3. Colocar el Oracle Wallet

Descarga tu wallet desde Oracle Cloud y coloca los archivos en:
```
src/main/resource/wallet/
```

## 🔧 Compilar y Ejecutar

### Opción 1: Con Maven

```bash
# Compilar
mvn clean compile

# Ejecutar
mvn exec:java -Dexec.mainClass=vista.VentanaPrincipal
```

### Opción 2: Desde VS Code

1. Abre el proyecto en VS Code
2. Ve a `VentanaPrincipal.java`
3. Haz clic en "Run" o presiona `F5`

### Opción 3: Compilación manual

```powershell
# Variables de los JARs de Oracle (ajusta la ruta según tu usuario)
$ojdbc = "$env:USERPROFILE\.m2\repository\com\oracle\database\jdbc\ojdbc11\23.9.0.25.07\ojdbc11-23.9.0.25.07.jar"
$oraclepki = "$env:USERPROFILE\.m2\repository\com\oracle\database\security\oraclepki\23.9.0.25.07\oraclepki-23.9.0.25.07.jar"
$osdt_cert = "$env:USERPROFILE\.m2\repository\com\oracle\database\security\osdt_cert\21.19.0.0\osdt_cert-21.19.0.0.jar"
$osdt_core = "$env:USERPROFILE\.m2\repository\com\oracle\database\security\osdt_core\21.19.0.0\osdt_core-21.19.0.0.jar"

# Compilar
javac -d target/classes -cp "$ojdbc;$oraclepki;$osdt_cert;$osdt_core" `
  src/main/java/bd/DatabaseManager.java `
  src/main/java/modelo/ConsultaDAO.java `
  src/main/java/controlador/Controlador.java `
  src/main/java/vista/VentanaPrincipal.java

# Copiar configuración
Copy-Item src\main\resource\database.properties -Destination target\classes\ -Force

# Ejecutar
java -cp "target/classes;$ojdbc;$oraclepki;$osdt_cert;$osdt_core" vista.VentanaPrincipal
```

## 📦 Dependencias Maven

```xml
<dependencies>
    <!-- Driver JDBC de Oracle -->
    <dependency>
        <groupId>com.oracle.database.jdbc</groupId>
        <artifactId>ojdbc11</artifactId>
        <version>23.9.0.25.07</version>
    </dependency>
    
    <!-- Soporte para Oracle Wallet -->
    <dependency>
        <groupId>com.oracle.database.security</groupId>
        <artifactId>oraclepki</artifactId>
        <version>23.9.0.25.07</version>
    </dependency>
    
    <!-- Herramientas de seguridad Oracle -->
    <dependency>
        <groupId>com.oracle.database.security</groupId>
        <artifactId>osdt_cert</artifactId>
        <version>21.19.0.0</version>
    </dependency>
    
    <dependency>
        <groupId>com.oracle.database.security</groupId>
        <artifactId>osdt_core</artifactId>
        <version>21.19.0.0</version>
    </dependency>
</dependencies>
```

## 🎯 Uso de la Aplicación

1. **Al iniciar:** La aplicación carga automáticamente todas las tablas del esquema
2. **Seleccionar tabla:** Elige una tabla del ComboBox
3. **Ver datos:** Haz clic en "Listar" para mostrar los datos en la tabla
4. **Datos mostrados:** Se muestran todas las columnas y registros de la tabla seleccionada

## 🔍 Solución de Problemas

### Error: "No se encontró el archivo database.properties"

**Solución:** Asegúrate de copiar `database.properties` a `target/classes/` después de compilar, o usa Maven que lo hace automáticamente.

### Error: "ORA-12263: Fallo al acceder a tnsnames.ora"

**Causa:** La ruta al wallet es incorrecta.

**Solución:** Verifica que `wallet.path` en `database.properties` sea la ruta correcta (relativa o absoluta).

### Error: "ORA-12261: Invalid character"

**Causa:** Backslashes mal escapados en la URL de conexión.

**Solución:** Usa `System.setProperty("oracle.net.tns_admin")` en lugar de parámetros en la URL (ya implementado en `DatabaseManager.java`).

### Error: "ORA-17957: No se ha podido inicializar el almacén de claves"

**Causa:** Faltan las librerías de seguridad de Oracle.

**Solución:** Verifica que `oraclepki`, `osdt_cert` y `osdt_core` estén en tu `pom.xml` y ejecuta `mvn clean install`.

### Caracteres especiales se ven mal (Ñ, tildes)

**Solución:** Ya está implementada la corrección automática en `ConsultaDAO.java`. Si persiste el problema, ejecuta:

```bash
java -Dfile.encoding=UTF-8 -cp ... vista.VentanaPrincipal
```

## 🏗️ Arquitectura (Patrón MVC)

### Modelo (`modelo/`)
- **ConsultaDAO.java:** Acceso a datos, ejecuta queries SQL

### Vista (`vista/`)
- **VentanaPrincipal.java:** Interfaz gráfica Swing

### Controlador (`controlador/`)
- **Controlador.java:** Coordina Modelo y Vista

### Infraestructura (`bd/`)
- **DatabaseManager.java:** Gestor de conexiones con Oracle

## 🔒 Seguridad

⚠️ **IMPORTANTE:** 

- ❌ **NO subas** `database.properties` a Git (ya está en `.gitignore`)
- ❌ **NO subas** la carpeta `wallet/` a Git (ya está en `.gitignore`)
- ✅ **SÍ sube** `database.properties.example` como plantilla
- ✅ Usa variables de entorno para producción

## 📚 Información Técnica

- **Base de Datos:** Oracle Autonomous Database
- **Character Set:** AL32UTF8
- **Región:** Santiago (sa-santiago-1)
- **Servicios TNS:** lab1_high, lab1_medium, lab1_low
- **Expiración Wallet:** 2030-03-16

## 👨‍💻 Autor

**Marcelo Rivera**
- GitHub: [@marceloriv](https://github.com/marceloriv)
- Proyecto: Taller de Base de Datos - DuocUC

## 📄 Licencia

Este proyecto es de uso académico para el curso de Taller de Base de Datos.
