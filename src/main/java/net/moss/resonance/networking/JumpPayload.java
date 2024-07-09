package net.moss.resonance.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record JumpPayload() implements CustomPayload {

    public static final PacketCodec<PacketByteBuf, JumpPayload> CODEC = CustomPayload.codecOf(JumpPayload::write, JumpPayload::new);
    public static final CustomPayload.Id<JumpPayload> ID = CustomPayload.id("resonance/jump");

    private JumpPayload(PacketByteBuf buf) { this(); }

    private void write(PacketByteBuf buf) { }

    @Override
    public Id<JumpPayload> getId() { return ID; }
}