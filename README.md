[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/omBieXIU)
# Versión en castellano

## Ejercicio: Procesador de Logs

Desarrolla una aplicación de consola que procese ficheros de log de una aplicación. El programa debe funcionar como una herramienta de línea de comandos, al estilo de los comandos de Linux, recibiendo sus opciones y parámetros al ejecutarse.

El procesador deberá leer un fichero de texto cuyo contenido siga este formato:

```text
[YYYY-MM-DD HH:MM:SS] NIVEL Mensaje
```

Ejemplo:

```text
[2025-01-15 10:30:15] INFO Aplicación iniciada
[2025-01-15 10:30:20] ERROR Error de conexión a BD
[2025-01-15 10:30:25] WARNING Reintentando conexión
[2025-01-15 10:30:30] INFO Conexión restaurada
```

## Objetivo del ejercicio

El objetivo es construir una herramienta capaz de:

* leer y validar entradas de log,
* interpretar fechas y niveles,
* filtrar información según distintos criterios,
* calcular estadísticas,
* y generar un informe final por consola o en fichero.

Dicho de forma más directa: no se trata solo de “leer líneas”, sino de **transformar un texto bruto en información útil**. Es justo lo que hacen muchas utilidades reales de administración y observabilidad.

## Requisitos funcionales

Tu programa debe permitir, como mínimo:

1. **Leer un fichero de logs** indicado por línea de comandos.
2. **Interpretar cada línea** del log separando:

    * timestamp,
    * nivel (`INFO`, `WARNING`, `ERROR`),
    * mensaje.
3. **Contar cuántas entradas hay de cada nivel**.
4. **Filtrar por rango de fechas**:

    * fecha/hora inicial,
    * fecha/hora final,
    * ambos límites incluidos.
5. **Generar un informe estadístico** con información como:

    * total de líneas procesadas,
    * total de líneas válidas,
    * total de líneas inválidas,
    * número de `INFO`,
    * número de `WARNING`,
    * número de `ERROR`,
    * primera fecha encontrada,
    * última fecha encontrada.
6. **Mostrar la salida por consola o guardarla en un fichero**, según la opción indicada al ejecutar el comando.

## Requisitos técnicos recomendados

Además de que funcione, conviene exigir al alumnado algunos mínimos de calidad:

* El programa **no debe tener valores fijos en el código** para el nombre del fichero, fechas o salida.
* Debe manejar errores frecuentes:

    * fichero inexistente,
    * formato de fecha incorrecto,
    * línea de log mal formada,
    * nivel no reconocido,
    * imposibilidad de escribir el fichero de salida.
* Debe separar responsabilidades. Por ejemplo:

    * una parte para parsear líneas,
    * otra para filtrar,
    * otra para calcular estadísticas,
    * otra para generar el informe.
* El código debe ser legible, modular y fácil de ampliar.

## Propuesta de comando

Puedes plantear que el ejecutable se llame, por ejemplo:

```bash
logtool
```

## Opciones del comando

Estas opciones le dan al ejercicio un aire más realista y, de paso, obligan a trabajar bien la entrada por línea de comandos:

* `-i, --input <fichero>`
  Fichero de log de entrada. Obligatorio.

* `-f, --from <fechaHora>`
  Fecha/hora inicial del filtro.
  Formato: `"YYYY-MM-DD HH:MM:SS"`

* `-t, --to <fechaHora>`
  Fecha/hora final del filtro.
  Formato: `"YYYY-MM-DD HH:MM:SS"`

* `-l, --level <niveles>`
  Permite filtrar por uno o varios niveles.
  Ejemplo: `INFO` o `ERROR,WARNING`

* `-s, --stats`
  Muestra únicamente las estadísticas.

* `-r, --report`
  Genera un informe completo con detalle y estadísticas.

* `-o, --output <fichero>`
  Guarda la salida en un fichero.

* `-p, --stdout`
  Muestra la salida por consola.

* `--ignore-invalid`
  Ignora líneas mal formadas y continúa el procesamiento.

* `-h, --help`
  Muestra la ayuda del comando.

## Reglas de funcionamiento recomendadas

Para que el ejercicio no quede ambiguo, conviene fijar estas reglas:

* `--input` es obligatorio.
* Debe indicarse **una salida**:

    * o `--stdout`,
    * o `--output <fichero>`.
* Si se indica `--stats`, solo se muestran estadísticas.
* Si se indica `--report`, se muestra el informe completo.
* Si no se indica ni `--stats` ni `--report`, puedes decidir una de estas dos políticas:

    * mostrar error de uso,
    * o asumir `--report` por defecto.

Yo, para alumnado, prefiero que lo dejes explícito y que **`--report` sea el comportamiento por defecto**. Menos ruido, más foco.

## Posible ayuda del comando al usar `-h`

```text
Uso:
  logtool -i <fichero> [opciones]

Descripción:
  Procesa un fichero de logs con formato:
    [YYYY-MM-DD HH:MM:SS] NIVEL Mensaje

Opciones:
  -i, --input <fichero>        Fichero de entrada (obligatorio)
  -f, --from <fechaHora>       Fecha/hora inicial inclusive
                               Formato: "YYYY-MM-DD HH:MM:SS"
  -t, --to <fechaHora>         Fecha/hora final inclusive
                               Formato: "YYYY-MM-DD HH:MM:SS"
  -l, --level <niveles>        Filtra niveles: INFO, WARNING, ERROR
                               Puede indicarse una lista separada por comas
  -s, --stats                  Muestra solo estadísticas
  -r, --report                 Genera informe completo
  -o, --output <fichero>       Guarda la salida en un fichero
  -p, --stdout                 Muestra la salida por consola
      --ignore-invalid         Ignora líneas inválidas y continúa
  -h, --help                   Muestra esta ayuda

Ejemplos:
  logtool -i app.log -p --report
  logtool -i app.log -f "2025-01-15 00:00:00" -t "2025-01-15 23:59:59" -p --stats
  logtool -i app.log -l ERROR,WARNING -o informe.txt --report
```

## Ejemplos de ejecución

### 1. Informe completo por consola

```bash
logtool -i sample_app.log -p --report
```

### 2. Solo estadísticas para un día concreto

```bash
logtool -i sample_app.log \
  -f "2025-01-16 00:00:00" \
  -t "2025-01-16 23:59:59" \
  -p --stats
```

### 3. Solo errores y warnings, guardando la salida en fichero

```bash
logtool -i sample_app.log -l ERROR,WARNING -o informe.txt --report
```

## Qué debería contener el informe

Un informe razonable podría tener este aspecto:

```text
INFORME DE LOGS
===============
Fichero analizado: sample_app.log
Rango aplicado: 2025-01-15 00:00:00 -> 2025-01-17 23:59:59
Niveles incluidos: INFO, WARNING, ERROR

Resumen:
- Líneas procesadas: 82
- Líneas válidas: 82
- Líneas inválidas: 0

Conteo por nivel:
- INFO: 58
- WARNING: 14
- ERROR: 10

Periodo detectado en los logs:
- Primera entrada: 2025-01-15 08:00:00
- Última entrada: 2025-01-17 08:40:00
```

## Qué se trabaja realmente con este ejercicio

Este ejercicio es bastante más rico de lo que parece a primera vista. Permite trabajar:

* lectura de ficheros,
* parsing de texto,
* manejo de fechas,
* colecciones,
* filtrado,
* tratamiento de errores,
* modularización,
* salida formateada,
* paso de parámetros por línea de comandos.

Vamos, que parece pequeño, pero viene bien cargado de “cosas de verdad”.

---
---

## Diferencia entre `--stats` y `--report`

El programa deberá permitir dos modos principales de salida: `--stats` y `--report`.

### Opción `--stats`

Cuando el usuario ejecute el programa con la opción `--stats`, la salida deberá mostrar **únicamente información estadística resumida** sobre el log procesado, sin incluir el detalle de las entradas individuales.

Como mínimo, esta salida deberá incluir:

* el nombre del fichero analizado,
* los filtros aplicados, en caso de existir,
* el número total de líneas procesadas,
* el número de líneas válidas e inválidas,
* el número de entradas de tipo `INFO`,
* el número de entradas de tipo `WARNING`,
* el número de entradas de tipo `ERROR`,
* la primera y la última fecha encontradas en el conjunto de datos filtrado.

Por tanto, `--stats` ofrece una visión global del contenido del log, centrada en el resumen numérico de la información.

### Ejemplo de llamada con `--stats`

```bash id="qf1aa1"
logtool -i sample_app.log -p --stats
```

### Ejemplo de salida de `--stats`

```text id="adnn7d"
ESTADÍSTICAS DE LOGS
====================
Fichero analizado: sample_app.log
Rango aplicado: sin filtro
Niveles incluidos: INFO, WARNING, ERROR

Resumen:
- Líneas procesadas: 82
- Líneas válidas: 82
- Líneas inválidas: 0

Conteo por nivel:
- INFO: 58
- WARNING: 14
- ERROR: 10

Periodo detectado:
- Primera entrada: 2025-01-15 08:00:00
- Última entrada: 2025-01-17 18:42:11
```

---

### Opción `--report`

Cuando el usuario ejecute el programa con la opción `--report`, la salida deberá mostrar un **informe completo**.

Este informe deberá incluir:

1. toda la información estadística que se mostraría con `--stats`,
2. y además el **detalle de las entradas del log** resultantes tras aplicar los filtros indicados.

Por tanto, `--report` no solo resume la información, sino que también muestra las líneas concretas que forman parte del resultado.

### Ejemplo de llamada con `--report`

```bash id="5myl16"
logtool -i sample_app.log -p --report
```

### Ejemplo de salida de `--report`

```text id="fdv2nq"
INFORME DE LOGS
===============
Fichero analizado: sample_app.log
Rango aplicado: sin filtro
Niveles incluidos: INFO, WARNING, ERROR

Resumen:
- Líneas procesadas: 82
- Líneas válidas: 82
- Líneas inválidas: 0

Conteo por nivel:
- INFO: 58
- WARNING: 14
- ERROR: 10

Periodo detectado:
- Primera entrada: 2025-01-15 08:00:00
- Última entrada: 2025-01-17 18:42:11

Entradas encontradas:
[2025-01-15 08:00:00] INFO Aplicación iniciada
[2025-01-15 08:00:03] INFO Cargando configuración
[2025-01-15 08:00:05] WARNING Archivo de configuración secundario no encontrado
[2025-01-15 08:00:10] ERROR Error de conexión a base de datos
[2025-01-15 08:00:15] INFO Reintentando conexión
[2025-01-15 08:00:20] INFO Conexión restaurada
...
```

---

### Regla de uso

Las opciones `--stats` y `--report` son excluyentes. El usuario deberá indicar solo una de ellas en cada ejecución. Si se proporcionan ambas a la vez, el programa deberá informar del error de uso correspondiente.

---

### Ejemplos adicionales con filtros

#### Estadísticas para un rango de fechas

```bash id="yad9s6"
logtool -i sample_app.log -f "2025-01-16 00:00:00" -t "2025-01-16 23:59:59" -p --stats
```

#### Informe completo filtrando solo errores y warnings

```bash id="t7c4w8"
logtool -i sample_app.log -l ERROR,WARNING -p --report
```

---
---

# English version

## Exercise: Log Processor

Build a console application that processes application log files. The program must behave like a Linux-style command-line tool, receiving options and arguments when executed.

The application must read a text file whose lines follow this format:

```text
[YYYY-MM-DD HH:MM:SS] LEVEL Message
```

Example:

```text
[2025-01-15 10:30:15] INFO Application started
[2025-01-15 10:30:20] ERROR Database connection error
[2025-01-15 10:30:25] WARNING Retrying connection
[2025-01-15 10:30:30] INFO Connection restored
```

## Goal

The goal of this exercise is to create a tool able to:

* read and validate log entries,
* parse timestamps and levels,
* filter entries using different criteria,
* compute statistics,
* and generate a final report either on screen or in a file.

In other words, students are not just reading text lines: they are turning raw log data into useful information.

## Functional requirements

The program must be able to:

1. **Read a log file** provided through the command line.
2. **Parse each line** into:

    * timestamp,
    * level (`INFO`, `WARNING`, `ERROR`),
    * message.
3. **Count how many entries exist for each level**.
4. **Filter entries by date range**:

    * start date/time,
    * end date/time,
    * both limits inclusive.
5. **Generate a statistics report** including:

    * total processed lines,
    * total valid lines,
    * total invalid lines,
    * number of `INFO`,
    * number of `WARNING`,
    * number of `ERROR`,
    * first detected timestamp,
    * last detected timestamp.
6. **Write the output either to standard output or to a file**, depending on the command-line option used.

## Suggested command name

```bash
logtool
```

## Suggested command-line options

* `-i, --input <file>`
  Input log file. Required.

* `-f, --from <dateTime>`
  Start date/time filter.
  Format: `"YYYY-MM-DD HH:MM:SS"`

* `-t, --to <dateTime>`
  End date/time filter.
  Format: `"YYYY-MM-DD HH:MM:SS"`

* `-l, --level <levels>`
  Filter by one or more levels.
  Example: `INFO` or `ERROR,WARNING`

* `-s, --stats`
  Show statistics only.

* `-r, --report`
  Generate a full report.

* `-o, --output <file>`
  Write output to a file.

* `-p, --stdout`
  Print output to the console.

* `--ignore-invalid`
  Ignore malformed lines and continue processing.

* `-h, --help`
  Show command help.

## Suggested help output

```text
Usage:
  logtool -i <file> [options]

Description:
  Processes log files with the format:
    [YYYY-MM-DD HH:MM:SS] LEVEL Message

Options:
  -i, --input <file>           Input log file (required)
  -f, --from <dateTime>        Inclusive start date/time
                               Format: "YYYY-MM-DD HH:MM:SS"
  -t, --to <dateTime>          Inclusive end date/time
                               Format: "YYYY-MM-DD HH:MM:SS"
  -l, --level <levels>         Filter by INFO, WARNING, ERROR
                               Comma-separated list allowed
  -s, --stats                  Show statistics only
  -r, --report                 Generate full report
  -o, --output <file>          Write output to a file
  -p, --stdout                 Print output to standard output
      --ignore-invalid         Ignore invalid lines and continue
  -h, --help                   Show this help

Examples:
  logtool -i app.log -p --report
  logtool -i app.log -f "2025-01-15 00:00:00" -t "2025-01-15 23:59:59" -p --stats
  logtool -i app.log -l ERROR,WARNING -o report.txt --report
```

## Example executions

```bash
logtool -i sample_app.log -p --report
```

```bash
logtool -i sample_app.log -f "2025-01-16 00:00:00" -t "2025-01-16 23:59:59" -p --stats
```

```bash
logtool -i sample_app.log -l ERROR,WARNING -o report.txt --report
```

## What students practice in this exercise

This exercise is useful to practice:

* file reading,
* text parsing,
* date/time handling,
* collections,
* filtering,
* error handling,
* modular design,
* formatted output,
* command-line argument processing.

It looks simple, but it is actually a very complete command-line programming task.
