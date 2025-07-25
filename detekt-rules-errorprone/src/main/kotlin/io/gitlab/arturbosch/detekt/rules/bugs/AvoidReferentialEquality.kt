package io.gitlab.arturbosch.detekt.rules.bugs

import io.gitlab.arturbosch.detekt.api.ActiveByDefault
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Configuration
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Finding
import io.gitlab.arturbosch.detekt.api.RequiresAnalysisApi
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.config
import io.gitlab.arturbosch.detekt.api.simplePatternToRegex
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.lexer.KtTokens.EQEQEQ
import org.jetbrains.kotlin.lexer.KtTokens.EXCLEQEQEQ
import org.jetbrains.kotlin.psi.KtBinaryExpression

/**
 * Kotlin supports two types of equality: structural equality and referential equality. While there are
 * use cases for both, checking for referential equality for some types (such as `String` or `List`) is
 * likely not intentional and may cause unexpected results.
 *
 * <noncompliant>
 *     val areEqual = "aString" === otherString
 *     val areNotEqual = "aString" !== otherString
 * </noncompliant>
 *
 * <compliant>
 *     val areEqual = "aString" == otherString
 *     val areNotEqual = "aString" != otherString
 * </compliant>
 */
@ActiveByDefault(since = "1.21.0")
class AvoidReferentialEquality(config: Config) :
    Rule(
        config,
        "Avoid using referential equality and prefer to use referential equality checks instead."
    ),
    RequiresAnalysisApi {

    @Configuration(
        "Specifies those types for which referential equality checks are considered a rule violation. " +
            "The types are defined by a list of simple glob patterns (supporting `*` and `?` wildcards) " +
            "that match the fully qualified type name."
    )
    private val forbiddenTypePatterns: List<Regex> by config(
        listOf(
            "kotlin.String"
        )
    ) { it.map(String::simplePatternToRegex) }

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        checkBinaryExpression(expression)
    }

    private fun checkBinaryExpression(expression: KtBinaryExpression) {
        if (expression.operationToken != EQEQEQ && expression.operationToken != EXCLEQEQEQ) return

        val fullyQualifiedType = expression.left?.let {
            analyze(it) {
                it.expressionType?.symbol?.classId?.asFqNameString()
            }
        } ?: return

        if (forbiddenTypePatterns.any { it.matches(fullyQualifiedType) }) {
            report(
                Finding(
                    Entity.from(expression),
                    "Checking referential equality may lead to unwanted results."
                )
            )
        }
    }
}
