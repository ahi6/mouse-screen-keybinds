package dev.pages.ahi.screen_secondary.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.pages.ahi.screen_secondary.ScreenSecondary;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import static dev.pages.ahi.screen_secondary.ScreenSecondary.LOGGER;

public class ScreenSecondaryClient implements ClientModInitializer {
	public static KeyMapping.Category CATEGORY = KeyMapping.Category.register(
			Identifier.fromNamespaceAndPath(ScreenSecondary.MOD_ID, "custom_category")
	);

	public static KeyMapping interact2 = KeyMappingHelper.registerKeyMapping(
			new KeyMapping(
					"key.screen-secondary.interact2", // The translation key for the key mapping.
					InputConstants.Type.KEYSYM, // The type of the keybinding; KEYSYM for keyboard, MOUSE for mouse.
					GLFW.GLFW_KEY_J, // The GLFW keycode of the key.
					ScreenSecondaryClient.CATEGORY // The category of the mapping.
			));

	private boolean wasDuplicateKeyDown = false;

	@Override
	public void onInitializeClient() {
		/*ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (this.wasDuplicateKeyDown) {
				LOGGER.info("if");
				Screen screen = client.gui.screen();

				if (screen == null) {
					return;
				}

				this.wasDuplicateKeyDown = false;

				double mouse_X = client.mouseHandler.xpos() * (double) client.getWindow().getGuiScaledWidth() / client.getWindow().getWidth();
				double mouse_Y = client.mouseHandler.ypos() * (double) client.getWindow().getGuiScaledHeight() / client.getWindow().getHeight();

				MouseButtonInfo mbi = new MouseButtonInfo(1, 0);
				MouseButtonEvent mbe = new MouseButtonEvent(mouse_X, mouse_Y, mbi);
				screen.mouseClicked(mbe, false);

				//client.options.keyUse.setDown(false);



			} else if (this.interact2.isDown()) {
				LOGGER.info("elif");
				Screen screen = client.gui.screen();

				if (screen == null) {
					return;
				}

				this.wasDuplicateKeyDown = false;

				double mouse_X = client.mouseHandler.xpos() * (double) client.getWindow().getGuiScaledWidth() / client.getWindow().getWidth();
				double mouse_Y = client.mouseHandler.ypos() * (double) client.getWindow().getGuiScaledHeight() / client.getWindow().getHeight();

				MouseButtonInfo mbi = new MouseButtonInfo(1, 0);
				MouseButtonEvent mbe = new MouseButtonEvent(mouse_X, mouse_Y, mbi);
				screen.mouseReleased(mbe);

				//client.options.keyUse.setDown(true);
				this.wasDuplicateKeyDown = true;
			}
		});*/

		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {

			// 2. Register the beforeKeyPress event to this specific screen instance
			ScreenKeyboardEvents.beforeKeyPress(screen).register((currentScreen, event) -> {

				if (ScreenSecondaryClient.interact2.matches(event)) {
					LOGGER.info("Correct key.");
					LOGGER.info("Event: " + event);

					double mouse_X = client.mouseHandler.xpos() * (double) scaledWidth / client.getWindow().getWidth();
					double mouse_Y = client.mouseHandler.ypos() * (double) scaledHeight / client.getWindow().getHeight();

					LOGGER.info("Clicking.");
					// TODO: set mousedown
					MouseButtonInfo mbi = new MouseButtonInfo(0, 0);
					MouseButtonEvent mbe = new MouseButtonEvent(mouse_X, mouse_Y, mbi);
					screen.mouseClicked(mbe, false);
				}
			});

			ScreenKeyboardEvents.beforeKeyRelease(screen).register((currentScreen, event) -> {

				if (ScreenSecondaryClient.interact2.matches(event)) {
					LOGGER.info("Correct key.");
					LOGGER.info("Event: " + event);

					double mouse_X = client.mouseHandler.xpos() * (double) scaledWidth / client.getWindow().getWidth();
					double mouse_Y = client.mouseHandler.ypos() * (double) scaledHeight / client.getWindow().getHeight();

					LOGGER.info("Releasing.");
					MouseButtonInfo mbi = new MouseButtonInfo(0, 0);
					MouseButtonEvent mbe = new MouseButtonEvent(mouse_X, mouse_Y, mbi);
					screen.mouseReleased(mbe);
				}
			});
		});
	}
}