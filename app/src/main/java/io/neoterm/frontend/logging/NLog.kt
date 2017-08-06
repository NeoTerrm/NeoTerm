package io.neoterm.frontend.logging

import android.content.Context
import android.support.annotation.IntDef
import android.util.Log
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater

object NLog {

    const val V = Log.VERBOSE
    const val D = Log.DEBUG
    const val I = Log.INFO
    const val W = Log.WARN
    const val E = Log.ERROR
    const val A = Log.ASSERT

    @IntDef(V.toLong(), D.toLong(), I.toLong(), W.toLong(), E.toLong(), A.toLong())
    @Retention(AnnotationRetention.SOURCE)
    private annotation class TYPE

    private val T = charArrayOf('V', 'D', 'I', 'W', 'E', 'A')
    private val FILE = 0x10

    private var executor: ExecutorService? = null
    private var logDir: String? = null       // log存储目录

    private var sLogSwitch = true // log总开关，默认开
    private var sLog2ConsoleSwitch = true // logcat是否打印，默认打印
    private var sGlobalTag: String = "" // log标签
    private var sTagIsSpace = true // log标签是否为空白
    private var sLogHeadSwitch = true // log头部开关，默认开
    private var sLog2FileSwitch = false// log写入文件开关，默认关
    private var sConsoleFilter = V    // log控制台过滤器
    private var sFileFilter = V    // log文件过滤器

    private val LINE_SEP = System.getProperty("line.separator")
    private val MAX_LEN = 4000
    private val FORMAT = SimpleDateFormat("MM-dd HH:mm:ss.SSS ", Locale.getDefault())

    private val NULL_TIPS = "Log with null object."
    private val ARGS = "args"

    fun init(context: Context) {
        logDir = context.getDir("logs", Context.MODE_PRIVATE).absolutePath
        sGlobalTag = "NeoTerm"
    }

    fun v(contents: Any) {
        log(V, sGlobalTag, contents)
    }

    fun v(tag: String, vararg contents: Any) {
        log(V, tag, *contents)
    }

    fun d(contents: Any) {
        log(D, sGlobalTag, contents)
    }

    fun d(tag: String, vararg contents: Any) {
        log(D, tag, *contents)
    }

    fun i(contents: Any) {
        log(I, sGlobalTag, contents)
    }

    fun i(tag: String, vararg contents: Any) {
        log(I, tag, *contents)
    }

    fun w(contents: Any) {
        log(W, sGlobalTag, contents)
    }

    fun w(tag: String, vararg contents: Any) {
        log(W, tag, *contents)
    }

    fun e(contents: Any) {
        log(E, sGlobalTag, contents)
    }

    fun e(tag: String, vararg contents: Any) {
        log(E, tag, *contents)
    }

    fun a(contents: Any) {
        log(A, sGlobalTag, contents)
    }

    fun a(tag: String, vararg contents: Any) {
        log(A, tag, *contents)
    }

    fun file(contents: Any) {
        log(FILE or D, sGlobalTag, contents)
    }

    fun file(@TYPE type: Int, contents: Any) {
        log(FILE or type, sGlobalTag, contents)
    }

    fun file(tag: String, contents: Any) {
        log(FILE or D, tag, contents)
    }

    fun file(@TYPE type: Int, tag: String, contents: Any) {
        log(FILE or type, tag, contents)
    }

    private fun log(type: Int, tag: String, vararg contents: Any) {
        if (!sLogSwitch || !sLog2ConsoleSwitch && !sLog2FileSwitch) return
        val type_low = type and 0x0f
        val type_high = type and 0xf0
        if (type_low < sConsoleFilter && type_low < sFileFilter) return
        val tagAndHead = processTagAndHead(tag)
        val body = processBody(*contents)
        if (sLog2ConsoleSwitch && type_low >= sConsoleFilter) {
            printToConsole(type_low, tagAndHead[0], tagAndHead[1] + body)
        }
        if (sLog2FileSwitch || type_high == FILE) {
            if (type_low >= sFileFilter) printToFile(type_low, tagAndHead[0], tagAndHead[2] + body)
        }
    }

    private fun processTagAndHead(tag: String): Array<String> {
        var returnTag = tag
        if (!sTagIsSpace && !sLogHeadSwitch) {
            returnTag = sGlobalTag
        } else {
            returnTag = "$sGlobalTag-$returnTag"
            val targetElement = Throwable().stackTrace[3]
            var className = targetElement.className
            val classNameInfo = className.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (classNameInfo.isNotEmpty()) {
                className = classNameInfo[classNameInfo.size - 1]
            }
            if (className.contains("$")) {
                className = className.split("\\$".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            }
            if (sTagIsSpace) {
                returnTag = if (isSpace(returnTag)) className else returnTag
            }
            if (sLogHeadSwitch) {
                val head = Formatter()
                        .format("%s, %s(%s:%d)",
                                Thread.currentThread().name,
                                targetElement.methodName,
                                targetElement.fileName,
                                targetElement.lineNumber)
                        .toString()
                return arrayOf(returnTag, head + LINE_SEP, " [$head]: ")
            }
        }
        return arrayOf(returnTag, "", ": ")
    }

    private fun processBody(vararg contents: Any): String {
        var body = NULL_TIPS
        if (contents.isNotEmpty()) {
            if (contents.size == 1) {
                body = contents[0].toString()
            } else {
                body = buildString {
                    var index = 0
                    contents.forEach {
                        append(ARGS)
                                .append("[")
                                .append(index++)
                                .append("]")
                                .append(" = ")
                                .append(it.toString())
                                .append(LINE_SEP)
                    }
                }
            }
        }
        return body
    }

    private fun printToConsole(type: Int, tag: String, msg: String) {
        val len = msg.length
        val countOfSub = len / MAX_LEN
        if (countOfSub > 0) {
            print(type, tag, msg.substring(0, MAX_LEN))
            var sub: String
            var index = MAX_LEN
            for (i in 1..countOfSub - 1) {
                sub = msg.substring(index, index + MAX_LEN)
                print(type, tag, sub)
                index += MAX_LEN
            }
            sub = msg.substring(index, len)
            print(type, tag, sub)
        } else {
            print(type, tag, msg)
        }
    }

    private fun print(type: Int, tag: String, msg: String) {
        Log.println(type, tag, msg)
    }

    private fun printToFile(type: Int, tag: String, msg: String) {
        val now = Date(System.currentTimeMillis())
        val format = FORMAT.format(now)
        val date = format.substring(0, 5)
        val time = format.substring(6)
        val fullPath = logDir + date + ".txt"
        if (!createOrExistsFile(fullPath)) {
            Log.e(tag, "log to $fullPath failed!")
            return
        }
        val sb = StringBuilder()
        sb.append(time)
                .append(T[type - V])
                .append("/")
                .append(tag)
                .append(msg)
                .append(LINE_SEP)
        val content = sb.toString()
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor()
        }
        executor!!.execute {
            var bw: BufferedWriter? = null
            try {
                bw = BufferedWriter(FileWriter(fullPath, true))
                bw.write(content)
                Log.d(tag, "log to $fullPath success!")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(tag, "log to $fullPath failed!")
            } finally {
                try {
                    if (bw != null) {
                        bw.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }

    private fun createOrExistsFile(filePath: String): Boolean {
        val file = File(filePath)
        if (file.exists()) return file.isFile
        if (!createOrExistsDir(file.parentFile)) return false
        try {
            return file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }

    }

    private fun createOrExistsDir(file: File?): Boolean {
        return file != null && if (file.exists()) file.isDirectory else file.mkdirs()
    }

    private fun isSpace(s: String?): Boolean {
        return s?.isEmpty() ?: true
    }

    fun compress(input: ByteArray): ByteArray {
        val bos = ByteArrayOutputStream()
        val compressor = Deflater(1)
        try {
            compressor.setInput(input)
            compressor.finish()
            val buf = ByteArray(2048)
            while (!compressor.finished()) {
                val count = compressor.deflate(buf)
                bos.write(buf, 0, count)
            }
        } finally {
            compressor.end()
        }
        return bos.toByteArray()
    }

    fun uncompress(input: ByteArray): ByteArray {
        val bos = ByteArrayOutputStream()
        val inflater = Inflater()
        try {
            inflater.setInput(input)
            val buf = ByteArray(2048)
            while (!inflater.finished()) {
                var count = 0
                try {
                    count = inflater.inflate(buf)
                } catch (e: DataFormatException) {
                    e.printStackTrace()
                }

                bos.write(buf, 0, count)
            }
        } finally {
            inflater.end()
        }
        return bos.toByteArray()
    }

}
