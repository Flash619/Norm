package com.travistruttschel.norm.translation;

public class SnakeCaseNameTranslator implements NameTranslator {
    @Override
    public String to(String name) {
        char[] ca = name.toCharArray();
        StringBuilder n = new StringBuilder();

        for (char c :
                ca) {
            if (Character.isUpperCase(c)) {
                n.append(String.format("_%s", Character.toLowerCase(c)));
            } else {
                n.append(c);
            }
        }

        return n.toString();
    }

    @Override
    public String from(String name) {
        char[] ca = name.toCharArray();
        StringBuilder n = new StringBuilder();
        boolean nu = false;

        for (char c :
                ca) {
            if (c == '_') {
                nu = true;
            } else if (nu) {
                n.append(Character.toLowerCase(c));
            } else {
                n.append(c);
            }
        }

        return n.toString();
    }
}
