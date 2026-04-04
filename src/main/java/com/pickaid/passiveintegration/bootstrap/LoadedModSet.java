package com.pickaid.passiveintegration.bootstrap;

@FunctionalInterface
public interface LoadedModSet {
    boolean isLoaded(String modId);
}
