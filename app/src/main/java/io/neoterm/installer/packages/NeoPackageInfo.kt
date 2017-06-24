package io.neoterm.installer.packages

/**
 * @author kiva
 */

class NeoPackageInfo {
    var packageName: String? = null
    var isEssential: Boolean = false
    var version: String? = null
    var architecture: NeoPackageArchitecture = NeoPackageArchitecture.ALL
    var maintainer: String? = null
    var installedSizeInBytes: Long = 0L
    var fileName: String? = null
    var dependenciesString: String? = null
    var dependencies: Array<NeoPackageInfo>? = null
    var sizeInBytes: Long = 0L
    var md5: String? = null
    var sha1: String? = null
    var sha256: String? = null
    var homePage: String? = null
    var description: String? = null
}

