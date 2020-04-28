plugins {
    java
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")

    group = "com.example.matchmaking"

    repositories {
        mavenCentral()
        jcenter()
        google()
    }

    dependencies {
        "testImplementation"("junit:junit:4.12")
    }
}

val workingDirectory = "workingDirectory"

tasks.register("proto", type = Exec::class) {
    commandLine("protoc", "-I=protos/", "--java_out=util/src/main/java", "protos/connection.proto")
}

tasks.register("clear", type = Delete::class) {
    delete(workingDirectory)
}

tasks.register("move", type = Copy::class) {
    from("client/build/libs", "matchmaker/build/libs")
    into(workingDirectory)
    dependsOn("client:build", "matchmaker:build")
    mustRunAfter("client:build", "matchmaker:build")
}

tasks {
    "build" {
        mustRunAfter("move")
        dependsOn("move")
    }
}

tasks.register("runMatchmaker", type = JavaExec::class) {
    main = "-jar"
    args("matchmaker.jar")
    isIgnoreExitValue = true
    workingDir = File(workingDirectory)

    dependsOn("build")
    mustRunAfter("build")
}

tasks.register("runClient", type = JavaExec::class) {
    main = "-jar"
    args("client.jar")
    isIgnoreExitValue = true
    workingDir = File(workingDirectory)

    dependsOn("build")
    mustRunAfter("build")
}
