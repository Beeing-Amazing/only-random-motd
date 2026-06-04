package beezing.ormotd.mixin;

import beezing.ormotd.OnlyRandomMotd;
import net.minecraft.server.ServerMetadata;

import net.minecraft.server.network.ServerQueryNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerQueryNetworkHandler.class)
public class ServerQueryNetworkHandlerMixin {

	@ModifyArg(
			method = "onRequest",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/packet/s2c/query/QueryResponseS2CPacket;<init>(Lnet/minecraft/server/ServerMetadata;)V"
			)
	)
	private ServerMetadata modifyMetadata(ServerMetadata metadata) {
		Text newMotd = OnlyRandomMotd.INSTANCE.getMotd();
		return new ServerMetadata(
				newMotd,
				metadata.players(),
				metadata.version(),
				metadata.favicon(),
				metadata.secureChatEnforced()
		);
	}
}