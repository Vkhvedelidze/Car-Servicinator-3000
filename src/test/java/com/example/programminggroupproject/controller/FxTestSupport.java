package com.example.programminggroupproject.controller;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;

/** Runs real JavaFX controls without opening a window or contacting a backend. */
final class FxTestSupport {
    private FxTestSupport() {
    }

    static void startToolkit() throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);
        Runnable ready = () -> {
            Platform.setImplicitExit(false);
            started.countDown();
        };
        try {
            Platform.startup(ready);
        } catch (IllegalStateException alreadyStarted) {
            Platform.runLater(ready);
        } catch (Throwable unavailable) {
            throw new AssertionError("JavaFX could not start. These UI tests require a working "
                    + "JavaFX graphics toolkit; they must not be silently skipped.", unavailable);
        }
        if (!started.await(10, TimeUnit.SECONDS)) {
            throw new AssertionError("JavaFX toolkit did not become ready within 10 seconds");
        }
    }

    static <T> T call(Callable<T> action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            return action.call();
        }
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        try {
            return task.get(10, TimeUnit.SECONDS);
        } catch (ExecutionException failure) {
            if (failure.getCause() instanceof Exception exception) {
                throw exception;
            }
            if (failure.getCause() instanceof Error error) {
                throw error;
            }
            throw new AssertionError(failure.getCause());
        }
    }

    static void run(Runnable action) throws Exception {
        call(() -> {
            action.run();
            return null;
        });
    }

    static void flush() throws Exception {
        run(() -> { });
    }

    /** Small RGBA PNG writer; avoids adding java.desktop or javafx.swing to the app module. */
    static void writeSnapshot(Node node, Path path) throws IOException {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Snapshots must run on the JavaFX application thread");
        }
        WritableImage image = node.snapshot(null, null);
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelReader pixels = image.getPixelReader();
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (DeflaterOutputStream zlib = new DeflaterOutputStream(compressed)) {
            for (int y = 0; y < height; y++) {
                zlib.write(0);
                for (int x = 0; x < width; x++) {
                    int argb = pixels.getArgb(x, y);
                    zlib.write((argb >>> 16) & 0xff);
                    zlib.write((argb >>> 8) & 0xff);
                    zlib.write(argb & 0xff);
                    zlib.write((argb >>> 24) & 0xff);
                }
            }
        }
        ByteArrayOutputStream headerBytes = new ByteArrayOutputStream();
        try (DataOutputStream header = new DataOutputStream(headerBytes)) {
            header.writeInt(width);
            header.writeInt(height);
            header.write(new byte[]{8, 6, 0, 0, 0});
        }
        Files.createDirectories(path.toAbsolutePath().getParent());
        try (DataOutputStream png = new DataOutputStream(Files.newOutputStream(path))) {
            png.write(new byte[]{(byte) 137, 80, 78, 71, 13, 10, 26, 10});
            writeChunk(png, "IHDR", headerBytes.toByteArray());
            writeChunk(png, "IDAT", compressed.toByteArray());
            writeChunk(png, "IEND", new byte[0]);
        }
    }

    private static void writeChunk(DataOutputStream output, String type, byte[] data) throws IOException {
        byte[] typeBytes = type.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        CRC32 checksum = new CRC32();
        checksum.update(typeBytes);
        checksum.update(data);
        output.writeInt(data.length);
        output.write(typeBytes);
        output.write(data);
        output.writeInt((int) checksum.getValue());
    }
}
