package cn.mtjsoft.tinypng.plugin.view

import cn.mtjsoft.tinypng.plugin.gui.AutoScriptWindow
import cn.mtjsoft.tinypng.plugin.task.TaskManager
import cn.mtjsoft.tinypng.plugin.utils.FileUtils
import java.awt.Image
import java.awt.Toolkit
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.*
import kotlin.system.exitProcess

class AutoWindow : JDialog() {

    private lateinit var autoScriptWindow: AutoScriptWindow

    private val icon: Image

    private var rpPath = ""
    private var ppPath = ""

    init {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        title = "图片资源自动压缩工具"
        icon = Toolkit.getDefaultToolkit().createImage(this.javaClass.getResource("/image/icon72.png"))
        setIconImage(icon)
    }

    fun showWindow() {
        contentPane.add(AutoScriptWindow().apply {
            autoScriptWindow = this
            rpBtn.addActionListener {
                chooseFile(rp)
            }
            ppBtn.addActionListener {
                chooseFile(pp)
            }
            ok.addActionListener {
                okClick()
            }
            progressBar.isVisible = false
        }.root)
        setSize(500, 600)
        setLocationRelativeTo(null)
        isModal = true
        // 不可缩放
        isResizable = false
        isVisible = true
        addWindowCloseListener(this)
    }

    /**
     * 点击确认
     */
    private fun okClick() {
        autoScriptWindow.apply {
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
            ppPath = pp.text
            if (ppPath.isEmpty()) {
                JOptionPane.showMessageDialog(null, "请选择保存压缩文件夹.", "提示", JOptionPane.ERROR_MESSAGE)
                return
            }
            val dstFile = File(ppPath)
            if (!dstFile.exists()) {
                dstFile.mkdirs()
            }
            // 进度条未走完
            if (progressBar.isVisible && progressBar.maximum > 0 && progressBar.value > 0 && progressBar.value < progressBar.maximum) {
                JOptionPane.showMessageDialog(null, "当前任务还未结束，请稍后.", "提示", JOptionPane.WARNING_MESSAGE)
                return
            }
            result.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE).format(Date()) + "\n"
            val resFiles = FileUtils.findFileList(rpPath)
            // 获取总的需要替换的文件个数
            setProgressBar(resFiles.size)
            printResult("=====================================")
            printResult("====            压缩开始  0%       ====")
            // 压缩
            for (file in resFiles) {
                TaskManager.init.addTask(file, { task ->
                    printResult("====    SUCCESS →  " + task.folderName + "    >>>>    " + task.fileName)
                    updateProgressBar()
                }, { task, errMsg ->
                    printResult("====    ERROR  ↓  " + task.folderName + "    >>>>    " + task.fileName)
                    printResult(errMsg)
                    printResult("====    ERROR  ↑  " + task.folderName + "    >>>>    " + task.fileName)
                    updateProgressBar()
                })
            }
        }
    }

    /**
     * 初始化进度条
     */
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
        if (progress > autoScriptWindow.progressBar.maximum || isOver) {
            progress = autoScriptWindow.progressBar.maximum
            autoScriptWindow.progressBar.isVisible = false
            printResult("=====================================")
            printResult("====          压缩完成  100%      ====")
        }
        autoScriptWindow.progressBar.value = progress
    }

    /**
     * 选择文件
     */
    private fun chooseFile(jTextField: JTextField) {
        val jFrame = JFrame()
        jFrame.iconImage = icon
        val jfc = JFileChooser()
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
    private fun addWindowCloseListener(mJDialog: JDialog) {
        mJDialog.defaultCloseOperation = DO_NOTHING_ON_CLOSE
        mJDialog.addWindowListener(object : WindowListener {
            override fun windowOpened(e: WindowEvent?) {
            }

            override fun windowClosing(e: WindowEvent) {
                val option = JOptionPane.showConfirmDialog(
                    mJDialog, "确定退出吗?", "提示",
                    JOptionPane.YES_NO_OPTION
                )
                if (option == JOptionPane.YES_OPTION) {
                    if (e.window === mJDialog) {
                        mJDialog.dispose()
                        exitProcess(0)
                    } else {
                        return
                    }
                } else if (option == JOptionPane.NO_OPTION) {
                    if (e.window === mJDialog) {
                        return
                    }
                }
            }

            override fun windowClosed(e: WindowEvent?) {
            }

            override fun windowIconified(e: WindowEvent?) {
            }

            override fun windowDeiconified(e: WindowEvent?) {
            }

            override fun windowActivated(e: WindowEvent?) {
            }

            override fun windowDeactivated(e: WindowEvent?) {
            }
        })
    }

}