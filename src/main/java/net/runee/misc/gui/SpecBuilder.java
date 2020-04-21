package net.runee.misc.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpecBuilder {
    private List<String> components = new ArrayList<>();
    private Map<Integer, String> gaps = new HashMap<>();

    public static SpecBuilder create() {
        return new SpecBuilder();
    }

    public SpecBuilder add(String component) {
        return add(component, 1);
    }

    public SpecBuilder add(String component, int times) {
        for (int i = 0; i < times; i++) {
            components.add(component);
        }
        return this;
    }

    public SpecBuilder gapUnrelated() {
        return gap("12dlu");
    }

    public SpecBuilder gap(String gap) {
        gaps.put(components.size() - 1, gap);
        return this;
    }

    public String build() {
        if (components.isEmpty()) {
            return "";
        }
        List<String> elements = new ArrayList<>();
        for (int i = 0; i < components.size(); i++) {
            String component = components.get(i);
            elements.add(component);
            if(i != components.size() - 1) {
                String gap = gaps.get(i);
                if(gap == null) {
                    gap = "3dlu";
                }
                elements.add(gap);
            }
        }

        return String.join(",", elements);
    }
}
