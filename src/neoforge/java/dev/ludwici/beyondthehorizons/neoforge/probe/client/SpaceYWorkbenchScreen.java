package dev.ludwici.beyondthehorizons.neoforge.probe.client;

import dev.ludwici.beyondthehorizons.BeyondTheHorizons;
import dev.ludwici.beyondthehorizons.neoforge.probe.SpaceYRegistry;
import dev.ludwici.beyondthehorizons.neoforge.probe.SpaceYWorkbenchMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = BeyondTheHorizons.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class SpaceYWorkbenchScreen extends AbstractContainerScreen<SpaceYWorkbenchMenu> {

    private static final int[][] MODULE_POSITIONS = {
            {14, 27}, {94, 27},
            {14, 51}, {94, 51},
            {14, 75}, {94, 75}
    };

    private SpaceYWorkbenchScreen(SpaceYWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 217;
        this.titleLabelX = 8;
        this.titleLabelY = 8;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 121;
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(SpaceYRegistry.WORKBENCH_MENU.get(), SpaceYWorkbenchScreen::new);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xff172630);
        graphics.fill(x + 2, y + 2, x + this.imageWidth - 2, y + this.imageHeight - 2, 0xff283b48);
        graphics.fill(x + 5, y + 5, x + this.imageWidth - 5, y + 21, 0xff11212d);
        graphics.fill(x + 5, y + 117, x + this.imageWidth - 5, y + 129, 0xff11212d);

        for (int[] position : MODULE_POSITIONS) {
            drawSlot(graphics, x + position[0], y + position[1]);
        }

        drawSlot(graphics, x + 80, y + 105);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlot(graphics, x + 8 + column * 18, y + 133 + row * 18);
            }
        }

        for (int column = 0; column < 9; column++) {
            drawSlot(graphics, x + 8 + column * 18, y + 191);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        drawLabel(graphics, "hull", 35, 32);
        drawLabel(graphics, "propulsion", 115, 32);
        drawLabel(graphics, "power", 35, 56);
        drawLabel(graphics, "communication", 115, 56);
        drawLabel(graphics, "protection", 35, 80);
        drawLabel(graphics, "payload", 115, 80);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xff0d1820);
        graphics.fill(x, y, x + 16, y + 16, 0xff506777);
    }

    private void drawLabel(GuiGraphics graphics, String name, int x, int y) {
        graphics.drawString(this.font, Component.translatable("slot.beyond_the_horizons." + name), x, y, 0xffd9e9ef, false);
    }
}
