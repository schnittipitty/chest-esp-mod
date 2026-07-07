package de.paulb.chestesp;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class ChestEspClient implements ClientModInitializer {
    private static final int SCAN_RADIUS_XZ = 48;
    private static final int SCAN_RADIUS_Y = 32;
    private static final int SCAN_INTERVAL_TICKS = 10;
    private static final KeyBinding.Category KEY_CATEGORY = KeyBinding.Category.create(Identifier.of("chestesp", "keys"));
    private static final RenderLayer LINE_LAYER = RenderLayer.of(
            "chestesp_lines",
            VertexFormats.LINES,
            208,
            false,
            true,
            RenderLayer.MultiPhaseParameters.builder()
                    .build(false)
    );

    private final List<Highlight> highlights = new ArrayList<>();
    private ChestEspConfig config;
    private int ticksUntilScan;

    @Override
    public void onInitializeClient() {
        config = ChestEspConfig.load();

        KeyBinding openConfig = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.chestesp.open_config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                KEY_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfig.wasPressed()) {
                client.setScreen(new ChestEspConfigScreen(config, client.currentScreen));
            }

            if (client.world == null || client.player == null) {
                highlights.clear();
                return;
            }

            if (--ticksUntilScan <= 0) {
                ticksUntilScan = SCAN_INTERVAL_TICKS;
                scan(client);
            }
        });

        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(context -> {
            if (config.enabled && !highlights.isEmpty()) {
                render(context);
            }
        });
    }

    private void scan(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            return;
        }

        highlights.clear();
        BlockPos center = client.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterateOutwards(center, SCAN_RADIUS_XZ, SCAN_RADIUS_Y, SCAN_RADIUS_XZ)) {
            BlockState state = client.world.getBlockState(pos);
            HighlightType type = getHighlightType(state);

            if (type != null && type.isEnabled(config)) {
                highlights.add(new Highlight(pos.toImmutable(), type));
            }
        }
    }

    private HighlightType getHighlightType(BlockState state) {
        Block block = state.getBlock();
        Identifier id = Registries.BLOCK.getId(block);
        String path = id.getPath();

        if (path.contains("copper_chest")) {
            return HighlightType.COPPER_CHEST;
        }

        if (block instanceof EnderChestBlock) {
            return HighlightType.ENDER_CHEST;
        }

        if (block instanceof BarrelBlock) {
            return HighlightType.BARREL;
        }

        if (block instanceof ChestBlock) {
            return HighlightType.CHEST;
        }

        return null;
    }

    private void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getCameraPos();
        MatrixStack matrices = context.matrices();
        VertexConsumer buffer = context.consumers().getBuffer(LINE_LAYER);

        for (Highlight highlight : highlights) {
            float[] color = highlight.type().color();
            Box box = new Box(highlight.pos()).expand(0.003).offset(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            drawBox(matrices, buffer, box, color);
        }
    }

    private void drawBox(MatrixStack matrices, VertexConsumer buffer, Box box, float[] color) {
        addLine(matrices, buffer, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, color);
        addLine(matrices, buffer, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, color);
        addLine(matrices, buffer, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ, color);
        addLine(matrices, buffer, box.minX, box.minY, box.maxZ, box.minX, box.minY, box.minZ, color);

        addLine(matrices, buffer, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, color);
        addLine(matrices, buffer, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, color);
        addLine(matrices, buffer, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ, color);
        addLine(matrices, buffer, box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ, color);

        addLine(matrices, buffer, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, color);
        addLine(matrices, buffer, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, color);
        addLine(matrices, buffer, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, color);
        addLine(matrices, buffer, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, color);
    }

    private void addLine(MatrixStack matrices, VertexConsumer buffer, double x1, double y1, double z1,
                         double x2, double y2, double z2, float[] color) {
        float normalX = (float) (x2 - x1);
        float normalY = (float) (y2 - y1);
        float normalZ = (float) (z2 - z1);
        float length = (float) Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
        if (length > 0) {
            normalX /= length;
            normalY /= length;
            normalZ /= length;
        }

        MatrixStack.Entry entry = matrices.peek();

        buffer.vertex(entry, (float) x1, (float) y1, (float) z1)
                .color(color[0], color[1], color[2], color[3])
                .normal(entry, normalX, normalY, normalZ);
        buffer.vertex(entry, (float) x2, (float) y2, (float) z2)
                .color(color[0], color[1], color[2], color[3])
                .normal(entry, normalX, normalY, normalZ);
    }

    private record Highlight(BlockPos pos, HighlightType type) {
    }

    enum HighlightType {
        CHEST(1.0F, 0.88F, 0.05F, 0.9F),
        ENDER_CHEST(0.55F, 0.12F, 1.0F, 0.9F),
        BARREL(0.9F, 0.52F, 0.16F, 0.9F),
        COPPER_CHEST(0.0F, 0.95F, 0.75F, 0.9F);

        private final float[] color;

        HighlightType(float red, float green, float blue, float alpha) {
            this.color = new float[]{red, green, blue, alpha};
        }

        float[] color() {
            return color;
        }

        boolean isEnabled(ChestEspConfig config) {
            return switch (this) {
                case CHEST -> config.chests;
                case ENDER_CHEST -> config.enderChests;
                case BARREL -> config.barrels;
                case COPPER_CHEST -> config.copperChests;
            };
        }

        Text label(ChestEspConfig config) {
            return switch (this) {
                case CHEST -> Text.literal("Chests: " + onOff(config.chests));
                case ENDER_CHEST -> Text.literal("Ender Chests: " + onOff(config.enderChests));
                case BARREL -> Text.literal("Barrels: " + onOff(config.barrels));
                case COPPER_CHEST -> Text.literal("Copper Chests: " + onOff(config.copperChests));
            };
        }

        void toggle(ChestEspConfig config) {
            switch (this) {
                case CHEST -> config.chests = !config.chests;
                case ENDER_CHEST -> config.enderChests = !config.enderChests;
                case BARREL -> config.barrels = !config.barrels;
                case COPPER_CHEST -> config.copperChests = !config.copperChests;
            }
        }

        private static String onOff(boolean enabled) {
            return enabled ? "ON" : "OFF";
        }
    }
}
