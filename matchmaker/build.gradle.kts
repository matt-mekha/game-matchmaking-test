dependencies {
    implementation(project(":util"))
}

tasks.register("fatJar", type = Jar::class) {
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })

    manifest {
        attributes["Main-Class"] = "com.example.matchmaking.matchmaker.Main"
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