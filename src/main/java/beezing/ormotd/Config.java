package beezing.ormotd;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Config {
    boolean enabled = true;
    List<String> motds = new ArrayList<>();
    boolean do_line1_override = false;
    boolean do_line2_override = false;
    String l1_overr = "";
    String l2_overr = "";

    /// Read config file and parse single-line overrides
    private void readFile(Path path) throws IOException {
        Yaml yaml = new Yaml();
        try (InputStream in = Files.newInputStream(path)) {
            ConfigData data = yaml.loadAs(in, ConfigData.class);

            this.enabled = data.enabled;
            this.motds = data.motds;
            this.do_line1_override = data.do_line1_override;
            this.do_line2_override = data.do_line2_override;
            this.l1_overr = data.override_line1;
            this.l2_overr = data.override_line2;

            parseOverrides();
            OnlyRandomMotd.LOGGER.info("correctly read from config file");
        }
    }
    private static class ConfigData {
        public boolean enabled = true;
        public List<String> motds = new ArrayList<>();
        public boolean do_line1_override = false;
        public boolean do_line2_override = false;
        public String override_line1 = "";
        public String override_line2 = "";
    }

    /// If overrides are enabled, change for that line for all MOTDs with default
    private void parseOverrides() {
        if (!this.do_line1_override && !this.do_line2_override)
            return;
        if (this.motds == null || this.motds.isEmpty())
            return;

        List<String> motds_l1 = new ArrayList<>();
        List<String> motds_l2 = new ArrayList<>();
        List<String> parsed = new ArrayList<>();

        this.motds.forEach(s -> {
            String[] parts = s.split("\n", 2);
            motds_l1.add(parts[0]);
            motds_l2.add(parts.length > 1 ? parts[1] : "");
        });

        if (this.do_line1_override && this.do_line2_override) {
            this.motds = new ArrayList<>();
            this.motds.add(this.l1_overr + "\n" + this.l2_overr);
        } else {
            for (int i = 0; i < this.motds.size(); ++i) {
                parsed.add(
                    (this.do_line1_override ? this.l1_overr : motds_l1.get(i))
                    + "\n"
                    + (this.do_line2_override ? this.l2_overr : motds_l2.get(i))
                );
            }
            this.motds = parsed;
        }
    }

    private void saveFile(Path path) {
        DumperOptions options = new DumperOptions();
        options.setPrettyFlow(true);

        Representer representer = new Representer(options);
        representer.addClassTag(ConfigData.class, Tag.MAP);

        Yaml yaml = new Yaml(representer, options);

        ConfigData data = new ConfigData();
        data.enabled = this.enabled;
        data.motds = this.motds;
        data.do_line1_override = this.do_line1_override;
        data.do_line2_override = this.do_line2_override;
        data.override_line1 = this.l1_overr;
        data.override_line2 = this.l2_overr;

        try {
            // ensure parent directory exists
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            // write YAML to file
            try (var writer = Files.newBufferedWriter(path)) {
                yaml.dump(data, writer);
                OnlyRandomMotd.LOGGER.info("correctly written to config file");
            }

        } catch (IOException e) {
            OnlyRandomMotd.LOGGER.error("failed to save config: {}", e.getMessage());
        }
    }

    public static Config loadConfig(Path path) {
        if (!Files.exists(path)) {
            OnlyRandomMotd.LOGGER.info("creating empty config...");
            Config conf = new Config();
            conf.saveFile(path); // here only so that files are never overwritten
            return conf;
        }

        // else
        try {
            Config conf = new Config();
            conf.readFile(path);
            return conf;
        } catch (IOException e) {
            // file read error
            OnlyRandomMotd.LOGGER.error("failed to load config: {}", e.getMessage());
            return new Config();
        }
    }
}
