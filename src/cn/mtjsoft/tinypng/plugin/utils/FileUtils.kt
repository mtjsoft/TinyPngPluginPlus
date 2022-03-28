package cn.mtjsoft.tinypng.plugin.utils

import java.io.*
import java.util.*


/**
 * 文件工具
 *
 * @author mtj
 * @date 2021-11-18 16:28:14
 */
object FileUtils {

    fun copyFile(srcPath: String, dstPath: String) {
        File(srcPath).runCatching {
            takeIf {
                it.exists()
            }?.inputStream()?.use { inputStream ->
                File(dstPath).outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }.onFailure {
            // print or throw
            it.printStackTrace()
        }
    }

    fun writeStringToFile(filePath: String, saveString: String) {
        try {
            val fw = FileWriter(filePath, true)
            val bw = BufferedWriter(fw)
            bw.write(saveString)
            bw.close()
            fw.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun findAllFileList(path: String): List<File> {
        val list: MutableList<File> = LinkedList()
        try {
            findFileList(File(path), list, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun findFileList(path: String): List<File> {
        val list: MutableList<File> = LinkedList()
        try {
            findFileList(File(path), list, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun findFileNameList(path: String): List<String> {
        val list: MutableList<String> = LinkedList()
        try {
            val dir = File(path)
            if (!dir.exists() || !dir.isDirectory) {
                return list
            }
            val filesName = dir.list()
            if (filesName != null) {
                list.addAll(Arrays.asList(*filesName))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun findFileList(dir: File, fileList: MutableList<File>, isAll: Boolean) {
        if (!dir.exists() || !dir.isDirectory) {
            return
        }
        if (dir.isFile) {
            fileList.add(dir)
            return
        }
        val files = dir.list()
        if (files != null) {
            for (s in files) {
                val file = File(dir, s)
                if (file.isFile) {
                    val name = file.name.toLowerCase()
                    // 过滤出支持的图片文件
                    if (name.endsWith(".webp") || name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg")) {
                        fileList.add(file)
                    }
                } else if (isAll) {
                    findFileList(file, fileList, true)
                }
            }
        }
    }
}