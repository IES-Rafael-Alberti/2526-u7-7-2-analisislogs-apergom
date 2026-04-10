package org.iesra

import java.io.File
import kotlin.system.exitProcess

data class LogEntry(
    val timestamp: String,
    val level: String,
    val message: String,
    val rawLine: String
)

class LogStats {
    var totalProcessed = 0
    var validLines = 0
    var invalidLines = 0
    val levelCounts = mutableMapOf("INFO" to 0, "WARNING" to 0, "ERROR" to 0)
    var firstDate: String? = null
    var lastDate: String? = null
}

data class AppConfig(
    val inputFile: String = "",
    val outputFile: String? = null,
    val stdout: Boolean = false,
    val dateFrom: String? = null,
    val dateTo: String? = null,
    val levels: Set<String>? = null,
    val isStatsOnly: Boolean = false,
    val isReport: Boolean = false,
    val ignoreInvalid: Boolean = false
)

class LogProcessor(private val config: AppConfig) {
    companion object {
        private val LOG_PATTERN = Regex("""^\[(.*?)\] (INFO|WARNING|ERROR) (.*)$""")
        private val DATE_PATTERN = Regex("""^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$""")
    }

    fun process(): Pair<LogStats, List<LogEntry>> {
        val stats = LogStats()
        val filteredEntries = mutableListOf<LogEntry>()
        val file = File(config.inputFile)

        if (!file.exists() || !file.isFile) {
            System.err.println("Error: El fichero '${config.inputFile}' no existe o no es válido.")
            exitProcess(1)
        }

        try {
            file.useLines { lines ->
                for (line in lines) {
                    val trimmed = line.trim()
                    if (trimmed.isEmpty()) continue

                    stats.totalProcessed++

                    val entry = parseLine(trimmed)
                    if (entry == null) {
                        stats.invalidLines++
                        continue
                    }

                    stats.validLines++

                    if (config.levels != null && entry.level !in config.levels) continue
                    if (config.dateFrom != null && entry.timestamp < config.dateFrom) continue
                    if (config.dateTo != null && entry.timestamp > config.dateTo) continue

                    stats.levelCounts[entry.level] = (stats.levelCounts[entry.level] ?: 0) + 1

                    if (stats.firstDate == null || entry.timestamp < stats.firstDate!!) {
                        stats.firstDate = entry.timestamp
                    }
                    if (stats.lastDate == null || entry.timestamp > stats.lastDate!!) {
                        stats.lastDate = entry.timestamp
                    }

                    filteredEntries.add(entry)
                }
            }
        } catch (e: Exception) {
            System.err.println("Error al leer el fichero: ${e.message}")
            exitProcess(1)
        }

        return Pair(stats, filteredEntries)
    }

    private fun parseLine(line: String): LogEntry? {
        val matchResult = LOG_PATTERN.matchEntire(line)
        if (matchResult == null) {
            if (!config.ignoreInvalid) {
                System.err.println("Error: Formato de línea inválido: $line")
                exitProcess(1)
            }
            return null
        }

        val (dateStr, level, message) = matchResult.destructured
        
        if (!DATE_PATTERN.matches(dateStr)) {
            if (!config.ignoreInvalid) {
                System.err.println("Error: Formato de fecha inválido en línea: $line")
                exitProcess(1)
            }
            return null
        }

        return LogEntry(dateStr, level, message, line)
    }
}

class ReportGenerator {
    fun generate(config: AppConfig, stats: LogStats, entries: List<LogEntry>): String {
        val builder = StringBuilder()
        val title = if (config.isStatsOnly) "ESTADÍSTICAS DE LOGS" else "INFORME DE LOGS"
        
        builder.appendLine(title)
        builder.appendLine("=".repeat(title.length))
        builder.appendLine("Fichero analizado: ${config.inputFile}")

        val fromStr = config.dateFrom ?: "sin límite inicial"
        val toStr = config.dateTo ?: "sin límite final"
        val rangeStr = if (config.dateFrom == null && config.dateTo == null) "sin filtro" else "$fromStr -> $toStr"
        
        builder.appendLine("Rango aplicado: $rangeStr")
        val levelsStr = config.levels?.joinToString(", ") ?: "INFO, WARNING, ERROR"
        builder.appendLine("Niveles incluidos: $levelsStr\n")

        builder.appendLine("Resumen:")
        builder.appendLine("- Líneas procesadas: ${stats.totalProcessed}")
        builder.appendLine("- Líneas válidas: ${stats.validLines}")
        builder.appendLine("- Líneas inválidas: ${stats.invalidLines}\n")

        builder.appendLine("Conteo por nivel:")
        builder.appendLine("- INFO: ${stats.levelCounts["INFO"] ?: 0}")
        builder.appendLine("- WARNING: ${stats.levelCounts["WARNING"] ?: 0}")
        builder.appendLine("- ERROR: ${stats.levelCounts["ERROR"] ?: 0}\n")

        builder.appendLine("Periodo detectado:")
        val firstDateStr = stats.firstDate ?: "N/A"
        val lastDateStr = stats.lastDate ?: "N/A"
        builder.appendLine("- Primera entrada: $firstDateStr")
        builder.appendLine("- Última entrada: $lastDateStr")

        if (!config.isStatsOnly && entries.isNotEmpty()) {
            builder.appendLine("\nEntradas encontradas:")
            entries.forEach { builder.appendLine(it.rawLine) }
        }

        return builder.toString()
    }
}

fun printHelp() {
    println("""
        Uso:
          logtool -i <fichero> [opciones]
        
        Descripción:
          Procesa un fichero de logs con formato:
            [YYYY-MM-DD HH:MM:SS] NIVEL Mensaje
        
        Opciones:
          -i, --input <fichero>        Fichero de entrada (obligatorio)
          -f, --from <fechaHora>       Fecha/hora inicial inclusive (Formato: "YYYY-MM-DD HH:MM:SS")
          -t, --to <fechaHora>         Fecha/hora final inclusive (Formato: "YYYY-MM-DD HH:MM:SS")
          -l, --level <niveles>        Filtra niveles: INFO, WARNING, ERROR (separados por comas)
          -s, --stats                  Muestra solo estadísticas
          -r, --report                 Genera informe completo (por defecto)
          -o, --output <fichero>       Guarda la salida en un fichero
          -p, --stdout                 Muestra la salida por consola
              --ignore-invalid         Ignora líneas inválidas y continúa
          -h, --help                   Muestra esta ayuda
    """.trimIndent())
}

fun validateDateInput(dateStr: String): Boolean {
    return Regex("""^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$""").matches(dateStr)
}

fun parseArgs(args: Array<String>): AppConfig {
    if (args.isEmpty() || args.contains("-h") || args.contains("--help")) {
        printHelp()
        exitProcess(0)
    }

    var inputFile = ""
    var outputFile: String? = null
    var stdout = false
    var dateFrom: String? = null
    var dateTo: String? = null
    var levels: Set<String>? = null
    var isStatsOnly = false
    var isReport = false
    var ignoreInvalid = false

    var i = 0
    while (i < args.size) {
        when (args[i]) {
            "-i", "--input" -> inputFile = args[++i]
            "-o", "--output" -> outputFile = args[++i]
            "-p", "--stdout" -> stdout = true
            "-s", "--stats" -> isStatsOnly = true
            "-r", "--report" -> isReport = true
            "--ignore-invalid" -> ignoreInvalid = true
            "-l", "--level" -> levels = args[++i].split(",").map { it.trim().uppercase() }.toSet()
            "-f", "--from" -> {
                dateFrom = args[++i]
                if (!validateDateInput(dateFrom)) {
                    System.err.println("Error: Formato de fecha inicial (-f) incorrecto.")
                    exitProcess(1)
                }
            }
            "-t", "--to" -> {
                dateTo = args[++i]
                if (!validateDateInput(dateTo)) {
                    System.err.println("Error: Formato de fecha final (-t) incorrecto.")
                    exitProcess(1)
                }
            }
        }
        i++
    }

    if (inputFile.isEmpty()) {
        System.err.println("Error: El fichero de entrada (-i) es obligatorio.")
        exitProcess(1)
    }
    if (!stdout && outputFile == null) {
        System.err.println("Error: Debe indicar una salida por consola (-p) o un fichero (-o).")
        exitProcess(1)
    }
    if (isStatsOnly && isReport) {
        System.err.println("Error: Las opciones --stats y --report son excluyentes.")
        exitProcess(1)
    }

    if (!isStatsOnly && !isReport) {
        isReport = true
    }

    return AppConfig(
        inputFile = inputFile,
        outputFile = outputFile,
        stdout = stdout,
        dateFrom = dateFrom,
        dateTo = dateTo,
        levels = levels,
        isStatsOnly = isStatsOnly,
        isReport = isReport,
        ignoreInvalid = ignoreInvalid
    )
}

fun main(args: Array<String>) {
    val config = parseArgs(args)

    val processor = LogProcessor(config)
    val (stats, entries) = processor.process()

    val reportGen = ReportGenerator()
    val finalOutput = reportGen.generate(config, stats, entries)

    if (config.stdout) {
        println(finalOutput)
    }

    if (config.outputFile != null) {
        try {
            File(config.outputFile).writeText(finalOutput)
            if (!config.stdout) {
                println("Informe generado correctamente en: ${config.outputFile}")
            }
        } catch (e: Exception) {
            System.err.println("Error al escribir el fichero de salida: ${e.message}")
            exitProcess(1)
        }
    }
}
