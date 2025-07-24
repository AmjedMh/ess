package com.teknokote.ess.core.model.configuration;

/**
 * Types de ports
 */
public enum EnumPortType {
    PUMP(PortType.PUMP), PROBE(PortType.PROBE), DEVICE(PortType.DEVICE);

    private EnumPortType(String val) {
        // Forces same name and value of enum
        if (!this.name().equals(val))
            throw new IllegalArgumentException("Incorrect use of EnumPortType");
    }

    public static class PortType {
        public static final String PUMP = "PUMP";
        public static final String PROBE = "PROBE";
        public static final String DEVICE = "DEVICE";

    private PortType() {
        throw new IllegalStateException("Utility class");
    }
    }
}
