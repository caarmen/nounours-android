package ca.rmen.nounours.android.wear;

import android.graphics.Point;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import ca.rmen.nounours.BuildConfig;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class NounoursWatchFaceRendererTest {

    @Test
    public void testDialNumberPositionInCircle() {
        testDialNumberPositionInCircleImpl(12, 480, 10, 6, 240, 5);
        testDialNumberPositionInCircleImpl(3, 480, 10, 6, 475, 240);
        testDialNumberPositionInCircleImpl(6, 480, 10, 6, 240, 475);
        testDialNumberPositionInCircleImpl(9, 480, 10, 6, 5, 240);
    }

    @Test
    public void testDialNumberPositionInRect() {
        testDialNumberPositionInRectImpl(12, 640, 480, 10, 6, 320, 3);
        testDialNumberPositionInRectImpl(3, 640, 480, 10, 6, 635, 240);
        testDialNumberPositionInRectImpl(6, 640, 480, 10, 6, 320, 477);
        testDialNumberPositionInRectImpl(9, 640, 480, 10, 6, 5, 240);
    }

    @Test
    public void testOuterRimPointInCircle() {
        testOuterRimPointInCircleImpl("12", 480, 90, 240, 0);
        testOuterRimPointInCircleImpl("1", 480, 60, 360, 32);
        testOuterRimPointInCircleImpl("2", 480, 30, 448, 120);
        testOuterRimPointInCircleImpl("3", 480, 0, 480, 240);
        testOuterRimPointInCircleImpl("4", 480, -30, 448, 360);
        testOuterRimPointInCircleImpl("5", 480, -60, 360, 448);
        testOuterRimPointInCircleImpl("6", 480, -90, 240, 480);
        testOuterRimPointInCircleImpl("7", 480, -120, 120, 448);
        testOuterRimPointInCircleImpl("8", 480, -150, 32, 360);
        testOuterRimPointInCircleImpl("9", 480, -180, 0, 240);
        testOuterRimPointInCircleImpl("10", 480, -210, 32, 120);
        testOuterRimPointInCircleImpl("11", 480, -240, 120, 32);
    }

    @Test
    public void testOuterRimPointInRect() {

        testOuterRimPointInRectImpl("12", 640, 480, 90, 320, 0);
        testOuterRimPointInRectImpl("1", 640, 480, 60, 459, 0);
        testOuterRimPointInRectImpl("2", 640, 480, 30, 640, 55);
        testOuterRimPointInRectImpl("3", 640, 480, 0, 640, 240);
        testOuterRimPointInRectImpl("4", 640, 480, -30, 640, 425);
        testOuterRimPointInRectImpl("5", 640, 480, -60, 459, 480);
        testOuterRimPointInRectImpl("6", 640, 480, -90, 320, 480);
        testOuterRimPointInRectImpl("7", 640, 480, -120, 181, 480);
        testOuterRimPointInRectImpl("8", 640, 480, -150, 0, 425);
        testOuterRimPointInRectImpl("9", 640, 480, -180, 0, 240);
        testOuterRimPointInRectImpl("10", 640, 480, -210, 0, 55);
        testOuterRimPointInRectImpl("11", 640, 480, -240, 181, 0);
    }

    private void testOuterRimPointInCircleImpl(String label, int screenWidth, double degrees, int expectedX, int expectedY) {
        Point actualPoint = NounoursWatchFaceRenderer.getOuterRimPointInCircle(screenWidth, degrees);
        Assert.assertEquals("Error calculating the x coordinate for dial number " + label, expectedX, actualPoint.x);
        Assert.assertEquals("Error calculating the y coordinate for dial number " + label, expectedY, actualPoint.y);
    }

    private void testOuterRimPointInRectImpl(String label, int screenWidth, int screenHeight, double degrees, int expectedX, int expectedY) {
        Point actualPoint = NounoursWatchFaceRenderer.getOuterRimPointInRect(screenWidth, screenHeight, degrees);
        Assert.assertEquals("Error calculating the x coordinate for dial number " + label, expectedX, actualPoint.x);
        Assert.assertEquals("Error calculating the y coordinate for dial number " + label, expectedY, actualPoint.y);
    }

    private void testDialNumberPositionInRectImpl(int dialNumber, int screenWidth, int screenHeight, int dialNumberWidth, int dialNumberHeight, int expectedX, int expectedY) {
        Point actualPoint = NounoursWatchFaceRenderer.getDialNumberPositionInRect(dialNumber, screenWidth, screenHeight, dialNumberWidth, dialNumberHeight);
        Assert.assertEquals("Error calculating the x coordinate for dial number " + dialNumber, expectedX, actualPoint.x);
        Assert.assertEquals("Error calculating the y coordinate for dial number " + dialNumber, expectedY, actualPoint.y);
    }

    private void testDialNumberPositionInCircleImpl(int dialNumber, int screenWidth, int dialNumberWidth, int dialNumberHeight, int expectedX, int expectedY) {
        Point actualPoint = NounoursWatchFaceRenderer.getDialNumberPositionInCircle(dialNumber, screenWidth, dialNumberWidth, dialNumberHeight);
        Assert.assertEquals("Error calculating the x coordinate for dial number " + dialNumber, expectedX, actualPoint.x);
        Assert.assertEquals("Error calculating the y coordinate for dial number " + dialNumber, expectedY, actualPoint.y);
    }
}