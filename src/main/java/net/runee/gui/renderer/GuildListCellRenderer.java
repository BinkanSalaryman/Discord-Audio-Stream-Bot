package net.runee.gui.renderer;

import net.dv8tion.jda.api.entities.Guild;
import net.runee.misc.Utils;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class GuildListCellRenderer extends net.gui.renderer.StandardListCellRenderer<Guild> {
    private static GuildListCellRenderer instance;
    private static final Icon missingIcon = new ImageIcon(Utils.missingIcon(24));
    private static final Icon emptyIcon = new ImageIcon(new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB));

    public static GuildListCellRenderer getInstance() {
        if(instance == null) {
            instance = new GuildListCellRenderer();
        }
        return instance;
    }

    private Map<String, Icon> icons = new HashMap<>();

    private GuildListCellRenderer() {

    }

    public void clearIconCache(Guild value) {
        icons.remove(value.getIconUrl());
    }

    @Override
    protected void apply(Guild value) {
        setIcon(getIcon(value));
        setText(value.getName());
    }

    private Icon getIcon(Guild guild) {
        String iconUrl = guild.getIconUrl();
        if(iconUrl == null) {
            return emptyIcon;
        }
        Icon result = icons.get(iconUrl);
        if(result == null) {
            BufferedImage image = Utils.downloadImage(iconUrl);
            if (image == null) {
                result = missingIcon;
            } else {
                result = new ImageIcon(Utils.aspectFit(image, 24, 24));
            }
            icons.put(iconUrl, result);
        }
        return result;
    }
}
