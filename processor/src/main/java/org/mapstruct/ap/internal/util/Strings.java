/*
 * Copyright MapStruct Authors.
 *
 * Licensed under the Apache License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.mapstruct.ap.internal.util;

import static org.mapstruct.ap.internal.util.Collections.asSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Helper class for dealing with strings.
 *
 * @author Gunnar Morling
 */
public class Strings {

    private static final Set<String> KEYWORDS = asSet(
        "abstract",
        "continue",
        "for",
        "new",
        "switch",
        "assert",
        "default",
        "goto",
        "package",
        "synchronized",
        "boolean",
        "do",
        "if",
        "private",
        "this",
        "break",
        "double",
        "implements",
        "protected",
        "throw",
        "byte",
        "else",
        "import",
        "public",
        "throws",
        "case",
        "enum",
        "instanceof",
        "return",
        "transient",
        "catch",
        "extends",
        "int",
        "short",
        "try",
        "char",
        "final",
        "interface",
        "static",
        "void",
        "class",
        "finally",
        "long",
        "strictfp",
        "volatile",
        "const",
        "float",
        "native",
        "super",
        "while"
    );

    private static final char UNDERSCORE = '_';

    private Strings() {
    }

    public static String capitalize(String string) {
        return string == null ? null : string.substring( 0, 1 ).toUpperCase( Locale.ROOT ) + string.substring( 1 );
    }

    public static String decapitalize(String string) {
        return string == null ? null : string.substring( 0, 1 ).toLowerCase( Locale.ROOT ) + string.substring( 1 );
    }

    public static String join(Iterable<?> iterable, String separator) {
        return join( iterable, separator, null );
    }

    public static <T> String join(Iterable<T> iterable, String separator, Extractor<T, String> extractor) {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;

        for ( T object : iterable ) {
            if ( !isFirst ) {
                sb.append( separator );
            }
            else {
                isFirst = false;
            }

            sb.append( extractor == null ? object : extractor.apply( object ) );
        }

        return sb.toString();
    }

    public static String joinAndCamelize(Iterable<?> iterable) {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;

        for ( Object object : iterable ) {
            if ( !isFirst ) {
                sb.append( capitalize( object.toString() ) );
            }
            else {
                sb.append( object );
                isFirst = false;
            }
        }

        return sb.toString();
    }

    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static boolean isNotEmpty(String string) {
        return !isEmpty( string );
    }

    public static String getSafeVariableName(String name, String... existingVariableNames) {
        return getSafeVariableName( name, Arrays.asList( existingVariableNames ) );
    }

    /**
     * Returns a variable name which doesn't conflict with the given variable names existing in the same scope and the
     * Java keywords.
     *
     * @param name the name to get a safe version for
     * @param existingVariableNames the names of other variables existing in the same scope
     *
     * @return a variable name based on the given original name, not conflicting with any of the given other names or
     * any Java keyword; starting with a lower-case letter
     */
    public static String getSafeVariableName(String name, Collection<String> existingVariableNames) {
        name = decapitalize( sanitizeIdentifierName( name ) );
        name = joinAndCamelize( extractParts( name ) );

        Set<String> conflictingNames = new HashSet<>( KEYWORDS );
        conflictingNames.addAll( existingVariableNames );

        if ( !conflictingNames.contains( name ) ) {
            return name;
        }

        int c = 1;
        String separator = Character.isDigit( name.charAt( name.length() - 1 ) ) ? "_" : "";
        while ( conflictingNames.contains( name + separator + c ) ) {
            c++;
        }

        return name + separator + c;
    }

    /**
     * @param identifier identifier to sanitize
     * @return the identifier without any characters that are not allowed as part of a Java identifier.
     */
    public static String sanitizeIdentifierName(String identifier) {
        if ( identifier != null && identifier.length() > 0 ) {

            int firstAlphabeticIndex = 0;
            while ( firstAlphabeticIndex < identifier.length() &&
                ( identifier.charAt( firstAlphabeticIndex ) == UNDERSCORE ||
                    Character.isDigit( identifier.charAt( firstAlphabeticIndex ) ) ) ) {
                firstAlphabeticIndex++;
            }

            if ( firstAlphabeticIndex < identifier.length()) {
                // If it is not consisted of only underscores
                String firstAlphaString = identifier.substring( firstAlphabeticIndex ).replace( "[]", "Array" );

                return firstAlphaString.codePoints()
                    .map(codePoint -> Character.isJavaIdentifierPart(codePoint) || codePoint == (int) '.' ? codePoint : (int) '_')
                    .collect(
                        StringBuilder::new,
                        StringBuilder::appendCodePoint,
                        StringBuilder::append
                    ).toString();
            }

            return identifier.replace( "[]", "Array" );
        }
        return identifier;
    }

    /**
     * Returns a stub property name from full class name by stripping away the package and decapitalizing the name
     * For example will return {@code fooBar} for {@code com.foo.bar.baz.FooBar} class name
     *
     * @param fullyQualifiedName fully qualified class name, such as com.foo.bar.baz.FooBar
     * @return stup property name, such as fooBar
     */
    public static String stubPropertyName(String fullyQualifiedName) {
        return Strings.decapitalize( fullyQualifiedName.substring( fullyQualifiedName.lastIndexOf( '.' ) + 1 ) );
    }

    /**
     * It removes the dots from the name and creates an {@link Iterable} from them.
     *
     * E.q. for the name {@code props.font} it will return an {@link Iterable} containing the {@code props} and
     * {@code font}
     * @param name the name that needs to be parsed into parts
     * @return an {@link Iterable} containing all the parts of the name.
     */
    static Iterable<String> extractParts(String name) {
        return Arrays.asList( name.split( "\\." ) );
    }

    public static String getMostSimilarWord(String word, Collection<String> similarWords) {
        int minLevenstein = Integer.MAX_VALUE;
        String mostSimilarWord = null;
        for ( String similarWord : similarWords ) {
            int levensteinDistance = levenshteinDistance( similarWord, word );
            if ( levensteinDistance < minLevenstein ) {
                minLevenstein = levensteinDistance;
                mostSimilarWord = similarWord;
            }
        }
        return mostSimilarWord;
    }

    private static int levenshteinDistance(String s, String t) {
        int sLength = s.length() + 1;
        int tLength = t.length() + 1;

        int[][] distances = new int[tLength][];
        for ( int i = 0; i < tLength; i++ ) {
            distances[i] = new int[sLength];
        }

        for ( int i = 0; i < tLength; i++ ) {
            distances[i][0] = i;
        }
        for ( int i = 0; i < sLength; i++ ) {
            distances[0][i] = i;
        }

        for ( int i = 1; i < tLength; i++ ) {
            for ( int j = 1; j < sLength; j++ ) {
                int cost = s.charAt( j - 1 ) == t.charAt( i - 1 ) ? 0 : 1;
                distances[i][j] = Math.min(
                    Math.min(
                        distances[i - 1][j] + 1,
                        distances[i][j - 1] + 1
                    ),
                    distances[i - 1][j - 1] + cost
                );
            }
        }

        return distances[tLength - 1][sLength - 1];
    }
}
