package dev.pages.ahi.mouse_screen_keybinds.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MouseScreenKeybindsClient implements ClientModInitializer {
    private static final String MOD_ID = "mouse-screen-keybinds";

    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(MouseScreenKeybindsClient.MOD_ID, "screen_mouse_buttons")
    );

    private static final KeyMapping KM_MOUSE_0 = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.screen-secondary.screen_mouse0",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_Z,
                    MouseScreenKeybindsClient.CATEGORY
            ));

    private static final KeyMapping KM_MOUSE_1 = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.screen-secondary.screen_mouse1",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_X,
                    MouseScreenKeybindsClient.CATEGORY
            ));

    private static final KeyMapping KM_MOUSE_2 = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.screen-secondary.screen_mouse2",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_V,
                    MouseScreenKeybindsClient.CATEGORY
            ));

    private static final KeyMapping[] MOUSE_KEYMAPS = {KM_MOUSE_0, KM_MOUSE_1, KM_MOUSE_2};

    Long lastClickMillis = null;
    Integer lastButtonIdx = null;
    boolean[] isIdxClicking = {false, false, false};
    double lastMouseX = 0;
    double lastMouseY = 0;

    private void handleKeyEvent(Minecraft client, int scaledWidth, int scaledHeight, Screen screen, KeyEvent event, boolean released) {
        for (int buttonIdx = 0; buttonIdx < MOUSE_KEYMAPS.length; buttonIdx++) {
            if (MOUSE_KEYMAPS[buttonIdx].matches(event)) {
                LOGGER.debug("Event: {} ({})", event, released ? "Released" : "Pressed");

                MouseButtonEvent mbe = this.mouseButtonEventHelper(buttonIdx, client, scaledWidth, scaledHeight);

                if (released) {
                    screen.mouseReleased(mbe);

                    this.isIdxClicking[buttonIdx] = false;
                } else if (!this.isIdxClicking[buttonIdx]){
                    long currentTime = Util.getMillis();
                    boolean doubleClick = this.lastButtonIdx != null
                            && currentTime - this.lastClickMillis < 250L
                            && this.lastButtonIdx == buttonIdx;

                    screen.mouseClicked(mbe, doubleClick);

                    this.isIdxClicking[buttonIdx] = true;
                    this.lastButtonIdx = buttonIdx;
                    this.lastClickMillis = currentTime;

                    this.lastMouseX = client.mouseHandler.xpos() * (double) scaledWidth / client.getWindow().getWidth();
                    this.lastMouseY = client.mouseHandler.ypos() * (double) scaledHeight / client.getWindow().getHeight();
                }

            }
        }
    }

    private void handleTickEvent(Minecraft client, int scaledWidth, int scaledHeight, Screen screen) {
        for (int buttonIdx = 0; buttonIdx < MOUSE_KEYMAPS.length; buttonIdx++) {
            if (this.isIdxClicking[buttonIdx]) {
                // dragging
                MouseButtonEvent mbe = this.mouseButtonEventHelper(buttonIdx, client, scaledWidth, scaledHeight);

                double curMouseX = client.mouseHandler.xpos() * (double) scaledWidth / client.getWindow().getWidth();
                double curMouseY = client.mouseHandler.ypos() * (double) scaledHeight / client.getWindow().getHeight();

                double dx = curMouseX - lastMouseX;
                double dy = curMouseY - lastMouseY;

                screen.mouseDragged(mbe, dx, dy);

                LOGGER.debug("Dragged: idx {}, dx:dy {}:{}", buttonIdx, dx, dy);

                this.lastMouseX = curMouseX;
                this.lastMouseY = curMouseY;
            }
        }

    }

    private MouseButtonEvent mouseButtonEventHelper(int buttonIdx, Minecraft client, int scaledWidth, int scaledHeight) {
        double mouse_X = client.mouseHandler.xpos() * (double) scaledWidth / client.getWindow().getWidth();
        double mouse_Y = client.mouseHandler.ypos() * (double) scaledHeight / client.getWindow().getHeight();

        MouseButtonInfo mbi = new MouseButtonInfo(buttonIdx, client.hasShiftDown() ? 1 : 0);
        MouseButtonEvent mbe = new MouseButtonEvent(mouse_X, mouse_Y, mbi);

        return mbe;
    }

    @Override
    public void onInitializeClient() {
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenKeyboardEvents.beforeKeyPress(screen).register((currentScreen, event) -> this.handleKeyEvent(client, scaledWidth, scaledHeight, currentScreen, event, false));
            ScreenKeyboardEvents.beforeKeyRelease(screen).register((currentScreen, event) -> this.handleKeyEvent(client, scaledWidth, scaledHeight, currentScreen, event, true));

            ScreenEvents.beforeTick(screen).register((currentScreen) -> this.handleTickEvent(client, scaledWidth, scaledHeight, currentScreen));
        });
    }
}