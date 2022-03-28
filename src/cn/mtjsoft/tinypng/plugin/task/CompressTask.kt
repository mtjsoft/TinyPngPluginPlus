package cn.mtjsoft.tinypng.plugin.task

data class CompressTask(
    val taskId: String,
    val folderName: String,
    val fileName: String,
    val srcPath: String,
    val dstPath: String,
    val oldSize: Long,
    var newSize: Long,
    var percentage: Int,
    var startTime: Long,
    var endTime: Long,
    val onStart: (task: CompressTask) -> Unit,
    val onSuccess: (task: CompressTask) -> Unit,
    val onError: (task: CompressTask, errMsg: String) -> Unit
)