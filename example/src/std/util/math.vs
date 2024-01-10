package math

/**
 * Retrieve the largest of two int values.
 * 
 * @param a the first value to test
 * @param b the second value to test
 * @return the largest value of `a` and `b`
 */
int max(int a, int b) {
    if (a > b)
        return a
    return b
}

/**
 * Retrieve the smallest of two int values.
 * 
 * @param a the first value to test
 * @param b the second value to test
 * @return the smallest value of `a` and `b`
 */
int min(int a, int b) {
    if (a < b)
        return a
    return b
}

/**
 * Retrieve the absolute value of an int value. If the argument is not negative,
 * the argument is returned. Otherwise, the negated version of the argument is returned.
 * 
 * @param a the argument whose absolute value is to be retrieved
 * @return the absolute value of the specified argument
 */
int abs(int a) {
    if (a < 0)
        return -a
    return a
}
