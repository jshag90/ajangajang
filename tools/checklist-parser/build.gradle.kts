plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(libs.jsoup)
    implementation(libs.kotlinx.serialization.json)
}

application {
    mainClass.set("com.dodamsoft.ajangajang.tools.checklistparser.MainKt")
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}
