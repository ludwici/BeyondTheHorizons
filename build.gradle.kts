import earth.terrarium.cloche.api.metadata.CommonMetadata
import org.gradle.api.tasks.bundling.AbstractArchiveTask

plugins {
    java
    id("earth.terrarium.cloche") version "0.18.15"
}

val minecraftVersion: String by project
val parchmentVersion: String by project
val adAstraVersion: String by project
val neoforgeVersion: String by project
val fabricLoaderVersion: String by project
val fabricApiVersion: String by project

base {
    archivesName.set("BeyondTheHorizons")
}

repositories {
    cloche.librariesMinecraft()

    mavenCentral()

    cloche {
        main()
        mavenNeoforged()
        mavenNeoforgedMeta()
        mavenFabric()
    }

    maven(url = "https://maven.teamresourceful.com/repository/maven-public/")

    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
}

cloche {
    minecraftVersion = project.property("minecraftVersion") as String

    metadata {
        modId = "beyond_the_horizons"
        name = "Ad Astra: Beyond The Horizons"
        description = "An exploration expansion for Ad Astra."
        url = "https://github.com/ludwici/BeyondTheHorizons"
        issues = "https://github.com/ludwici/BeyondTheHorizons/issues"
        license = "All Rights Reserved"

        author("ludwici")

        require("ad_astra", adAstraVersion)
    }

    common {
    }

    neoforge {
        loaderVersion = neoforgeVersion
        mixins.from(file("src/neoforge/resources/beyond_the_horizons.mixins.json"))

        dependencies {
            modImplementation("maven.modrinth:3ufwT9JF:9pnKHdCG")
            modImplementation("earth.terrarium.common_storage_lib:common-storage-lib-neoforge-${project.property("minecraftVersion")}:0.0.10")
        }
    }

    fabric {
        loaderVersion = fabricLoaderVersion

        metadata {
            entrypoint("main", "dev.ludwici.beyondthehorizons.fabric.BeyondTheHorizonsFabric::init")

            dependency {
                modId = "fabric-api"
                type = CommonMetadata.Dependency.Type.Required
            }
        }

        dependencies {
            fabricApi(fabricApiVersion)
            modImplementation("maven.modrinth:3ufwT9JF:R1Zh2VaO")
        }
    }

    mappings {
        official()
        parchment(parchmentVersion)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.named<AbstractArchiveTask>("neoforgeIncludeJar") {
    archiveFileName.set("BeyondTheHorizons-neoforge-$minecraftVersion-${project.version}.jar")
}
