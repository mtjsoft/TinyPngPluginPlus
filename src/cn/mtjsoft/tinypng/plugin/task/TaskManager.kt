package cn.mtjsoft.tinypng.plugin.task

import com.tinify.Tinify
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.Executors

class TaskManager {

    companion object {
        private val mTaskManager: TaskManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            TaskManager()
        }

        private val readyQ = ConcurrentLinkedDeque<CompressTask>()

        private const val runingMaxNumber = 6

        private val runningQ = ConcurrentLinkedDeque<CompressTask>()

        private val cachedThreadPool = Executors.newCachedThreadPool()

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
        onSuccess: (task: CompressTask) -> Unit,
        onError: (task: CompressTask, errMsg: String) -> Unit
    ) {
        addTask(
            CompressTask(
                UUID.randomUUID().toString(),
                file.parentFile.name,
                file.name,
                file.absolutePath,
                if (ppPath.isEmpty()) file.absolutePath else ppPath + File.separator + file.name,
                onSuccess,
                onError
            )
        )
    }

    /**
     * 开始执行压缩任务
     */
    private fun startNextTask(it: CompressTask) {
        cachedThreadPool.execute {
            // 压缩图像
            // 您可以将任何 WebP、JPEG 或 PNG 图像上传到 Tinify API 以对其进行压缩。
            // 我们将自动检测图像的类型并相应地使用 TinyPNG 或 TinyJPG 引擎进行优化。
            // 只要您上传文件或提供图像的 URL，压缩就会开始。=
            try {
                val srcPath = it.srcPath
                val dstPath = it.dstPath
                val source = Tinify.fromFile(srcPath)
                source.toFile(dstPath)
                it.onSuccess.invoke(it)
            } catch (e: Exception) {
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