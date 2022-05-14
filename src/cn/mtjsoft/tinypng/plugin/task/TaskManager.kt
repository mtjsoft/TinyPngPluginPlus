package cn.mtjsoft.tinypng.plugin.task

import cn.mtjsoft.tinypng.plugin.utils.FileUtils
import com.tinify.Tinify
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.Executors

class TaskManager {

    companion object {
        /**
         * 单例
         */
        private val mTaskManager: TaskManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            TaskManager()
        }

        /**
         * 等待压缩队列
         */
        private val readyQ = ConcurrentLinkedDeque<CompressTask>()

        /**
         * 最多同时执行的压缩任务
         */
        private const val runingMaxNumber = 5

        /**
         * 正在执行的压缩任务
         */
        private val runningQ = ConcurrentLinkedDeque<CompressTask>()

        /**
         * 线程池
         */
        private val cachedThreadPool = Executors.newCachedThreadPool()

        /**
         * 单例实例
         */
        val init = mTaskManager
    }

    /**
     * 添加任务
     */
    @Synchronized
    fun addTask(
        task: CompressTask
    ) {
        if (runningQ.size < runingMaxNumber) {
            runningQ.add(task)
            startNextTask(task)
        } else {
            readyQ.add(task)
        }
    }

    @Synchronized
    fun addTask(
        file: File,
        ppPath: String,
        onStart: (task: CompressTask) -> Unit,
        onSuccess: (task: CompressTask) -> Unit,
        onError: (task: CompressTask, errMsg: String) -> Unit
    ) {
        addTask(
            CompressTask(
                UUID.randomUUID().toString(),
                file.parentFile.name,
                file.name,
                file.absolutePath,
                when{
                    ppPath.isEmpty() -> file.absolutePath
                    FileUtils.isCanAddCompressTask(ppPath) -> ppPath
                    else -> ppPath + File.separator + file.name
                },
                if (file.length() > 0) file.length() else 1,
                0,
                0,
                0,
                0,
                onStart,
                onSuccess,
                onError
            )
        )
    }

    /**
     * 清空停止任务
     */
    fun cleanTask() {
        try {
            readyQ.clear()
        } catch (e: Exception) {
        }
    }

    /**
     * 开始执行压缩任务
     */
    private fun startNextTask(it: CompressTask) {
        cachedThreadPool.execute {
            // 压缩图像
            // 您可以将任何 WebP、JPEG 或 PNG 图像上传到 Tinify API 以对其进行压缩。
            // 我们将自动检测图像的类型并相应地使用 TinyPNG 或 TinyJPG 引擎进行优化。
            // 只要您上传文件或提供图像的 URL，压缩就会开始。
            try {
                it.startTime = System.currentTimeMillis()
                it.onStart.invoke(it)
                val srcPath = it.srcPath
                val dstPath = it.dstPath
                val source = Tinify.fromFile(srcPath)
                source.toFile(dstPath)
                it.newSize = File(dstPath).length()
                it.percentage = when {
                    it.newSize > 0 && it.newSize < it.oldSize -> {
                        ((it.oldSize - it.newSize).toFloat() / it.oldSize * 100).toInt()
                    }
                    else -> 0
                }
                it.endTime = System.currentTimeMillis()
                it.onSuccess.invoke(it)
            } catch (e: Exception) {
                it.endTime = System.currentTimeMillis()
                it.onError.invoke(it, e.message ?: "压缩发生未知错误")
            } finally {
                // 任务结束
                promoteCalls(it)
            }
        }
    }

    /**
     * 任务调度执行
     */
    @Synchronized
    private fun promoteCalls(task: CompressTask) {
        if (!runningQ.remove(task)) {
            throw AssertionError("Call wasn't in-flight!")
        }
        if (getTaskRunningCount() >= runingMaxNumber) {
            return
        }
        if (getTaskReadyCount() == 0) {
            return
        }
        val iterator = readyQ.iterator()
        while (iterator.hasNext()) {
            val nextTask = iterator.next()
            runningQ.add(nextTask)
            iterator.remove()
            startNextTask(nextTask)
            if (getTaskRunningCount() >= runingMaxNumber) {
                return
            }
        }
    }

    private fun getTaskRunningCount(): Int = runningQ.size

    private fun getTaskReadyCount(): Int = readyQ.size
}