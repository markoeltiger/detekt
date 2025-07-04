package io.gitlab.arturbosch.detekt.rules.style

import io.gitlab.arturbosch.detekt.api.ActiveByDefault
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Finding
import io.gitlab.arturbosch.detekt.api.RequiresAnalysisApi
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.rules.isCallingWithNonNullCheckArgument
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtCallExpression

/**
 * Turn on this rule to flag `check` calls for not-null check that can be replaced with a `checkNotNull` call.
 *
 * <noncompliant>
 * check(x != null)
 * </noncompliant>
 *
 * <compliant>
 * checkNotNull(x)
 * </compliant>
 */
@ActiveByDefault(since = "1.21.0")
class UseCheckNotNull(config: Config) :
    Rule(
        config,
        "Use checkNotNull() instead of check() for checking not-null."
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.isCallingWithNonNullCheckArgument(requireFunctionCallableId)) {
            report(Finding(Entity.from(expression), description))
        }
    }

    companion object {
        private val requireFunctionCallableId =
            CallableId(StandardClassIds.BASE_KOTLIN_PACKAGE, Name.identifier("check"))
    }
}
