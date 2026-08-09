package com.vurses.offlineauth;

/**
 * Example configuration POJO.
 *
 * Configurations are saved and loaded to JSON files.
 *
 * Save and load is handled automatically, happens on every command execution, proxy start/stop, etc.
 *
 * All fields should be public and mutable.
 *
 * Fields to static inner classes generate nested JSON objects.
 */
public class OfflineAuthConfig {
    public boolean enabled = true;
}
