package beezing.ormotd;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class OnlyRandomMotd implements DedicatedServerModInitializer {
	public static final String MOD_ID = "only-random-motd";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private final Random random = new Random();

	private static final Map<Character, String> SMALL_CAPS = Map.ofEntries(
			Map.entry('a', "ᴀ"),
			Map.entry('b', "ʙ"),
			Map.entry('c', "ᴄ"),
			Map.entry('d', "ᴅ"),
			Map.entry('e', "ᴇ"),
			Map.entry('f', "ꜰ"),
			Map.entry('g', "ɢ"),
			Map.entry('h', "ʜ"),
			Map.entry('i', "ɪ"),
			Map.entry('j', "ᴊ"),
			Map.entry('k', "ᴋ"),
			Map.entry('l', "ʟ"),
			Map.entry('m', "ᴍ"),
			Map.entry('n', "ɴ"),
			Map.entry('o', "ᴏ"),
			Map.entry('p', "ᴘ"),
			Map.entry('q', "ǫ"),
			Map.entry('r', "ʀ"),
			Map.entry('s', "s"),
			Map.entry('t', "ᴛ"),
			Map.entry('u', "ᴜ"),
			Map.entry('v', "ᴠ"),
			Map.entry('w', "ᴡ"),
			Map.entry('x', "x"),
			Map.entry('y', "ʏ"),
			Map.entry('z', "ᴢ")
	);

	public static OnlyRandomMotd INSTANCE;

	private final Path configPath = Path.of("config", "ormotd.yml");
	private Config config;

	@Override
	public void onInitializeServer() {
		INSTANCE = this;
		this.config = Config.loadConfig(configPath);
		LOGGER.info("loaded correctly");
	}

	/// Choose a random MOTD from list
	public Text getMotd() {
		List<String> motds = config.motds;

		if (motds.isEmpty())
			return Text.empty();

		String selec = motds.get(random.nextInt(motds.size()));
		return parseColorCodes(selec);
	}

	/// Handle strings, apply style and color to text and change to Text type
	public static Text parseColorCodes(String input) {
		MutableText result = Text.empty();
		Style currentStyle = Style.EMPTY;
		StringBuilder buffer = new StringBuilder();
		boolean smallCapsEnabled = false;

		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);

			if ((c == '&' || c == '§') && i + 1 < input.length()) {
				char code = Character.toLowerCase(input.charAt(i + 1));
				Formatting formatting = Formatting.byCode(code);

				if (formatting != null) {
					// Flush accumulated text with current style
					if (!buffer.isEmpty()) {
						result.append(
								Text.literal(buffer.toString()).setStyle(currentStyle)
						);
						buffer.setLength(0);
					}

					// Apply formatting
					if (formatting.isColor()) { // color
						currentStyle = Style.EMPTY.withColor(formatting);
					} else { // other text effects
						currentStyle = switch (formatting) {
							case BOLD -> currentStyle.withBold(true);
							case ITALIC -> currentStyle.withItalic(true);
							case UNDERLINE -> currentStyle.withUnderline(true);
							case STRIKETHROUGH -> currentStyle.withStrikethrough(true);
							case OBFUSCATED -> currentStyle.withObfuscated(true);
							case RESET -> {
								smallCapsEnabled = false;
								yield Style.EMPTY;
							}
							default -> currentStyle;
						};
					}

					i++; // Skip formatting code character
					continue;
				}
				if (code == '^') {
					smallCapsEnabled = true;
					i++;
					continue;
				}
			}

			if (smallCapsEnabled && c != '&' && c != '§') {
				String mapped = SMALL_CAPS.get(Character.toLowerCase(c));
				if (mapped != null) {
					buffer.append(mapped);
				} else {
					buffer.append(c);
				}
			} else {
				buffer.append(c);
			}
		}

		if (!buffer.isEmpty()) {
			result.append(
					Text.literal(buffer.toString()).setStyle(currentStyle)
			);
		}

		return result;
	}
}