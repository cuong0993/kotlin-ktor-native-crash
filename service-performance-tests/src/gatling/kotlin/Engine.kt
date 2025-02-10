internal fun getLongPropertyOr(key: String, fallback: Long): Long {
    return getProperty(key, fallback) {
        System.getProperty(it).toLong()
    }
}

internal fun getDoublePropertyOr(key: String, fallback: Double): Double {
    return getProperty(key, fallback) {
        System.getProperty(it).toDouble()
    }
}

internal fun getStringPropertyOr(key: String, fallback: String): String {
    return getProperty(key, fallback) {
        System.getProperty(it)
    }
}

private inline fun <T> getProperty(key: String, fallback: T, valueGetter: (String) -> (T)): T {
    return try {
        valueGetter.invoke(key)
    } catch (exception: Exception) {
        fallback
    }
}
