package cwr.cutomIterm.GUI;

import org.bukkit.Material;
import java.util.List;

public class GUIItem {
    private final String id;
    private final Material material;
    private final String name;
    private final List<String> lore;

    public GUIItem(String id, Material material, String name, List<String> lore) {
        this.id = id;
        this.material = material;
        this.name = name;
        this.lore = lore;
    }

    public String getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }
}