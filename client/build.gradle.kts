val appName = "Client"
val gdxVersion = "1.9.10"
val roboVMVersion = "2.3.7"
val box2DLightsVersion = "1.4"
val ashleyVersion = "1.7.0"
val aiVersion = "1.8.0"

dependencies {
    implementation(project(":util"))

    api("com.badlogicgames.gdx:gdx-backend-lwjgl:$gdxVersion")
    api("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
    api("com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-desktop")
    api("com.badlogicgames.gdx:gdx:$gdxVersion")
    api("com.badlogicgames.gdx:gdx-box2d:$gdxVersion")
}

tasks.register("fatJar", type = Jar::class) {
    from(configurations.runtimeClasspath.get().map{ if (it.isDirectory) it else zipTree(it) })

    manifest {
        attributes["Main-Class"] = "com.example.matchmaking.client.DesktopLauncher"
    }

    dependsOn (
            rootProject.tasks.getByName("proto"),
            rootProject.tasks.getByPath("util:build")
    )
    mustRunAfter(
            rootProject.tasks.getByName("proto"),
            rootProject.tasks.getByPath("util:build")
    )

    with(tasks["jar"] as CopySpec)
}

tasks {
    "build" {
        dependsOn("fatJar")
    }
}