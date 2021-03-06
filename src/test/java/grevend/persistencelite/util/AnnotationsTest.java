/*
 * MIT License
 *
 * Copyright (c) 2020 David Greven
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package grevend.persistencelite.util;

import grevend.persistencelite.entity.Ignore;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Test;

class AnnotationsTest {

    @Test
    void testRetentionAndTargetArePresentAndCorrect() {
        try {
            TestUtil.assertAnnotationRetentionAndTarget(Ignore.class, ElementType.FIELD);
        } catch (AssertionError assertionError) {
            Assertions.fail(assertionError.getMessage());
        }
    }

    @Test
    void testRetentionAndTargetAreNotPresent() {
        try {
            TestUtil.assertAnnotationRetentionAndTarget(NoRetentionOrTargetTest.class);
            Assertions.fail("No assertion error found. Retention and Target already set.");
        } catch (AssertionError assertionError) {
            Assertions.assertThat(assertionError.getMessage())
                .isEqualToIgnoringCase("No annotation retention policy found for " +
                    NoRetentionOrTargetTest.class.getCanonicalName() + ".");
        }
    }

    @Test
    void testRetentionIsNotRuntime() {
        try {
            TestUtil.assertAnnotationRetentionAndTarget(NotRuntimeTest.class);
            Assertions.fail("No assertion error found. Retentions is already RUNTIME.");
        } catch (AssertionError assertionError) {
            Assertions.assertThat(assertionError.getMessage())
                .contains("annotation retention policy must be RUNTIME.");
        }
    }

    @Test
    void testTargetIsNotPresent() {
        try {
            TestUtil.assertAnnotationRetentionAndTarget(NoTargetTest.class);
        } catch (AssertionError assertionError) {
            Assertions.assertThat(assertionError.getMessage())
                .startsWith("No annotation target found for");
        }
    }

    @Test
    void testTargetIsNotField() {
        try {
            TestUtil.assertAnnotationRetentionAndTarget(MethodTargetTest.class, ElementType.FIELD);
            Assertions.fail("No assertion error found. Target is already FIELD.");
        } catch (AssertionError assertionError) {
            Assertions.assertThat(assertionError.getMessage())
                .contains("annotation target must contain FIELD.");
        }
    }

    @TestOnly
    @interface NoRetentionOrTargetTest {

    }

    @TestOnly
    @Retention(RetentionPolicy.SOURCE)
    @interface NotRuntimeTest {

    }

    @TestOnly
    @Retention(RetentionPolicy.RUNTIME)
    @interface NoTargetTest {

    }

    @TestOnly
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface MethodTargetTest {

    }

}
