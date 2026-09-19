package dev.pages.ahi.mouse_screen_keybinds.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.pages.ahi.mouse_screen_keybinds.client.mixin.KeyMappingAccessor;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MouseScreenKeybindsClient implements ClientModInitializer {
    private static final String MOD_ID = "mouse-screen-keybinds";

    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(MouseScreenKeybindsClient.MOD_ID, "screen_mouse_buttons")
    );

    private static final KeyMapping KM_MOUSE_PRIMARY = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.mouse-screen-keybinds.screen_mouse0",
                    InputConstants.Type.KEYBOARD,
                    InputConstants.UNKNOWN.getValue(),
                    MouseScreenKeybindsClient.CATEGORY
            ));

    private static final KeyMapping KM_MOUSE_SECONDARY = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.mouse-screen-keybinds.screen_mouse1",
                    InputConstants.Type.KEYBOARD,
                    InputConstants.UNKNOWN.getValue(),
                    MouseScreenKeybindsClient.CATEGORY
            ));

    private static final KeyMapping KM_MOUSE_MIDDLE = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.mouse-screen-keybinds.screen_mouse2",
                    InputConstants.Type.KEYBOARD,
                    InputConstants.UNKNOWN.getValue(),
                    MouseScreenKeybindsClient.CATEGORY
            ));

    private static final Map<KeyMapping, Integer> MOUSE_KEYMAPS = Map.of(
            KM_MOUSE_PRIMARY, InputConstants.MOUSE_BUTTON_LEFT,
            KM_MOUSE_SECONDARY, InputConstants.MOUSE_BUTTON_RIGHT,
            KM_MOUSE_MIDDLE, InputConstants.MOUSE_BUTTON_MIDDLE
    );


    Long lastClickMillis = null;
    Integer lastButtonIdx = null;

    HashMap<Integer, Boolean> isIdxClicking = new HashMap<>();
    double lastMouseX = 0;
    double lastMouseY = 0;

    private void handleKeyEvent(Minecraft client, int scaledWidth, int scaledHeight, Screen screen, KeyEvent event, boolean released) {

        for (KeyMapping keymap : MOUSE_KEYMAPS.keySet()) {
            if (keymap.matches(event)) {
                int buttonIdx = MOUSE_KEYMAPS.get(keymap);
                LOGGER.debug("Event: {} ({})", event, released ? "Released" : "Pressed");

                MouseButtonEvent mbe = this.mouseButtonEventHelper(buttonIdx, client, scaledWidth, scaledHeight);

                if (released) {
                    screen.mouseReleased(mbe);

                    this.isIdxClicking.put(buttonIdx, false);
                } else if (!this.isIdxClicking.getOrDefault(buttonIdx, false)){
                    long currentTime = Util.getMillis();
                    boolean doubleClick = this.lastButtonIdx != null
                            && currentTime - this.lastClickMillis < 250L
                            && this.lastButtonIdx == buttonIdx;

                    screen.mouseClicked(mbe, doubleClick);

                    this.isIdxClicking.put(buttonIdx, true);
                    this.lastButtonIdx = buttonIdx;
                    this.lastClickMillis = currentTime;

                    this.lastMouseX = mbe.x();
                    this.lastMouseY = mbe.y();
                }
            }
        }
    }

    private void handleTickEvent(Minecraft client, int scaledWidth, int scaledHeight, Screen screen) {
        MOUSE_KEYMAPS.forEach((keymap, buttonIdx) -> {
            if (this.isIdxClicking.getOrDefault(buttonIdx, false)) {
                // manually query if it has been released, since Minecraft's SDL3 backend does not send release events
                // via Fabric API when loading into a world, for example
                final int k = ((KeyMappingAccessor) keymap).mouse_screen_keybinds$getKey().getValue();
                if (!InputConstants.isKeyDown(k)) {
                    LOGGER.debug("Clearing held button {}", buttonIdx);
                    this.isIdxClicking.put(buttonIdx, false);
                    return;
                }

                // dragging
                MouseButtonEvent mbe = this.mouseButtonEventHelper(buttonIdx, client, scaledWidth, scaledHeight);

                double curMouseX = mbe.x();
                double curMouseY = mbe.y();

                double dx = curMouseX - lastMouseX;
                double dy = curMouseY - lastMouseY;

                screen.mouseDragged(mbe, dx, dy);

                LOGGER.debug("Dragged: idx {}, dx:dy {}:{}", buttonIdx, dx, dy);

                this.lastMouseX = curMouseX;
                this.lastMouseY = curMouseY;
            }
        });
    }

    private MouseButtonEvent mouseButtonEventHelper(int buttonIdx, Minecraft client, int scaledWidth, int scaledHeight) {
        double mouse_X = client.mouseHandler.xpos() * (double) scaledWidth / client.getWindow().getWidth();
        double mouse_Y = client.mouseHandler.ypos() * (double) scaledHeight / client.getWindow().getHeight();

        MouseButtonInfo mbi = new MouseButtonInfo(buttonIdx, client.hasShiftDown() ? 1 : 0);
        return new MouseButtonEvent(mouse_X, mouse_Y, mbi);
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