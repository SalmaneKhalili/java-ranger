package gov.nasa.jpf.symbc.numeric;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestSpecialValue {

    // RealNaN tests

    @Test
    public void testNaNInstances() {
        assertNotNull(RealNaN.FLOAT_NAN);
        assertNotNull(RealNaN.DOUBLE_NAN);
        assertNotSame(RealNaN.FLOAT_NAN, RealNaN.DOUBLE_NAN);
    }

    @Test
    public void testNaNEquality() {
        RealNaN floatNaN1 = RealNaN.FLOAT_NAN;
        RealNaN floatNaN2 = RealNaN.FLOAT_NAN;
        RealNaN doubleNaN = RealNaN.DOUBLE_NAN;

        assertEquals(floatNaN1, floatNaN2);
        assertNotEquals(floatNaN1, doubleNaN);
        assertEquals(floatNaN1.hashCode(), floatNaN2.hashCode());
        assertNotEquals(floatNaN1.hashCode(), doubleNaN.hashCode());
    }

    @Test
    public void testNaNToString() {
        assertEquals("FloatNaN", RealNaN.FLOAT_NAN.toString());
        assertEquals("DoubleNaN", RealNaN.DOUBLE_NAN.toString());
    }

    @Test
    public void testNaNUnderlyingDouble() {
        assertTrue(Double.isNaN(RealNaN.FLOAT_NAN.value));
        assertTrue(Double.isNaN(RealNaN.DOUBLE_NAN.value));
    }

    //  RealInfinity tests

    @Test
    public void testInfinityCreation() {
        RealInfinity posFloatInf = RealInfinity.get(true, FpSort.FLOAT);
        RealInfinity negFloatInf = RealInfinity.get(false, FpSort.FLOAT);
        RealInfinity posDoubleInf = RealInfinity.get(true, FpSort.DOUBLE);
        RealInfinity negDoubleInf = RealInfinity.get(false, FpSort.DOUBLE);

        assertNotNull(posFloatInf);
        assertNotNull(negFloatInf);
        assertNotNull(posDoubleInf);
        assertNotNull(negDoubleInf);
    }

    @Test
    public void testInfinityEquality() {
        RealInfinity inf1 = RealInfinity.get(true, FpSort.FLOAT);
        RealInfinity inf2 = RealInfinity.get(true, FpSort.FLOAT);
        RealInfinity inf3 = RealInfinity.get(false, FpSort.FLOAT);
        RealInfinity inf4 = RealInfinity.get(true, FpSort.DOUBLE);

        assertEquals(inf1, inf2);
        assertEquals(inf1.hashCode(), inf2.hashCode());
        assertNotEquals(inf1, inf3);
        assertNotEquals(inf1, inf4);
    }

    @Test
    public void testInfinitySign() {
        assertTrue(RealInfinity.get(true, FpSort.FLOAT).isPositive());
        assertFalse(RealInfinity.get(false, FpSort.FLOAT).isPositive());
    }

    @Test
    public void testInfinityToString() {
        assertEquals("Float+Inf", RealInfinity.get(true, FpSort.FLOAT).toString());
        assertEquals("Float-Inf", RealInfinity.get(false, FpSort.FLOAT).toString());
        assertEquals("Double+Inf", RealInfinity.get(true, FpSort.DOUBLE).toString());
        assertEquals("Double-Inf", RealInfinity.get(false, FpSort.DOUBLE).toString());
    }

    @Test
    public void testInfinityUnderlyingDouble() {
        RealInfinity posFloatInf = RealInfinity.get(true, FpSort.FLOAT);
        RealInfinity negFloatInf = RealInfinity.get(false, FpSort.FLOAT);
        RealInfinity posDoubleInf = RealInfinity.get(true, FpSort.DOUBLE);
        RealInfinity negDoubleInf = RealInfinity.get(false, FpSort.DOUBLE);

        assertEquals(Double.POSITIVE_INFINITY, posFloatInf.value, 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, negFloatInf.value, 0.0);
        assertEquals(Double.POSITIVE_INFINITY, posDoubleInf.value, 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, negDoubleInf.value, 0.0);
    }

    // RealZero tests

    @Test
    public void testZeroCreation() {
        RealZero posFloatZero = RealZero.get(true, FpSort.FLOAT);
        RealZero negFloatZero = RealZero.get(false, FpSort.FLOAT);
        RealZero posDoubleZero = RealZero.get(true, FpSort.DOUBLE);
        RealZero negDoubleZero = RealZero.get(false, FpSort.DOUBLE);

        assertNotNull(posFloatZero);
        assertNotNull(negFloatZero);
        assertNotNull(posDoubleZero);
        assertNotNull(negDoubleZero);
    }

    @Test
    public void testZeroEquality() {
        RealZero zero1 = RealZero.get(true, FpSort.FLOAT);
        RealZero zero2 = RealZero.get(true, FpSort.FLOAT);
        RealZero zero3 = RealZero.get(false, FpSort.FLOAT);
        RealZero zero4 = RealZero.get(true, FpSort.DOUBLE);

        assertEquals(zero1, zero2);
        assertEquals(zero1.hashCode(), zero2.hashCode());
        assertNotEquals(zero1, zero3);
        assertNotEquals(zero1, zero4);
    }

    @Test
    public void testZeroSign() {
        assertTrue(RealZero.get(true, FpSort.FLOAT).isPositive());
        assertFalse(RealZero.get(false, FpSort.FLOAT).isPositive());
    }

    @Test
    public void testZeroToString() {
        assertEquals("Float+Zero", RealZero.get(true, FpSort.FLOAT).toString());
        assertEquals("Float-Zero", RealZero.get(false, FpSort.FLOAT).toString());
        assertEquals("Double+Zero", RealZero.get(true, FpSort.DOUBLE).toString());
        assertEquals("Double-Zero", RealZero.get(false, FpSort.DOUBLE).toString());
    }

    @Test
    public void testZeroUnderlyingDouble() {
        RealZero posFloatZero = RealZero.get(true, FpSort.FLOAT);
        RealZero negFloatZero = RealZero.get(false, FpSort.FLOAT);
        RealZero posDoubleZero = RealZero.get(true, FpSort.DOUBLE);
        RealZero negDoubleZero = RealZero.get(false, FpSort.DOUBLE);

        assertEquals(0.0, posFloatZero.value, 0.0);
        assertEquals(-0.0, negFloatZero.value, 0.0);
        assertEquals(0.0, posDoubleZero.value, 0.0);
        assertEquals(-0.0, negDoubleZero.value, 0.0);

        // Verify bit pattern for negative zero
        assertEquals(0x8000000000000000L, Double.doubleToLongBits(negFloatZero.value));
    }

    // Factory tests

    @Test
    public void testFactoryNaN() {
        assertSame(RealNaN.FLOAT_NAN, SpecialValueFactory.createNaN(false));
        assertSame(RealNaN.DOUBLE_NAN, SpecialValueFactory.createNaN(true));
    }

    @Test
    public void testFactoryInfinity() {
        RealInfinity inf1 = SpecialValueFactory.createInfinity(true, false);
        RealInfinity inf2 = SpecialValueFactory.createInfinity(false, true);
        assertTrue(inf1.isPositive());
        assertEquals(FpSort.FLOAT, inf1.getSort());
        assertFalse(inf2.isPositive());
        assertEquals(FpSort.DOUBLE, inf2.getSort());
    }

    @Test
    public void testFactoryZero() {
        RealZero zero1 = SpecialValueFactory.createZero(true, false);
        RealZero zero2 = SpecialValueFactory.createZero(false, true);
        assertTrue(zero1.isPositive());
        assertEquals(FpSort.FLOAT, zero1.getSort());
        assertFalse(zero2.isPositive());
        assertEquals(FpSort.DOUBLE, zero2.getSort());
    }

    // Visitor tests

    // Simple visitor that counts visits to RealConstant
    private static class CountingVisitor extends ConstraintExpressionVisitor {
        int count = 0;

        @Override
        public void postVisit(RealConstant realConstant) {
            count++;
        }
    }

    @Test
    public void testAcceptNaN() {
        CountingVisitor visitor = new CountingVisitor();
        RealNaN.FLOAT_NAN.accept(visitor);
        assertEquals(1, visitor.count);
    }

    @Test
    public void testAcceptInfinity() {
        CountingVisitor visitor = new CountingVisitor();
        RealInfinity.get(true, FpSort.FLOAT).accept(visitor);
        assertEquals(1, visitor.count);
    }

    @Test
    public void testAcceptZero() {
        CountingVisitor visitor = new CountingVisitor();
        RealZero.get(true, FpSort.FLOAT).accept(visitor);
        assertEquals(1, visitor.count);
    }
}