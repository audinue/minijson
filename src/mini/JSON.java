package mini;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class JSON {

    private JSON() {
    }

    /**
     * Stringifies an object. Map keys must not be {@code null}.
     * <h4>Type Mapping</h4>
     * <table border="1">
     * <tr>
     * <th>Java Type</th>
     * <th>JSON Type</th>
     * </tr>
     * <tr>
     * <td>{@code java.lang.Boolean}</td>
     * <td>Boolean</td>
     * </tr>
     * <tr>
     * <td>{@code java.lang.Number}</td>
     * <td>Number</td>
     * </tr>
     * <tr>
     * <td>{@code java.lang.String}</td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td>{@code java.util.List}</td>
     * <td>Array</td>
     * </tr>
     * <tr>
     * <td>{@code java.util.Map}</td>
     * <td>Object</td>
     * </tr>
     * </table>
     *
     * @param object The object to be stringified or {@code null}.
     * @return The JSON string.
     * @throws UnsupportedOperationException if unsupported object type found.
     */
    public static String stringify(Object object) {
        StringBuilder sb = new StringBuilder();
        stringify(object, sb);
        return sb.toString();
    }

    /**
     * Parses a JSON string. Supported escape characters in JSON strings are
     * only {@code \\}, {@code \"}, {@code \r} and {@code \n}.
     * <h4>Type Mapping</h4>
     * <table border="1">
     * <tr>
     * <th>JSON Type</th>
     * <th>Java Type</th>
     * </tr>
     * <tr>
     * <td>Boolean</td>
     * <td>{@code java.lang.Boolean}</td>
     * </tr>
     * <tr>
     * <td>Number</td>
     * <td>{@code java.lang.Double}</td>
     * </tr>
     * <tr>
     * <td>String</td>
     * <td>{@code java.lang.String}</td>
     * </tr>
     * <tr>
     * <td>Array</td>
     * <td>{@code java.util.ArrayList}</td>
     * </tr>
     * <tr>
     * <td>Object</td>
     * <td>{@code java.util.HashMap}</td>
     * </tr>
     * </table>
     *
     * @param string The string to be parsed.
     * @return The parsed object.
     * @throws UnsupportedOperationException if JSON strings contains unsupported escape character.
     * @throws IllegalArgumentException if unrecognizable character found.
     */
    public static Object parse(String string) {
        return parse(string, new Int());
    }

    private static final char[] NULL = new char[] {'n', 'u', 'l', 'l'};
    private static final char[] TRUE = new char[] {'t', 'r', 'u', 'e'};
    private static final char[] FALSE = new char[] {'f', 'a', 'l', 's', 'e'};
    private static final char[] SLASH = new char[] {'\\', '\\'};
    private static final char[] QUOTE = new char[] {'\\', '"'};
    private static final char[] RETURN = new char[] {'\\', 'r'};
    private static final char[] LINE = new char[] {'\\', 'n'};

    private static void stringify(Object object, StringBuilder sb) {
        if (object == null) {
            sb.append(NULL);
        } else if (object == Boolean.TRUE) {
            sb.append(TRUE);
        } else if (object == Boolean.FALSE) {
            sb.append(FALSE);
        } else if (object instanceof Number) {
            sb.append(object);
        } else if (object instanceof String) {
            sb.append('"');
            String string = (String) object;
            for (int i = 0; i < string.length(); i++) {
                char c = string.charAt(i);
                switch (c) {
                    case '\\':
                        sb.append(SLASH);
                        break;
                    case '"':
                        sb.append(QUOTE);
                        break;
                    case '\r':
                        sb.append(RETURN);
                        break;
                    case '\n':
                        sb.append(LINE);
                        break;
                    default:
                        sb.append(c);
                        break;
                }
            }
            sb.append('"');
        } else if (object instanceof List) {
            sb.append('[');
            List list = (List) object;
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                stringify(list.get(i), sb);
            }
            sb.append(']');
        } else if (object instanceof Map) {
            sb.append('{');
            Iterator entries = ((Map) object).entrySet().iterator();
            boolean next = false;
            while (entries.hasNext()) {
                if (next) {
                    sb.append(',');
                } else {
                    next = true;
                }
                Map.Entry entry = (Map.Entry) entries.next();
                stringify(entry.getKey().toString(), sb);
                sb.append(':');
                stringify(entry.getValue(), sb);
            }
            sb.append('}');
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private static final Object SQUARE = new Object();
    private static final Object CURLY = new Object();
    private static final Object COLON = new Object();
    private static final Object COMMA = new Object();

    private static Object parse(String string, Int counter) {
        int i = counter.value;
        char c = string.charAt(i);
        if (isSpace(c)) {
            do {
                c = string.charAt(++i);
            } while (isSpace(c) && i < string.length());
            counter.value = i;
        }
        switch (c) {
            case 'n':
                counter.value = i + 4;
                return null;
            case 't':
                counter.value = i + 4;
                return Boolean.TRUE;
            case 'f':
                counter.value = i + 5;
                return Boolean.FALSE;
            case '[':
                List list = new ArrayList();
                counter.value++;
                while (true) {
                    Object item = parse(string, counter);
                    if (item == SQUARE) {
                        break;
                    }
                    if (item == COMMA) {
                        item = parse(string, counter);
                    }
                    list.add(item);
                }
                return list;
            case ']':
                counter.value++;
                return SQUARE;
            case '{':
                Map map = new HashMap();
                counter.value++;
                while (true) {
                    Object key = parse(string, counter);
                    if (key == CURLY) {
                        break;
                    }
                    if (key == COMMA) {
                        key = parse(string, counter);
                    }
                    parse(string, counter);
                    map.put(key, parse(string, counter));
                }
                return map;
            case '}':
                counter.value++;
                return CURLY;
            case ':':
                counter.value++;
                return COLON;
            case ',':
                counter.value++;
                return COMMA;
            case '"':
                StringBuilder sb = new StringBuilder();
                while (true) {
                    c = string.charAt(++i);
                    switch (c) {
                        case '\\':
                            c = string.charAt(++i);
                            switch (c) {
                                case '\\':
                                case '"':
                                    sb.append(c);
                                    break;
                                case 'r':
                                    sb.append('\r');
                                    break;
                                case 'n':
                                    sb.append('\n');
                                    break;
                                default:
                                    throw new UnsupportedOperationException();
                            }
                            break;
                        case '"':
                            counter.value = i + 1;
                            return sb.toString();
                        default:
                            sb.append(c);
                    }
                }
            default:
                if (isDigit(c) || c == '-') {
                    int start = i;
                    while (++i < string.length() && isDigit(string.charAt(i))) {
                    }
                    if (i < string.length() && string.charAt(i) == '.') {
                        while (++i < string.length() && isDigit(string.charAt(i))) {
                        }
                    }
                    counter.value = i;
                    return Double.parseDouble(string.substring(start, i));
                } else {
                    throw new IllegalArgumentException();
                }
        }
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isSpace(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n';
    }

    private static class Int {

        public int value;
    }
}
