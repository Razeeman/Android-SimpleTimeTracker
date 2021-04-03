package com.example.util.simpletimetracker.utils

import android.util.Log
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Retry test rule used to retry test that failed.
 */
class RetryTestRule(val retryCount: Int = 5) : TestRule {

    private val tag: String = RetryTestRule::class.java.simpleName

    override fun apply(base: Statement, description: Description): Statement {
        return statement(base, description)
    }

    private fun statement(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                var caughtThrowable: Throwable? = null

                for (i in 0 until retryCount) {
                    try {
                        base.evaluate()
                        return
                    } catch (t: Throwable) {
                        caughtThrowable = t
                        Log.e(tag, "${description.displayName}: run ${i + 1} failed")
                    }
                }

                Log.e(tag, "${description.displayName}: giving up after $retryCount failures")
                throw caughtThrowable!!
            }
        }
    }
}