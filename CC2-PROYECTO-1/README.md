# Elevator Manager

Simulador gráfico de manejo de elevadores desarrollado en Java. Permite configurar varios elevadores, ingresar solicitudes y observar su estado y sus registros.

## Requisitos

- Java JDK instalado y disponible desde la terminal (`java` y `javac`).
- Ejecutar los comandos desde la carpeta `CC2-PROYECTO-1`.

## Compilar

En Windows PowerShell:

```powershell
New-Item -ItemType Directory -Force out | Out-Null
$archivos = Get-ChildItem -Recurse Elevator -Filter *.java | ForEach-Object { $_.FullName }
javac -d out $archivos
```

En Linux o macOS:

```bash
mkdir -p out
javac -d out Elevator/*.java Elevator/componentes/*.java
```

Si PowerShell indica que `javac` no se reconoce, instala un JDK y configura su carpeta `bin` en `PATH`; después abre una terminal nueva.

## Ejecutar

```text
java -cp out Elevator.ElevatorManager
```

Al iniciar, ingresa la cantidad de elevadores, la cantidad de pisos y los tiempos de espera y movimiento **en milisegundos**.

## Controles e información

- **Iniciar:** configura y comienza una simulación.
- **Detener:** detiene los hilos; cuando terminan, se puede iniciar otra simulación.
- **Reset:** borra las solicitudes y devuelve inmediatamente los elevadores al piso 1, con dirección hacia arriba.
- **Nueva solicitud:** permite llamar un elevador desde un piso con una dirección, o elegir un destino desde dentro de un elevador.
- **Cargar archivo:** carga llamadas externas antes de iniciar o después de Reset.

La tabla muestra el piso actual y la dirección de cada elevador. **Piso destino** presenta los pisos elegidos desde dentro; **Llamadas externas** presenta las llamadas asignadas por el manejador, con `↑` para subir y `↓` para bajar. Los registros aparecen en la ventana y en un archivo `Elevador-ID.log` por elevador.

## Formato del archivo de solicitudes

Cada línea contiene el piso y la dirección de una llamada externa, separados por una coma:

```text
5,UP
2,DOWN
```

Se aceptan `UP`/`DOWN` en mayúsculas o minúsculas. Las líneas vacías y las líneas que comienzan con `#` se ignoran. El piso debe estar dentro del rango configurado para el edificio.
