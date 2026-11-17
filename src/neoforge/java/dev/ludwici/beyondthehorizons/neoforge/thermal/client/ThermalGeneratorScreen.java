package dev.ludwici.beyondthehorizons.neoforge.thermal.client;

import dev.ludwici.beyondthehorizons.BeyondTheHorizons;
import dev.ludwici.beyondthehorizons.neoforge.thermal.ThermalGeneratorBlock;
import dev.ludwici.beyondthehorizons.neoforge.thermal.ThermalGeneratorMenu;
import dev.ludwici.beyondthehorizons.neoforge.thermal.ThermalGeneratorRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import java.util.function.IntConsumer;

@EventBusSubscriber(modid = BeyondTheHorizons.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ThermalGeneratorScreen extends AbstractContainerScreen<ThermalGeneratorMenu> {

    private TemperatureSlider slider;
    private EditBox exactTemperature;
    private ThermalGeneratorBlock.Mode sliderMode;
    private boolean receivedInitialState;

    public ThermalGeneratorScreen(ThermalGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 218;
        titleLabelX = 8;
        titleLabelY = 8;
        inventoryLabelX = 8;
        inventoryLabelY = 124;
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ThermalGeneratorRegistry.GENERATOR_MENU.get(), ThermalGeneratorScreen::new);
    }

    @Override
    protected void init() {
        super.init();
        int min = menu.mode().min();
        int max = menu.mode().max();
        sliderMode = menu.mode();
        receivedInitialState = false;
        slider = addRenderableWidget(new TemperatureSlider(leftPos + 9, topPos + 40, 158, 16,
                min, max, menu.targetTemperature(), this::sendTarget));

        exactTemperature = new EditBox(font, leftPos + 9, topPos + 70, 76, 18,
                Component.translatable("screen.beyond_the_horizons.temperature_target"));
        exactTemperature.setMaxLength(5);
        exactTemperature.setValue(Integer.toString(menu.targetTemperature()));
        addRenderableWidget(exactTemperature);

        addRenderableWidget(Button.builder(Component.translatable("button.beyond_the_horizons.apply"),
                button -> {
                    try {
                        int selected = Integer.parseInt(exactTemperature.getValue().trim());
                        int clamped = Mth.clamp(selected, menu.mode().min(), menu.mode().max());
                        slider.setSelected(clamped);
                        sendTarget(clamped);
                        exactTemperature.setValue(Integer.toString(clamped));
                    } catch (NumberFormatException ignored) {
                        exactTemperature.setValue(Integer.toString(menu.targetTemperature()));
                    }
                }).bounds(leftPos + 90, topPos + 69, 77, 20).build());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (slider == null || exactTemperature == null) {
            return;
        }

        boolean stateAvailable = menu.targetTemperature() != 0
                || menu.mode() == ThermalGeneratorBlock.Mode.COOL;
        if (!receivedInitialState && stateAvailable) {
            receivedInitialState = true;
            sliderMode = menu.mode();
            slider.updateRange(sliderMode.min(), sliderMode.max(), menu.targetTemperature());
            exactTemperature.setValue(Integer.toString(menu.targetTemperature()));
        } else if (receivedInitialState && sliderMode != menu.mode()) {
            sliderMode = menu.mode();
            slider.updateRange(sliderMode.min(), sliderMode.max(), menu.targetTemperature());
            exactTemperature.setValue(Integer.toString(menu.targetTemperature()));
        }
    }

    private void sendTarget(int target) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, target + 200);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        int accent = menu.mode() == ThermalGeneratorBlock.Mode.COOL ? 0xff42c4db : 0xffff9449;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xff18232e);
        graphics.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xff30404c);
        graphics.fill(x + 5, y + 5, x + imageWidth - 5, y + 20, 0xff172b39);
        graphics.fill(x + 6, y + 25, x + imageWidth - 6, y + 115, 0xff1b2d37);
        graphics.fill(x + 7, y + 112, x + imageWidth - 7, y + 114, accent);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlot(graphics, x + 8 + column * 18, y + 138 + row * 18);
            }
        }

        for (int column = 0; column < 9; column++) {
            drawSlot(graphics, x + 8 + column * 18, y + 196);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(font, Component.translatable("screen.beyond_the_horizons.target",
                menu.targetTemperature()), 9, 27, 0xffe4f2f6, false);
        graphics.drawString(font, Component.translatable("screen.beyond_the_horizons.actual",
                menu.currentTemperature()), 9, 92, 0xffbbd9e1, false);
        graphics.drawString(font, Component.translatable("screen.beyond_the_horizons.energy",
                menu.storedEnergy(), 50_000, menu.energyPerTick()), 9, 102, 0xffbbd9e1, false);
        graphics.drawString(font, Component.translatable("screen.beyond_the_horizons.volume",
                menu.volume()), 9, 116, 0xffbbd9e1, false);
        graphics.drawString(font, Component.translatable("status.beyond_the_horizons.thermal." + menu.status()),
                102, 116, 0xffbbd9e1, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xff0e1c24);
        graphics.fill(x, y, x + 16, y + 16, 0xff566b76);
    }

    private static final class TemperatureSlider extends AbstractSliderButton {

        private int min;
        private int max;
        private final IntConsumer callback;
        private int lastSent;

        private TemperatureSlider(int x, int y, int width, int height, int min, int max,
                                  int initial, IntConsumer callback) {
            super(x, y, width, height, Component.empty(),
                    (double) (Mth.clamp(initial, min, max) - min) / (max - min));
            this.min = min;
            this.max = max;
            this.callback = callback;
            this.lastSent = initial;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(selected() + " °C"));
        }

        @Override
        protected void applyValue() {
            int selected = selected();
            if (selected != lastSent) {
                lastSent = selected;
                callback.accept(selected);
            }
        }

        private int selected() {
            return min + (int) Math.round(value * (max - min));
        }

        private void updateRange(int min, int max, int selected) {
            this.min = min;
            this.max = max;
            setSelected(selected);
        }

        private void setSelected(int selected) {
            this.value = (double) (Mth.clamp(selected, min, max) - min) / (max - min);
            this.lastSent = selected;
            updateMessage();
        }
    }
}
