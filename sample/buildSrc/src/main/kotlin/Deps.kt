@file:Suppress("ClassName", "ClassNaming", "Filename", "MatchingDeclarationName")

object deps {

    object ktor {
        const val serverCore = "io.ktor:ktor-server-core:${Versions.ktor}"
        const val serverCio = "io.ktor:ktor-server-cio:${Versions.ktor}"
        const val serverStatusPages = "io.ktor:ktor-server-status-pages:${Versions.ktor}"
        const val clientCore = "io.ktor:ktor-client-core:${Versions.ktor}"
        const val clientCurl = "io.ktor:ktor-client-curl:${Versions.ktor}"
    }
}
