

package fit.lab;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;


public enum FileSystem {

  
    GENERIC(false, false, Integer.MAX_VALUE, Integer.MAX_VALUE, new char[] { 0 }, new String[] {}, false, '/'),

   
    LINUX(true, true, 255, 4096, new char[] {
            
    }, new String[] {}, false, '/'),

   
    MAC_OSX(true, true, 255, 1024, new char[] {
           
    }, new String[] {}, false, '/'),

   
    WINDOWS(false, true, 255,
            32000, new char[] {
                    
                    0,
                   
                    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
                    29, 30, 31,
                    '"', '*', '/', ':', '<', '>', '?', '\\', '|'
            }, 
            new String[] { "AUX", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "CON", "LPT1",
                    "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9", "NUL", "PRN" }, true, '\\');

    
    private static final boolean IS_OS_LINUX = getOsMatchesName("Linux");

   
    private static final boolean IS_OS_MAC = getOsMatchesName("Mac");

  
    private static final String OS_NAME_WINDOWS_PREFIX = "Windows";

   
    private static final boolean IS_OS_WINDOWS = getOsMatchesName(OS_NAME_WINDOWS_PREFIX);

    private static final FileSystem CURRENT = current();

  
    private static FileSystem current() {
        if (IS_OS_LINUX) {
            return LINUX;
        }
        if (IS_OS_MAC) {
            return MAC_OSX;
        }
        if (IS_OS_WINDOWS) {
            return WINDOWS;
        }
        return GENERIC;
    }

    
    public static FileSystem getCurrent() {
        return CURRENT;
    }

  
    private static boolean getOsMatchesName(final String osNamePrefix) {
        return isOsNameMatch(getSystemProperty("os.name"), osNamePrefix);
    }

    private static String getSystemProperty(final String property) {
        try {
            return System.getProperty(property);
        } catch (final SecurityException ex) {
            System.err.println("Caught a SecurityException reading the system property '" + property
                    + "'; the SystemUtils property value will default to null.");
            return null;
        }
    }

   
    private static boolean isOsNameMatch(final String osName, final String osNamePrefix) {
        if (osName == null) {
            return false;
        }
        return osName.toUpperCase(Locale.ROOT).startsWith(osNamePrefix.toUpperCase(Locale.ROOT));
    }

   
    private static String replace(final String path, final char oldChar, final char newChar) {
        return path == null ? null : path.replace(oldChar, newChar);
    }

    private final boolean casePreserving;
    private final boolean caseSensitive;
    private final char[] illegalFileNameChars;
    private final int maxFileNameLength;
    private final int maxPathLength;
    private final String[] reservedFileNames;
    private final boolean supportsDriveLetter;
    private final char nameSeparator;

    private final char nameSeparatorOther;

    
    FileSystem(final boolean caseSensitive, final boolean casePreserving, final int maxFileLength,
        final int maxPathLength, final char[] illegalFileNameChars, final String[] reservedFileNames,
        final boolean supportsDriveLetter, final char nameSeparator) {
        this.maxFileNameLength = maxFileLength;
        this.maxPathLength = maxPathLength;
        this.illegalFileNameChars = Objects.requireNonNull(illegalFileNameChars, "illegalFileNameChars");
        this.reservedFileNames = Objects.requireNonNull(reservedFileNames, "reservedFileNames");
        this.caseSensitive = caseSensitive;
        this.casePreserving = casePreserving;
        this.supportsDriveLetter = supportsDriveLetter;
        this.nameSeparator = nameSeparator;
        this.nameSeparatorOther = FilenameUtils.flipSeparator(nameSeparator);
    }

   
    public char[] getIllegalFileNameChars() {
        return this.illegalFileNameChars.clone();
    }

   
    public int getMaxFileNameLength() {
        return maxFileNameLength;
    }

   
    public int getMaxPathLength() {
        return maxPathLength;
    }

   
    public char getNameSeparator() {
        return nameSeparator;
    }

   
    public String[] getReservedFileNames() {
        return reservedFileNames.clone();
    }

   
    public boolean isCasePreserving() {
        return casePreserving;
    }

   
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

   
    private boolean isIllegalFileNameChar(final char c) {
        return Arrays.binarySearch(illegalFileNameChars, c) >= 0;
    }

  
    public boolean isLegalFileName(final CharSequence candidate) {
        if (candidate == null || candidate.length() == 0 || candidate.length() > maxFileNameLength) {
            return false;
        }
        if (isReservedFileName(candidate)) {
            return false;
        }
        for (int i = 0; i < candidate.length(); i++) {
            if (isIllegalFileNameChar(candidate.charAt(i))) {
                return false;
            }
        }
        return true;
    }

   
    public boolean isReservedFileName(final CharSequence candidate) {
        return Arrays.binarySearch(reservedFileNames, candidate) >= 0;
    }

   
    public String normalizeSeparators(final String path) {
        return replace(path, nameSeparatorOther, nameSeparator);
    }

   
    public boolean supportsDriveLetter() {
        return supportsDriveLetter;
    }

    
    public String toLegalFileName(final String candidate, final char replacement) {
        if (isIllegalFileNameChar(replacement)) {
            throw new IllegalArgumentException(
                    String.format("The replacement character '%s' cannot be one of the %s illegal characters: %s",
                            replacement == '\0' ? "\\0" : replacement, name(), Arrays.toString(illegalFileNameChars)));
        }
        final String truncated = candidate.length() > maxFileNameLength ? candidate.substring(0, maxFileNameLength)
                : candidate;
        boolean changed = false;
        final char[] charArray = truncated.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            if (isIllegalFileNameChar(charArray[i])) {
                charArray[i] = replacement;
                changed = true;
            }
        }
        return changed ? String.valueOf(charArray) : truncated;
    }
}