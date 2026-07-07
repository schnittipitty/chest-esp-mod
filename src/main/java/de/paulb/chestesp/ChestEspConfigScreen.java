package de.paulb.chestesp;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

final class ChestEspConfigScreen extends Screen {
    private final ChestEspConfig config;
    private final Screen parent;

    ChestEspConfigScreen(ChestEspConfig config, Screen parent) {
        super(Text.literal("Chest ESP"));
        this.config = config;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = 180;
        int x = (width - buttonWidth) / 2;
        int y = height / 2 - 74;

        addDrawableChild(ButtonWidget.builder(globalLabel(), button -> {
            config.enabled = !config.enabled;
            config.save();
            button.setMessage(globalLabel());
        }).dimensions(x, y, buttonWidth, 20).build());

        y += 26;

        for (ChestEspClient.HighlightType type : ChestEspClient.HighlightType.values()) {
            int buttonY = y;
            addDrawableChild(ButtonWidget.builder(type.label(config), button -> {
                type.toggle(config);
                config.save();
                button.setMessage(type.label(config));
            }).dimensions(x, buttonY, buttonWidth, 20).build());
            y += 26;
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> close())
                .dimensions(x, y + 8, buttonWidth, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 104, 0xFFFFFF);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    private Text globalLabel() {
        return Text.literal("Chest ESP: " + (config.enabled ? "ON" : "OFF"));
    }
}
