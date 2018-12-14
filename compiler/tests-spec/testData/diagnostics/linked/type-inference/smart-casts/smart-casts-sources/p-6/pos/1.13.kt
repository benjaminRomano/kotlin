// !LANGUAGE: +NewInference
// !DIAGNOSTICS: -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-draft
 * PLACE: type-inference, smart-casts, smart-casts-sources -> paragraph 6 -> sentence 1
 * NUMBER: 13
 * DESCRIPTION: Nullability condition, if, generic type variables
 * NOTE: lazy smartcasts
 */


// TESTCASE NUMBER: 1
fun <T> case_1(x: T) {
    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("T!! & T")!>x<!>
    }
}

// TESTCASE NUMBER: 2
fun <T> case_2(x: T?) {
    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("T!! & T?")!>x<!>
    }
}

/*
 * TESTCASE NUMBER: 18
 * NOTE: lazy smartcasts
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-28785
 */
interface A18<T> { fun test() }

fun <T: A18<in T>?> case_18(x: T) {
    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("T!! & T")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("T & T!!"), DEBUG_INFO_SMARTCAST!>x<!>.hashCode()
        <!DEBUG_INFO_EXPRESSION_TYPE("T!! & T")!>x<!><!UNSAFE_CALL!>.<!>test()
    }
}

/*
 * TESTCASE NUMBER: 22
 * NOTE: lazy smartcasts
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-28785
 */
interface A22_1<T> { fun test1() }
interface A22_2<T> { fun test2() }
interface A22_3<T> { fun test3() }

fun <T> case_22(x: T) where T: A22_1<in T>?, T: A22_2<out T>?, T: A22_3<T>? {
    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("T!! & T")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("T & T!!"), DEBUG_INFO_SMARTCAST!>x<!>.hashCode()
        <!DEBUG_INFO_EXPRESSION_TYPE("T!! & T")!>x<!><!UNSAFE_CALL!>.<!>test1()
        <!DEBUG_INFO_EXPRESSION_TYPE("T!! & T")!>x<!><!UNSAFE_CALL!>.<!>test2()
        <!DEBUG_INFO_EXPRESSION_TYPE("T & T!!"), DEBUG_INFO_SMARTCAST!>x<!>.test3()
    }
}

// TESTCASE NUMBER: 23
interface A23<T> { fun test() }

fun <T1: A23<A23<out T1>>?, T2: A23<out T2>> case_23(x: A23<T2>?, y: T1) {
    if (y != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("T1!! & T1")!>y<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("T1 & T1!!"), DEBUG_INFO_SMARTCAST!>y<!>.hashCode()
        <!DEBUG_INFO_EXPRESSION_TYPE("T1 & T1!!"), DEBUG_INFO_SMARTCAST!>y<!>.test()
    }
    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("A23<T2> & A23<T2>?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("A23<T2>? & A23<T2>"), DEBUG_INFO_SMARTCAST!>x<!>.hashCode()
        <!DEBUG_INFO_EXPRESSION_TYPE("A23<T2>? & A23<T2>"), DEBUG_INFO_SMARTCAST!>x<!>.test()
    }
}

// TESTCASE NUMBER: 36
interface A36<T> { fun test() }

fun <T> case_36(x: A36<in T>?) {
    if (x != null) {
        <!DEBUG_INFO_EXPRESSION_TYPE("A36<in T> & A36<in T>?")!>x<!>
        <!DEBUG_INFO_EXPRESSION_TYPE("A36<in T> & A36<in T>?")!>x<!>.hashCode()
        <!DEBUG_INFO_EXPRESSION_TYPE("A36<in T> & A36<in T>?")!>x<!>.test()
    }
}