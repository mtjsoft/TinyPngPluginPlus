package cn.mtjsoft.tinypng.plugin.utils

import java.io.File
import java.util.*


/**
 * 文件工具
 *
 * @author mtj
 * @date 2021-11-18 16:28:14
 */
object FileUtils {
    fun findFileList(path: String): List<File> {
        val list: MutableList<File> = LinkedList()
        try {
            findFileList(File(path), list, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun findFileList(dir: File, fileList: MutableList<File>, isAll: Boolean) {
        if (!dir.exists()) {
            return
        }
        if (dir.isFile) {
            if (isCanAddCompressTask(dir.name)) {
                fileList.add(dir)
            }
            return
        }
        val files = dir.list()
        if (files != null) {
            for (s in files) {
                val file = File(dir, s)
                if (file.isFile) {
                    // 过滤出支持的图片文件
                    if (isCanAddCompressTask(file.name)) {
                        fileList.add(file)
                    }
                } else if (file.isDirectory && isAll) {
                    findFileList(file, fileList, true)
                }
            }
        }
    }

    /**
     * 是否是支持压缩的文件类型
     */
    fun isCanAddCompressTask(fileName: String): Boolean {
        val name = fileName.toLowerCase()
        return name.endsWith(".webp") || name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg")
    }
}