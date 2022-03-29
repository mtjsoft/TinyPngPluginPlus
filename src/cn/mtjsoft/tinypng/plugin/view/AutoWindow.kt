package cn.mtjsoft.tinypng.plugin.view

import cn.mtjsoft.tinypng.plugin.gui.AutoScriptWindow
import cn.mtjsoft.tinypng.plugin.gui.GradientProgressBarUI
import cn.mtjsoft.tinypng.plugin.gui.ProgressUI
import cn.mtjsoft.tinypng.plugin.task.TaskManager
import cn.mtjsoft.tinypng.plugin.utils.CacheUtils
import cn.mtjsoft.tinypng.plugin.utils.FileUtils
import com.tinify.Tinify
import java.awt.Color
import java.awt.Desktop
import java.awt.Image
import java.awt.Toolkit
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.io.File
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.*
import kotlin.system.exitProcess

class AutoWindow : JFrame() {

    private lateinit var autoScriptWindow: AutoScriptWindow

    private val icon: Image

    private var apiKey = ""
    private var rpPath = ""
    private var ppPath = ""

    @Volatile
    private var startTPing = false

    private var startTime = System.currentTimeMillis()
    private var oldFileAllSize: Long = 0
    private var newFileAllSize: Long = 0

    init {
        title = "图片资源自动压缩工具"
        icon = Toolkit.getDefaultToolkit().createImage(this.javaClass.getResource("/image/icon72.png"))
        setIconImage(icon)
    }

    fun showWindow() {
        startTPing = false
        contentPane.add(AutoScriptWindow().apply {
            autoScriptWindow = this
            rpBtn.addActionListener {
                chooseFile(rp)
            }
            ppBtn.addActionListener {
                chooseFile(pp)
            }
            getKeyBtn.addActionListener {
                // 打开地址 https://tinypng.com/developers
                browse2("https://tinypng.com/developers")
            }
            ok.addActionListener {
                okClick()
            }
            // 默认进度条显示
            GradientProgressBarUI().also {
                progressBar.ui = it
            }
            progressBar.value = 0
            progressBar.minimum = 0
            progressBar.maximum = 100
            progressBar.isVisible = true
            // 设置执行日志不可编辑
            result.isEditable = false
            // 设置缓存数据
            apiKeyTF.text = CacheUtils.getApiKey()
            rp.text = CacheUtils.getPath1()
            pp.text = CacheUtils.getPath2()

        }.root)
        setSize(530, 630)
        setLocationRelativeTo(null)
        // 不可缩放
        isResizable = false
        isVisible = true
        addWindowCloseListener(this)
    }

    /**
     * @title 使用默认浏览器打开
     * @param url 要打开的网址
     */
    private fun browse2(url: String) {
        val desktop: Desktop = Desktop.getDesktop()
        if (Desktop.isDesktopSupported() && desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.browse(URI(url))
        }
    }

    /**
     * 点击确认
     */
    private fun okClick() {
        autoScriptWindow.apply {
            // 正在进行中，请稍后
            if (startTPing || (progressBar.maximum > 0 && progressBar.value > 0 && progressBar.value < progressBar.maximum)) {
                JOptionPane.showMessageDialog(null, "当前任务还未结束，请稍后.", "提示", JOptionPane.WARNING_MESSAGE)
                return
            }
            apiKey = apiKeyTF.text
            if (apiKey.isEmpty()) {
                JOptionPane.showMessageDialog(null, "请获取ApiKey并输入.", "提示", JOptionPane.ERROR_MESSAGE)
                return
            }
            CacheUtils.saveApiKey(apiKey)
            // 54vYvMTl6s36kN1q802ln0XqZMl5Q2tg
            Tinify.setKey(apiKey)
            rpPath = rp.text
            if (rpPath.isEmpty()) {
                JOptionPane.showMessageDialog(null, "请选择待压缩资源图片文件夹.", "提示", JOptionPane.ERROR_MESSAGE)
                return
            }
            val rpFileNames = FileUtils.findFileNameList(rpPath)
            if (rpFileNames.isEmpty()) {
                JOptionPane.showMessageDialog(null, "待压缩资源图片文件夹为空.", "错误", JOptionPane.ERROR_MESSAGE)
                return
            }
            CacheUtils.savePath1(rpPath)
            ppPath = pp.text
            if (ppPath.isEmpty()) {
                JOptionPane.showMessageDialog(null, "请选择保存压缩文件夹.", "提示", JOptionPane.ERROR_MESSAGE)
                return
            }
            val dstFile = File(ppPath)
            if (!dstFile.exists()) {
                dstFile.mkdirs()
            }
            CacheUtils.savePath2(ppPath)
            startTPing = true
            result.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE).format(Date()) + "\n"
            val resFiles = FileUtils.findFileList(rpPath)
            if (resFiles.isEmpty()) {
                printResult("==== 当前文件夹没有可压缩的图片")
                startTPing = false
                return
            }
            // 获取总的需要替换的文件个数
            setProgressBar(resFiles.size)
            printResult("=====================================")
            printResult("====压缩开始  0%")
            // 压缩
            startTime = System.currentTimeMillis()
            oldFileAllSize = 0
            newFileAllSize = 0
            for (file in resFiles) {
                TaskManager.init.addTask(file, ppPath, { task ->
                    printResult("==Task Start → ${task.fileName}")
                }, { task ->
                    oldFileAllSize += task.oldSize
                    newFileAllSize += task.newSize
                    printResult("==SUCCESS → ${task.fileName}，压缩比: -${task.percentage}%，耗时：${task.endTime - task.startTime}ms")
                    updateProgressBar()
                }, { task, errMsg ->
                    printResult("==ERROR ↓ ${task.fileName} 耗时：${task.endTime - task.startTime}ms")
                    printResult(errMsg)
                    printResult("==ERROR ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ")
                    updateProgressBar()
                })
            }
        }
    }

    /**
     * 初始化进度条
     */
    @Synchronized
    private fun setProgressBar(size: Int) {
        autoScriptWindow.apply {
            progressBar.isVisible = size > 0
            progressBar.value = 0
            progressBar.minimum = 0
            progressBar.maximum = size
        }
    }

    /**
     * 每循环一个文件，进度 + 1
     */
    @Synchronized
    private fun updateProgressBar(addNUm: Int = 1, isOver: Boolean = false) {
        var progress = autoScriptWindow.progressBar.value + addNUm
        if (progress >= autoScriptWindow.progressBar.maximum || isOver) {
            progress = autoScriptWindow.progressBar.maximum
            startTPing = false
            printResult("=====================================")
            printResult("====压缩完成  100%")
            printResult("====压缩总比例： -${((oldFileAllSize - newFileAllSize).toFloat() / oldFileAllSize * 100).toInt()}%   -${(oldFileAllSize - newFileAllSize) / 1024}Kb")
            printResult("====压缩总耗时： ${System.currentTimeMillis() - startTime}ms")
        }
        autoScriptWindow.progressBar.value = progress
    }

    /**
     * 选择文件
     */
    private fun chooseFile(jTextField: JTextField) {
        val jFrame = JFrame()
        jFrame.iconImage = icon
        val defPath = if (jTextField.text.isNotEmpty()) jTextField.text else CacheUtils.getProjectFilePath()
        val jfc = if (defPath.isEmpty()) JFileChooser() else JFileChooser(defPath)
        jfc.dialogTitle = "选择文件夹"
        jfc.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        val returnVal = jfc.showOpenDialog(jFrame)
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            val file = jfc.selectedFile
            file?.let {
                if (it.isDirectory) {
                    jTextField.text = it.absolutePath
                }
            }
        }
    }

    /**
     * 输出结果
     */
    @Synchronized
    private fun printResult(s: String, isLine: Boolean = true) {
        autoScriptWindow.result.apply {
            append(s)
            if (isLine) {
                append("\n")
            }
        }
        // 处理自动滚动到最底部
        autoScriptWindow.result.caretPosition = autoScriptWindow.result.text.length
    }

    /**
     * 处理关闭
     */
    private fun addWindowCloseListener(mJFrame: JFrame) {
        mJFrame.defaultCloseOperation = DO_NOTHING_ON_CLOSE
        mJFrame.addWindowListener(object : WindowListener {
            override fun windowOpened(e: WindowEvent?) {
                println("1")
            }

            override fun windowClosing(e: WindowEvent) {
                println("2")
                val option = JOptionPane.showConfirmDialog(
                    mJFrame, if (startTPing) "当前压缩任务还在进行中，确定要退出吗?" else "确定退出吗?", "提示",
                    JOptionPane.YES_NO_OPTION
                )
                if (option == JOptionPane.YES_OPTION) {
                    if (e.window === mJFrame) {
                        TaskManager.init.cleanTask()
                        mJFrame.dispose()
//                        exitProcess(0)
                    } else {
                        return
                    }
                } else if (option == JOptionPane.NO_OPTION) {
                    if (e.window === mJFrame) {
                        return
                    }
                }
            }

            override fun windowClosed(e: WindowEvent?) {
                println("3")
            }

            override fun windowIconified(e: WindowEvent?) {
                println("4")
            }

            override fun windowDeiconified(e: WindowEvent?) {
                println("5")
            }

            override fun windowActivated(e: WindowEvent?) {
                println("6")
            }

            override fun windowDeactivated(e: WindowEvent) {
                println("7")
            }
        })
    }

}