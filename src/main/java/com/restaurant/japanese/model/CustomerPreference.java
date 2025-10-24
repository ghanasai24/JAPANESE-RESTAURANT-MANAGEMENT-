package com.restaurant.japanese.model;

public class CustomerPreference {
    private boolean isJain;

    public boolean isJain() { return isJain; }
    public void setJain(boolean jain) { isJain = jain; }

    @Override
    public String toString() {
        return isJain ? "JAIN (No Onion/Garlic)" : "Standard";
    }
}