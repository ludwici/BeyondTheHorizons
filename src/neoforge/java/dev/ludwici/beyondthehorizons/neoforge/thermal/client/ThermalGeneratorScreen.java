package dev.ludwici.beyondthehorizons.neoforge.thermal.client;

import dev.ludwici.beyondthehorizons.BeyondTheHorizons;
import dev.ludwici.beyondthehorizons.neoforge.thermal.ThermalGeneratorBlockEntity;
import dev.ludwici.beyondthehorizons.neoforge.thermal.ThermalGeneratorMenu;
import dev.ludwici.beyondthehorizons.neoforge.thermal.ThermalGeneratorRegistry;
import earth.terrarium.adastra.client.components.GravitySlider;
import earth.terrarium.adastra.client.utils.GuiUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = BeyondTheHorizons.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ThermalGeneratorScreen extends AbstractContainerScreen<ThermalGeneratorMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "ad_astra", "textures/gui/container/gravity_normalizer.png");
    private static final ResourceLocation STEEL_SLOT = ResourceLocation.fromNamespaceAndPath(
            "ad_astra", "textures/gui/container/slots/steel.png");

    private GravitySlider slider;
    private int displayedTemperature;
    private boolean receivedInitialState;

    public ThermalGeneratorScreen(ThermalGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 184;
        imageHeight = 215;
        titleLabelX = 8;
        titleLabelY = 11;
        inventoryLabelX = 12;
        inventoryLabelY = 116;
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ThermalGeneratorRegistry.GENERATOR_MENU.get(), ThermalGeneratorScreen::new);
    }

    @Override
    protected void init() {
        super.init();
        receivedInitialState = menu.isSynchronized();
        addTemperatureSlider(receivedInitialState ? menu.targetTemperature()
                : ThermalGeneratorBlockEntity.DEFAULT_TEMPERATURE);
    }

    private void addTemperatureSlider(int temperature) {
        displayedTemperature = temperature;
        slider = addRenderableWidget(new GravitySlider(
                leftPos + 25, topPos + 52, 108, 11, CommonComponents.EMPTY, temperature,
                ThermalGeneratorBlockEntity.MIN_TEMPERATURE,
                ThermalGeneratorBlockEntity.MAX_TEMPERATURE,
                this::sendTarget));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (!receivedInitialState && menu.isSynchronized()) {
            receivedInitialState = true;
            removeWidget(slider);
            addTemperatureSlider(menu.targetTemperature());
        }
    }

    private void sendTarget(double temperature) {
        int selected = Mth.clamp((int) Math.round(temperature),
                ThermalGeneratorBlockEntity.MIN_TEMPERATURE,
                ThermalGeneratorBlockEntity.MAX_TEMPERATURE);
        displayedTemperature = selected;
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                    selected - ThermalGeneratorBlockEntity.MIN_TEMPERATURE);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawInventorySlot(graphics, leftPos + 12 + column * 18, topPos + 130 + row * 18);
            }
        }

        for (int column = 0; column < 9; column++) {
            drawInventorySlot(graphics, leftPos + 12 + column * 18, topPos + 188);
        }

        GuiUtils.drawEnergyBar(graphics, mouseX, mouseY, leftPos + 145, topPos + 70,
                menu.storedEnergy(), ThermalGeneratorBlockEntity.CAPACITY);
        GuiUtils.drawColoredShadowCenteredString(graphics, font,
                Component.translatable("screen.beyond_the_horizons.target", displayedTemperature),
                leftPos + 78, topPos + 32, 0x8cf5f5, 0x32506e);
        GuiUtils.drawColoredShadowString(graphics, font,
                Component.translatable("screen.beyond_the_horizons.actual", menu.currentTemperature()),
                leftPos + 45, topPos + 71, 0x8cf5f5, 0x32506e);
        GuiUtils.drawColoredShadowString(graphics, font,
                Component.translatable("screen.beyond_the_horizons.energy", menu.energyPerTick()),
                leftPos + 45, topPos + 82, 0x8cf5f5, 0x32506e);
        GuiUtils.drawColoredShadowString(graphics, font,
                Component.translatable("screen.beyond_the_horizons.volume", menu.volume()),
                leftPos + 45, topPos + 93, 0x8cf5f5, 0x32506e);
        GuiUtils.drawColoredShadowString(graphics, font,
                Component.translatable("status.beyond_the_horizons.thermal." + menu.status()),
                leftPos + 45, topPos + 104, 0x8cf5f5, 0x32506e);
    }

    private static void drawInventorySlot(GuiGraphics graphics, int x, int y) {
        graphics.blit(STEEL_SLOT, x - 1, y - 1, 0, 0, 18, 18, 18, 18);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (slider.isFocused()) {
            slider.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
}
