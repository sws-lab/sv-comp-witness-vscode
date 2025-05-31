// sv-comp-witness-vscode/build.gradle.kts
plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
    id("com.google.protobuf") version "0.9.4"
    id("org.hidetake.ssh") version "2.11.2"
}

repositories {
    mavenCentral()
}

val kotlinVersion = "2.1.20"
val serializationVersion = "1.8.1"
val protocVersion = "3.25.5"
val grpcVersion = "1.70.0"

dependencies {
    // LSP4J
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:0.23.1")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j.jsonrpc:0.23.1")
    
    // Logging
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")
    
    // JSON processing
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.2")
    
    // gRPC Dependencies
    implementation("io.grpc:grpc-netty:${grpcVersion}")
    implementation("io.grpc:grpc-protobuf:${grpcVersion}")
    implementation("io.grpc:grpc-stub:${grpcVersion}")
    implementation("com.google.protobuf:protobuf-java:4.29.3")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${serializationVersion}")
    
    // ANTLR
    implementation("org.antlr:antlr4-runtime:4.13.2")
    
    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test:${kotlinVersion}")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:${kotlinVersion}")
    
    // YAML Processing
    implementation("com.charleskorn.kaml:kaml-jvm:0.74.0")
    
    // KSMT
    implementation("io.ksmt:ksmt-core:0.5.30")
    implementation("io.ksmt:ksmt-z3-core:0.5.30")
    implementation("io.ksmt:ksmt-z3-native-linux-x64:0.5.30")
    
    // Graph Library
    implementation("org.jgrapht:jgrapht-core:1.5.2")
}

// ANTLR Configuration
tasks.register<Exec>("generateAntlrSources") {
    commandLine("mkdir", "-p", "src/gen/java")
    doLast {
        exec {
            commandLine(
                "java", "-jar", "/usr/local/lib/antlr-4.13.2-complete.jar",
                "-visitor", "-encoding", "utf-8",
                "-o", "src/gen/java",
                "src/main/antlr4/org/example/Grammar.g4"
            )
        }
    }
}

// Protobuf Configuration
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${protocVersion}"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${grpcVersion}"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("grpc")
            }
        }
    }
}

// Configure source sets to include generated sources
sourceSets {
    main {
        java {
            srcDirs(
                "src/main/kotlin",
                "src/gen/java",
                "${buildDir}/generated/source/proto/main/java",
                "${buildDir}/generated/source/proto/main/grpc"
            )
        }
    }
}

// Shadow JAR Configuration
tasks.shadowJar {
    manifest {
        attributes["Main-Class"] = "Main"
    }
    archiveBaseName.set("sv-comp-witness-vscode")
    archiveClassifier.set("")
    archiveVersion.set("")
    
    // Filter out unnecessary files
    exclude("META-INF/*")
    exclude("META-INF/versions/**")
    exclude("about.html")
    exclude("module-info.class")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
