package util;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Normalizes familiar India labels to India's single canonical IANA zone. */
public final class TimeZoneCatalog {
    private static final String INDIA_ZONE = "Asia/Kolkata";
    private static final Set<String> INDIA_REGIONS = new HashSet<>(Arrays.asList(
            "andhrapradesh", "arunachalpradesh", "assam", "bihar", "chhattisgarh", "goa", "gujarat",
            "haryana", "himachalpradesh", "jharkhand", "karnataka", "kerala", "madhyapradesh",
            "maharashtra", "manipur", "meghalaya", "mizoram", "nagaland", "odisha", "punjab",
            "rajasthan", "sikkim", "tamilnadu", "telangana", "tripura", "uttarpradesh", "uttarakhand",
            "westbengal", "andamanandnicobarislands", "chandigarh",
            "dadranagarhavelianddamananddiu", "delhi", "jammuandkashmir", "ladakh", "lakshadweep", "puducherry"));

    private TimeZoneCatalog() { }

    public static String canonicalize(String input) {
        if (input == null || input.trim().isEmpty())
            throw new DateTimeException("Time zone is required.");
        String value = input.trim();
        String lower = value.toLowerCase(Locale.ROOT);
        if (lower.equals("india") || lower.equals("india/calcutta") || lower.equals("india/kolkata") || lower.equals("india/delhi")
                || lower.equals("asia/calcutta") || lower.equals("asia/kolkata")) return INDIA_ZONE;

        String region = indiaRegion(value);
        if (region != null && INDIA_REGIONS.contains(region)) return INDIA_ZONE;

        ZoneId zone = ZoneId.of(value);
        return "Asia/Calcutta".equals(zone.getId()) ? INDIA_ZONE : zone.getId();
    }

    private static String indiaRegion(String value) {
        String lower = value.toLowerCase(Locale.ROOT).trim();
        String region = null;
        if (lower.startsWith("india/")) region = lower.substring("india/".length());
        else if (lower.startsWith("india ")) region = lower.substring("india ".length());
        else if (lower.startsWith("india-")) region = lower.substring("india-".length());
        if (region == null) return null;
        return region.replaceAll("[^a-z]", "");
    }
}
