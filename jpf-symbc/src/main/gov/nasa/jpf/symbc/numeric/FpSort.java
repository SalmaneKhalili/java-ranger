package gov.nasa.jpf.symbc.numeric;

/**
 * Distinguishes between single and double precision floating‑point sorts.
 */
public enum FpSort {
    FLOAT,   // 32‑bit, 8 exponent bits, 24 significand bits
    DOUBLE;  // 64‑bit, 11 exponent bits, 53 significand bits
}