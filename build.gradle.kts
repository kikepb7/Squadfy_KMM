plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.hot.reload) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.kover) apply false
}
/**
 * AC-011-09: fails when a user-visible literal ("…" with a letter) is passed to a Compose text slot in
 * presentation code. `@Preview` functions are skipped; a deliberate literal (brand, symbol) can be kept with
 * a trailing `// i18n-ignore`.
 */
tasks.register("checkHardcodedStrings") {
    group = "verification"
    description = "Fails if presentation code passes hardcoded user-visible strings to Compose"
    val sources = fileTree(rootDir) {
        include("feature/*/presentation/src/commonMain/kotlin/**/*.kt")
        include("core/presentation/src/commonMain/kotlin/**/*.kt")
        include("core/designsystem/src/commonMain/kotlin/**/*.kt")
        include("composeApp/src/commonMain/kotlin/**/*.kt")
    }
    val root = rootDir
    inputs.files(sources)
    doLast {
        val slot = Regex("""(\bText\(\s*|\b(text|title|label|placeholder|contentDescription|headerText|subtitle|message)\s*=\s*)"([^"]*)"""")
        // Interpolations ("$score", "${a.b} $c") are not copy: only the literal remainder counts
        val interpolation = Regex("""\$\{[^}]*\}?|\$\w+""")
        fun isCopy(literal: String) = interpolation.replace(literal, "").any { it.isLetter() }
        val violations = mutableListOf<String>()
        sources.files.sortedBy { it.path }.forEach { file ->
            var inPreview = false
            file.readLines().forEachIndexed { index, line ->
                if (line.trimStart().startsWith("@Preview")) inPreview = true
                if (inPreview) {
                    if (line.startsWith("}")) inPreview = false
                    return@forEachIndexed
                }
                if (line.contains("// i18n-ignore")) return@forEachIndexed
                if (slot.findAll(line).any { isCopy(it.groupValues[3]) }) {
                    violations += "${file.relativeTo(root)}:${index + 1}: ${line.trim()}"
                }
            }
        }
        if (violations.isNotEmpty()) {
            throw GradleException(
                "Hardcoded user-visible strings (move them to composeResources):\n" + violations.joinToString("\n")
            )
        }
    }
}
