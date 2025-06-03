import java.util.Calendar

plugins {
    id("java-library")
    id("net.neoforged.licenser")
    id("neoforge.formatting-conventions")
    kotlin("jvm")
}



dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    compileOnly(platform("net.neoforged:minecraft-dependencies:${project.property("minecraft_version")}"))
    compileOnly("org.slf4j:slf4j-api")
    implementation(kotlin("stdlib-jdk8"))
    implementation("dev.vankka:dependencydownload-runtime:1.3.1")
    implementation("me.tongfei:progressbar:0.10.1")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

license {
    header(rootProject.file("codeformat/MAGMA-HEADER.txt"))
    properties {
        this["year"] = Calendar.getInstance().get(Calendar.YEAR).toString()
    }
    include("**/*.java", "**/*.kt")
}

tasks.jar.configure {
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
