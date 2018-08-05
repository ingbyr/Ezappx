package com.ezappx.builder.services

import com.ezappx.builder.utils.Cordova
import com.ezappx.builder.utils.ProcessUtils
import java.nio.file.Files
import java.nio.file.StandardOpenOption

class AndroidAppBuilder : AbstractMobileAppBuilder() {

    var androidMinSDK: String? = null

    override fun initProject() {
        if (!Files.exists(projectDir)) {
            info("init ezappx-cordova project at ${projectDir.toAbsolutePath()}")
            ProcessUtils.exec(userProjectDir, Cordova.create(project.projectName, projectPackage, project.projectName))
        }
    }

    override fun addPlatform() {
        // 添加Android编译环境
        if (!Files.exists(projectDir.resolve("platforms").resolve("android"))) {
            debug("add android platform")
            // TODO 安卓版本问题，插件版本不同支持的安卓版本范围不同 https://cordova.apache.org/docs/en/latest/guide/platforms/android/index.html
            ProcessUtils.exec(projectDir, Cordova.addPlatform(project.mobileOS, version ="6.4.0"))
        }

        // 设置最小SDK版本号
        if (androidMinSDK != null) {
            val gradleSettings = projectDir.resolve("platforms").resolve("android").resolve("gradle.properties")
            Files.write(gradleSettings, "cdvMinSdkVersion=$androidMinSDK".toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.WRITE)
        }
    }

    override fun addCordovaPlugins() {
        debug("add cordova plugins")
        for (plugin in project.cordovaPlugins) {
            ProcessUtils.exec(projectDir, Cordova.addPlugin(plugin))
        }
    }

    override fun build() {
        debug("build app project")
        ProcessUtils.exec(projectDir, Cordova.build(project.mobileOS))
    }

}

fun androidBuilder(init: AndroidAppBuilder.() -> Unit): AndroidAppBuilder {
    val androidAppBuilder = AndroidAppBuilder()
    androidAppBuilder.init()
    androidAppBuilder.initBuilderArgs()
    androidAppBuilder.initProject()
    androidAppBuilder.addResources()
    androidAppBuilder.addPlatform()
    androidAppBuilder.addCordovaPlugins()
    androidAppBuilder.build()
    return androidAppBuilder
}