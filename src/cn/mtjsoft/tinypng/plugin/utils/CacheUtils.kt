package cn.mtjsoft.tinypng.plugin.utils

import com.intellij.ide.util.PropertiesComponent

/**
 * 缓存数据
 */
object CacheUtils {

    /**
     * 缓存 ApiKey
     */
    fun saveApiKey(value: String) {
        PropertiesComponent.getInstance().setValue("apiKey", value)
    }

    fun getApiKey(): String = PropertiesComponent.getInstance().getValue("apiKey", "")

    /**
     * 缓存 ProjectFilePath
     */
    fun saveProjectFilePath(value: String) {
        PropertiesComponent.getInstance().setValue("projectFilePath", value)
    }

    fun getProjectFilePath(): String = PropertiesComponent.getInstance().getValue("projectFilePath", "")

    /**
     * 缓存 path1
     */
    fun savePath1(value: String) {
        PropertiesComponent.getInstance().setValue("path1", value)
    }

    fun getPath1(): String = PropertiesComponent.getInstance().getValue("path1", "")

    /**
     * 缓存 path2
     */
    fun savePath2(value: String) {
        PropertiesComponent.getInstance().setValue("path2", value)
    }

    fun getPath2(): String = PropertiesComponent.getInstance().getValue("path2", "")
}